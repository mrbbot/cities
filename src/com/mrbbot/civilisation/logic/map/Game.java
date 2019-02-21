package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.PlayerStats;
import com.mrbbot.civilisation.logic.interfaces.Mappable;
import com.mrbbot.civilisation.logic.interfaces.TurnHandler;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Terrain;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.net.ServerOnly;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Game implements Mappable, TurnHandler {
  private String name;
  public HexagonGrid<Tile> hexagonGrid;
  public ArrayList<City> cities;
  public ArrayList<Unit> units;
  public ArrayList<Player> players;
  private Map<String, Integer> playerScienceCounts;
  private Map<String, Integer> playerGoldCounts;
  @ServerOnly
  public Map<String, Boolean> readyPlayers = new HashMap<>();
  @ClientOnly
  public Unit selectedUnit = null;
  @ClientOnly
  public boolean waitingForPlayers = false;
  @ClientOnly
  private String currentPlayerId;
  @ClientOnly
  private Consumer<PlayerStats> playerStatsListener;

  public Game(String name) {
    this.name = name;

    //hexagonGrid = new HexagonGrid<>(40, 34, 1);
    //hexagonGrid = new HexagonGrid<>(20, 17, 1);
    hexagonGrid = new HexagonGrid<>(5, 5, 1);
    //hexagonGrid = new HexagonGrid<>(100, 40, 1);
    //hexagonGrid = new HexagonGrid<>(1, 1, 1);

    hexagonGrid.forEach((_tile, hex, x, y) -> hexagonGrid.set(x, y, new Tile(hex, x, y)));
    cities = new ArrayList<>();
    units = new ArrayList<>();
    players = new ArrayList<>();

    playerScienceCounts = new HashMap<>();
    playerGoldCounts = new HashMap<>();
  }

  public Game(Map<String, Object> map) {
    this.name = (String) map.get("name");

    //noinspection unchecked
    this.players = (ArrayList<Player>) ((List<String>) map.get("players")).stream()
      .map(Player::new)
      .collect(Collectors.toList());

    //noinspection unchecked
    List<List<Map<String, Object>>> terrainList = (List<List<Map<String, Object>>>) map.get("terrain");
    int height = terrainList.size();
    for (int y = 0; y < height; y++) {
      List<Map<String, Object>> row = terrainList.get(y);
      int width = row.size();

      if (hexagonGrid == null) {
        hexagonGrid = new HexagonGrid<>(width, height, 1);
      }

      for (int x = 0; x < width; x++) {
        Map<String, Object> terrain = row.get(x);

        double terrainHeight = (double) terrain.get("height");
        boolean tree = (boolean) terrain.get("tree");

        hexagonGrid.set(x, y, new Tile(hexagonGrid.getHexagon(x, y), x, y, terrainHeight, tree));
      }
    }

    assert hexagonGrid != null;

    //noinspection unchecked
    this.cities = (ArrayList<City>) ((List<Map<String, Object>>) map.get("cities")).stream()
      .map(m -> new City(hexagonGrid, m))
      .collect(Collectors.toList());

    //noinspection unchecked
    this.units = (ArrayList<Unit>) ((List<Map<String, Object>>) map.get("units")).stream()
      .map(m -> new Unit(hexagonGrid, m))
      .collect(Collectors.toList());

    //noinspection unchecked
    this.playerScienceCounts = (Map<String, Integer>) map.get("science");
    //noinspection unchecked
    this.playerGoldCounts = (Map<String, Integer>) map.get("gold");
  }

  public void setCurrentPlayer(String currentPlayerId, Consumer<PlayerStats> playerStatsListener) {
    this.currentPlayerId = currentPlayerId;
    this.playerStatsListener = playerStatsListener;
    sendPlayerStats();
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    map.put("name", name);

    List<String> playerList = players.stream()
      .map(player -> player.id)
      .collect(Collectors.toList());
    map.put("players", playerList);

    ArrayList<ArrayList<Map<String, Object>>> terrainList = new ArrayList<>();
    int gridWidth = hexagonGrid.getWidth() + 1;
    int gridHeight = hexagonGrid.getHeight();
    for (int y = 0; y < gridHeight; y++) {
      ArrayList<Map<String, Object>> row = new ArrayList<>();
      for (int x = 0; x < gridWidth - ((y + 1) % 2); x++) {
        Map<String, Object> terrainMap = new HashMap<>();
        Terrain terrain = hexagonGrid.get(x, y).getTerrain();

        terrainMap.put("height", terrain.height);
        terrainMap.put("tree", terrain.hasTree);

        row.add(terrainMap);
      }
      terrainList.add(row);
    }
    map.put("terrain", terrainList);

    List<Map<String, Object>> cityList = cities.stream()
      .map(City::toMap)
      .collect(Collectors.toList());
    map.put("cities", cityList);

    List<Map<String, Object>> unitList = units.stream()
      .map(Unit::toMap)
      .collect(Collectors.toList());
    map.put("units", unitList);

    map.put("science", playerScienceCounts);
    map.put("gold", playerGoldCounts);

    return map;
  }

  public boolean containsPlayerWithId(String id) {
    for (Player player : players) {
      if (player.id.equals(id)) return true;
    }
    return false;
  }

  private Tile[] handleUnitCreatePacket(PacketUnitCreate packet) {
    ArrayList<Tile> checkedTiles = new ArrayList<>();
    Queue<Tile> placementTilesToCheck = new LinkedList<>();
    placementTilesToCheck.add(hexagonGrid.get(packet.x, packet.y));

    Tile tileToCheck;
    while((tileToCheck = placementTilesToCheck.poll()) != null) {
      boolean tileIsCapital = false;
      if(tileToCheck.city != null) {
        tileIsCapital = tileToCheck.city.getCenter().samePositionAs(tileToCheck);
      }
      if(!tileIsCapital && tileToCheck.unit == null && tileToCheck.canTraverse()) {
        break;
      }

      checkedTiles.add(tileToCheck);

      placementTilesToCheck.addAll(
        hexagonGrid.getNeighbours(
          tileToCheck.x,
          tileToCheck.y,
          false
        ).stream()
          .filter(tile -> !checkedTiles.contains(tile))
          .collect(Collectors.toList())
      );
    }

    if(tileToCheck != null) {
      //found space
      Player player = new Player(packet.id);
      Unit unit = new Unit(player, tileToCheck, packet.getUnitType());
      units.add(unit);

      return new Tile[]{tileToCheck};
    }

    return null;
  }

  private Tile[] handleUnitMovePacket(PacketUnitMove packet) {
    Tile startTile = hexagonGrid.get(packet.startX, packet.startY);
    Tile endTile = hexagonGrid.get(packet.endX, packet.endY);

    startTile.unit.remainingMovementPointsThisTurn -= packet.usedMovementPoints;
    assert startTile.unit.remainingMovementPointsThisTurn >= 0;
    startTile.unit.tile = endTile;
    endTile.unit = startTile.unit;
    startTile.unit = null;

    return new Tile[]{startTile, endTile};
  }

  private Tile[] handleUnitDeletePacket(PacketUnitDelete packet) {
    Tile tile = hexagonGrid.get(packet.x, packet.y);

    Unit unit = tile.unit;
    if (unit != null) {
      tile.unit = null;
      units.remove(unit);
    }

    return new Tile[]{tile};
  }

  private Tile[] handleUnitDamagePacket(PacketUnitDamage packet) {
    Tile attackerTile = hexagonGrid.get(packet.attackerX, packet.attackerY);
    Tile targetTile = hexagonGrid.get(packet.targetX, packet.targetY);

    Unit attacker = attackerTile.unit;
    Unit target = targetTile.unit;

    if (attacker == null || target == null) return null;

    Point2D targetPos = target.tile.getHexagon().getCenter();
    Point2D attackerPos = attacker.tile.getHexagon().getCenter();
    double distanceBetween = targetPos.distance(attackerPos);
    int tilesBetween = (int) Math.round(distanceBetween / Hexagon.SQRT_3);
    if (attacker.unitType.hasAbility(UnitAbility.ABILITY_ATTACK) && tilesBetween <= 1) {
      target.health -= attacker.unitType.getAttackStrength();
      attacker.health -= target.unitType.getBaseHealth() / 5;
      attacker.hasAttackedThisTurn = true;
    }
    if (attacker.unitType.hasAbility(UnitAbility.ABILITY_RANGED_ATTACK) && tilesBetween <= 2) {
      target.health -= attacker.unitType.getAttackStrength();
      attacker.hasAttackedThisTurn = true;
    }

    return new Tile[]{attackerTile, targetTile};
  }

  private Tile[] handleCityCreate(PacketCityCreate packet) {
    Player player = new Player(packet.id);
    cities.add(new City(hexagonGrid, packet.x, packet.y, player));
    sendPlayerStats();
    return new Tile[]{};
  }

  private Tile[] handleCityGrow(PacketCityGrow packet) {
    for (City city : cities) {
      Tile center = city.getCenter();
      if (center.x == packet.x && center.y == packet.y) {
        city.growTo(packet.getGrownTo());
        break;
      }
    }
    sendPlayerStats();
    return new Tile[]{};
  }

  private Tile[] handleCityRename(PacketCityRename packet) {
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if(t.city != null) {
      t.city.name = packet.newName;
    }
    return null;
  }

  private Tile[] handleCityBuildRequest(PacketCityBuildRequest packet) {
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if(t.city != null) {
      CityBuildable buildable = packet.getBuildable();
      if(packet.withProduction) {
        t.city.currentlyBuilding = buildable;
      } else if(buildable.canBuildWithGold(getPlayerGoldTotal(t.city.player.id))) {
        increasePlayerGoldBy(t.city.player.id, -buildable.getGoldCost());
        buildable.build(t.city, this);
      }

    }
    return null;
  }

  @Override
  public Tile[] handleTurn(Game game) {
    ArrayList<Tile> updatedTiles = new ArrayList<>();

    for (Unit unit : units) {
      Tile[] unitUpdatedTiles = unit.handleTurn(this);
      if(unitUpdatedTiles != null) Collections.addAll(updatedTiles, unitUpdatedTiles);
    }

    for (City city : cities) {
      city.handleTurn(this);
    }

    waitingForPlayers = false;
    readyPlayers.clear();

    sendPlayerStats();

    return updatedTiles.toArray(new Tile[]{});
  }

  @ClientOnly
  private void sendPlayerStats() {
    if(currentPlayerId != null && playerStatsListener != null) {
      int totalSciencePerTurn = 0;
      int totalGoldPerTurn = 0;
      for (City city : cities) {
        if(city.player.id.equals(currentPlayerId)) {
          totalSciencePerTurn += city.getSciencePerTurn();
          totalGoldPerTurn += city.getGoldPerTurn();
        }
      }
      playerStatsListener.accept(new PlayerStats(
        totalSciencePerTurn,
        getPlayerGoldTotal(currentPlayerId),
        totalGoldPerTurn
      ));
    }
  }

  public Tile[] handlePacket(Packet packet) {
    if (packet instanceof PacketPlayerChange) {
      String newId = ((PacketPlayerChange) packet).id;
      if (!containsPlayerWithId(newId)) {
        players.add(new Player(newId));
      }
    } else if (packet instanceof PacketCityCreate) {
      return handleCityCreate((PacketCityCreate) packet);
    } else if (packet instanceof PacketCityGrow) {
      return handleCityGrow((PacketCityGrow) packet);
    } else if (packet instanceof PacketUnitCreate) {
      return handleUnitCreatePacket((PacketUnitCreate) packet);
    } else if (packet instanceof PacketUnitMove) {
      return handleUnitMovePacket((PacketUnitMove) packet);
    } else if (packet instanceof PacketUnitDelete) {
      return handleUnitDeletePacket((PacketUnitDelete) packet);
    } else if (packet instanceof PacketUnitDamage) {
      return handleUnitDamagePacket((PacketUnitDamage) packet);
    } else if(packet instanceof PacketCityRename) {
      return handleCityRename((PacketCityRename) packet);
    } else if(packet instanceof PacketCityBuildRequest) {
      return handleCityBuildRequest((PacketCityBuildRequest) packet);
    } else if (packet instanceof PacketReady) {
      return handleTurn(this);
    }
    return null;
  }

  @ServerOnly
  public PacketUnitCreate[] createStartingUnits(String playerId) {
    int numPlayers = players.size();
    int gridWidth = hexagonGrid.getWidth();
    int gridHeight = hexagonGrid.getHeight();
    int x = gridWidth / 2;
    int y = gridHeight / 2;
    switch (numPlayers) {
      case 1:
        x = 1;
        y = 1;
        break;
      case 2:
        x = gridWidth - 3;
        y = gridHeight - 3;
        break;
      case 3:
        x = gridWidth - 3;
        y = 1;
        break;
      case 4:
        x = 1;
        y = gridHeight - 3;
        break;
    }

    PacketUnitCreate[] packetUnitCreates = new PacketUnitCreate[]{
      new PacketUnitCreate(playerId, x, y, UnitType.SETTLER),
      new PacketUnitCreate(playerId, x, y, UnitType.WARRIOR)
    };
    for (PacketUnitCreate packetUnitCreate : packetUnitCreates) handlePacket(packetUnitCreate);
    return packetUnitCreates;
  }

  @ServerOnly
  public boolean allPlayersReady() {
    for (Player player : players) {
      if (!readyPlayers.getOrDefault(player.id, false)) {
        return false;
      }
    }
    return true;
  }

  private int getPlayerResource(Map<String, Integer> counts, String playerId) {
    return counts.getOrDefault(playerId, 0);
  }

  public int getPlayerGoldTotal(String playerId) {
    return getPlayerResource(playerGoldCounts, playerId);
  }

  public int getPlayerScienceTotal(String playerId) {
    return getPlayerResource(playerScienceCounts, playerId);
  }

  private void increasePlayerResourceBy(Map<String, Integer> counts, String playerId, int amount) {
    if(counts.containsKey(playerId)) {
      counts.put(playerId, counts.get(playerId) + amount);
    } else {
      counts.put(playerId, amount);
    }
    sendPlayerStats();
  }

  public void increasePlayerGoldBy(String playerId, int gold) {
    increasePlayerResourceBy(playerGoldCounts, playerId, gold);
  }

  public void increasePlayerScienceBy(String playerId, int science) {
    increasePlayerResourceBy(playerScienceCounts, playerId, science);
    //TODO: check tech unlock
  }

  public ArrayList<City> getPlayersCitiesById(String id) {
    return (ArrayList<City>) cities.stream()
      .filter(city -> city.player.id.equals(id))
      .collect(Collectors.toList());
  }
}
