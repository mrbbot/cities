package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.civilisation.geometry.HexagonConsumer;
import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.PlayerStats;
import com.mrbbot.civilisation.logic.interfaces.Mappable;
import com.mrbbot.civilisation.logic.interfaces.TurnHandler;
import com.mrbbot.civilisation.logic.interfaces.Unlockable;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Terrain;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.civilisation.logic.techs.Tech;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.net.ServerOnly;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Game implements Mappable, TurnHandler {
  private static final String VICTORY_REASON_DOMINATION = "conquering every single tile";
  private static final String VICTORY_REASON_SCIENCE = "blasting off into space";

  private String name;
  public HexagonGrid<Tile> hexagonGrid;
  public ArrayList<City> cities;
  public ArrayList<Unit> units;
  public ArrayList<Player> players;
  private Map<String, Integer> playerScienceCounts;
  private Map<String, Integer> playerGoldCounts;
  private Map<String, Set<Tech>> playerUnlockedTechs;
  private Map<String, Tech> playerUnlockingTechs;
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
  @ClientOnly
  private Consumer<PlayerTechDetails> techDetailsListener;
  @ClientOnly
  private BiConsumer<String, Boolean> messageListener;

  public Game(String name, MapSize mapSize) {
    this.name = name;

    //hexagonGrid = new HexagonGrid<>(40, 34, 1);
//    hexagonGrid = new HexagonGrid<>(20, 17, 1);
    hexagonGrid = new HexagonGrid<>(mapSize.width, mapSize.height, 1);
    //hexagonGrid = new HexagonGrid<>(100, 40, 1);
    //hexagonGrid = new HexagonGrid<>(1, 1, 1);

    hexagonGrid.forEach((_tile, hex, x, y) -> hexagonGrid.set(x, y, new Tile(hex, x, y)));
    cities = new ArrayList<>();
    units = new ArrayList<>();
    players = new ArrayList<>();

    playerScienceCounts = new HashMap<>();
    playerGoldCounts = new HashMap<>();
    playerUnlockedTechs = new HashMap<>();
    playerUnlockingTechs = new HashMap<>();
  }

  public Game(Map<String, Object> map) {
    this.name = (String) map.get("name");

    //noinspection unchecked
    this.players = (ArrayList<Player>) ((List<String>) map.get("players")).stream()
      .map(Player::new)
      .collect(Collectors.toList());

    //noinspection unchecked
    List<List<Double>> terrainList = (List<List<Double>>) map.get("terrain");
    //noinspection unchecked
    List<List<Integer>> treeList = (List<List<Integer>>) map.get("trees");
    int height = terrainList.size();
    for (int y = 0; y < height; y++) {
      List<Double> terrainRow = terrainList.get(y);
      List<Integer> treeRow = treeList.get(y);
      int width = terrainRow.size();

      if (hexagonGrid == null) {
        hexagonGrid = new HexagonGrid<>(width, height, 1);
      }

      for (int x = 0; x < width; x++) {
        double terrainHeight = terrainRow.get(x);
        boolean tree = treeRow.get(x) == 1;

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

    //noinspection unchecked
    Map<String, ArrayList<String>> unlockedTechs = (Map<String, ArrayList<String>>) map.get("unlockedTechs");
    this.playerUnlockedTechs = new HashMap<>();
    for (Map.Entry<String, ArrayList<String>> playerEntry : unlockedTechs.entrySet()) {
      ArrayList<Tech> list = (ArrayList<Tech>) playerEntry.getValue().stream()
        .map(Tech::fromName)
        .collect(Collectors.toList());
      this.playerUnlockedTechs.put(
        playerEntry.getKey(),
        new HashSet<>(list)
      );
    }
    //noinspection unchecked
    Map<String, String> unlockingTechs = (Map<String, String>) map.get("unlockingTechs");
    this.playerUnlockingTechs = new HashMap<>();
    for (Map.Entry<String, String> playerEntry : unlockingTechs.entrySet()) {
      this.playerUnlockingTechs.put(playerEntry.getKey(), Tech.fromName(playerEntry.getValue()));
    }
  }

  @ClientOnly
  public void setCurrentPlayer(String currentPlayerId, Consumer<PlayerStats> playerStatsListener) {
    this.currentPlayerId = currentPlayerId;
    this.playerStatsListener = playerStatsListener;
    sendPlayerStats();
  }

  @ClientOnly
  public void setTechDetailsListener(Consumer<PlayerTechDetails> techDetailsListener) {
    this.techDetailsListener = techDetailsListener;
    sendTechDetails();
  }

  @ClientOnly
  public void setMessageListener(BiConsumer<String, Boolean> messageListener) {
    this.messageListener = messageListener;
  }

  private void sendMessage(String message, boolean isError) {
    if (this.messageListener != null) {
      this.messageListener.accept(message, isError);
    }
  }

  private void sendMessageTo(String forPlayerId, String message, boolean isError) {
    if (this.messageListener != null && Objects.equals(forPlayerId, currentPlayerId)) {
      this.messageListener.accept(message, isError);
    }
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    map.put("name", name);

    List<String> playerList = players.stream()
      .map(player -> player.id)
      .collect(Collectors.toList());
    map.put("players", playerList);

    ArrayList<ArrayList<Double>> terrainList = new ArrayList<>();
    ArrayList<ArrayList<Integer>> treeList = new ArrayList<>();
    int gridWidth = hexagonGrid.getWidth() + 1;
    int gridHeight = hexagonGrid.getHeight();
    for (int y = 0; y < gridHeight; y++) {
      ArrayList<Double> terrainRow = new ArrayList<>();
      ArrayList<Integer> treeRow = new ArrayList<>();
      for (int x = 0; x < gridWidth - ((y + 1) % 2); x++) {
        Terrain terrain = hexagonGrid.get(x, y).getTerrain();
        terrainRow.add(terrain.height);
        treeRow.add(terrain.hasTree ? 1 : 0);
      }
      terrainList.add(terrainRow);
      treeList.add(treeRow);
    }
    map.put("terrain", terrainList);
    map.put("trees", treeList);

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

    Map<String, ArrayList<String>> unlockedTechsMap = new HashMap<>();
    for (Map.Entry<String, Set<Tech>> playerEntry : playerUnlockedTechs.entrySet()) {
      unlockedTechsMap.put(
        playerEntry.getKey(),
        (ArrayList<String>) playerEntry.getValue().stream().map(Tech::getName).collect(Collectors.toList())
      );
    }
    map.put("unlockedTechs", unlockedTechsMap);

    Map<String, String> unlockingTechsMap = new HashMap<>();
    for (Map.Entry<String, Tech> playerEntry : playerUnlockingTechs.entrySet()) {
      if (playerEntry.getValue() != null) {
        unlockingTechsMap.put(
          playerEntry.getKey(),
          playerEntry.getValue().getName()
        );
      }
    }
    map.put("unlockingTechs", unlockingTechsMap);

    return map;
  }

  private void win(String playerId, String reason) {
    boolean victory = currentPlayerId == null || playerId.equals(currentPlayerId);
    String messageStart = victory ? "Victory!" : "Defeat!";
    String playerPart = playerId.equals(currentPlayerId) ? "You've" : String.format("%s has", playerId);
    sendMessage(String.format("%s %s won the game by %s!", messageStart, playerPart, reason), !victory);
  }

  private void checkDominationVictory() {
    String playerId = null;

    Iterator<Tile> tileIterator = hexagonGrid.iterator();
    while (tileIterator.hasNext()) {
      Tile tile = tileIterator.next();
      if (tile.city == null) return;
      if (playerId == null) playerId = tile.city.player.id;
      if (!playerId.equals(tile.city.player.id)) return;
    }

    if (playerId != null) {
      win(playerId, VICTORY_REASON_DOMINATION);
    }
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
    while ((tileToCheck = placementTilesToCheck.poll()) != null) {
      boolean tileIsCapital = false;
      if (tileToCheck.city != null) {
        tileIsCapital = tileToCheck.city.getCenter().samePositionAs(tileToCheck);
      }
      if (!tileIsCapital && tileToCheck.unit == null && tileToCheck.canTraverse()) {
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

    if (tileToCheck != null) {
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

  private Tile[] handleUnitDamagePacket(PacketDamage packet) {
    Tile attackerTile = hexagonGrid.get(packet.attackerX, packet.attackerY);
    Tile targetTile = hexagonGrid.get(packet.targetX, packet.targetY);

    boolean targetIsCity = targetTile.city != null && targetTile.city.getCenter().samePositionAs(targetTile);

    Unit attacker = attackerTile.unit;
    //prioritise unit
    Living target = targetTile.unit == null ? (targetIsCity ? targetTile.city : null) : targetTile.unit;

    if (attacker == null || !attacker.canAttack()) return null;
    if (target == null) {
      sendMessageTo(attacker.player.id, "You can't attack nothing!", true);
      return null;
    }
    if (attacker.getOwner().equals(target.getOwner())) {
      sendMessageTo(attacker.player.id, "You can't attack yourself!", true);
      return null;
    }
    if (attacker.hasAttackedThisTurn) {
      sendMessageTo(attacker.player.id, "You've already attacked this turn!", true);
      return null;
    }

    Point2D targetPos = target.getPosition();
    Point2D attackerPos = attacker.getPosition();
    double distanceBetween = targetPos.distance(attackerPos);
    int tilesBetween = (int) Math.round(distanceBetween / Hexagon.SQRT_3);
    if (attacker.hasAbility(UnitAbility.ABILITY_ATTACK) && tilesBetween <= 1) {
      target.onAttack(attacker, false);
      attacker.hasAttackedThisTurn = true;
    }
    if (attacker.hasAbility(UnitAbility.ABILITY_RANGED_ATTACK) && tilesBetween <= 2) {
      target.onAttack(attacker, true);
      attacker.hasAttackedThisTurn = true;
    }

    if (target instanceof City && target.isDead()) {
      City targetCity = (City) target;
      targetCity.setHealth(10);
      targetCity.setOwner(targetCity.lastAttacker.player);
      checkDominationVictory();
      return new Tile[]{};
    }

    return new Tile[]{attackerTile, targetTile};
  }

  private Tile[] handleCityCreatePacket(PacketCityCreate packet) {
    Player player = new Player(packet.id);
    cities.add(new City(hexagonGrid, packet.x, packet.y, player));
    sendPlayerStats();
    return new Tile[]{};
  }

  private Tile[] handleCityGrowPacket(PacketCityGrow packet) {
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

  private Tile[] handleCityRenamePacket(PacketCityRename packet) {
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.city != null) {
      t.city.name = packet.newName;
    }
    return null;
  }

  private Tile[] handleCityBuildRequestPacket(PacketCityBuildRequest packet) {
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.city != null) {
      CityBuildable buildable = packet.getBuildable();
      if (packet.withProduction) {
        t.city.currentlyBuilding = buildable;
      } else if (buildable.canBuildWithGold(getPlayerGoldTotal(t.city.player.id))) {
        increasePlayerGoldBy(t.city.player.id, -buildable.getGoldCost());
        Tile placed = buildable.build(t.city, this);
        if (placed != null) return new Tile[]{placed};
      }

    }
    return null;
  }

  private Tile[] handleWorkerImproveRequestPacket(PacketWorkerImproveRequest packet) {
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.unit != null) {
      t.unit.startWorkerBuilding(packet.getImprovement());
      return new Tile[]{t};
    }
    return null;
  }

  private Tile[] handlePlayerResearchRequestPacket(PacketPlayerResearchRequest packet) {
    playerUnlockingTechs.put(packet.playerId, packet.getTech());
    sendTechDetails();
    return null;
  }

  private Tile[] handleUnitUpgradePacket(PacketUnitUpgrade packet) {
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.unit != null && t.unit.unitType.getUpgrade() != null) {
      t.unit.unitType = t.unit.unitType.getUpgrade();
      return new Tile[]{t};
    }
    return null;
  }

  private Tile[] handlePurchaseTileRequestPacket(PacketPurchaseTileRequest packet) {
    Tile t = hexagonGrid.get(packet.cityX, packet.cityY);
    if (t.city != null) {
      String playerId = t.city.player.id;

      Tile purchaseTile = hexagonGrid.get(packet.purchaseX, packet.purchaseY);

      // already part of city
      if (purchaseTile.city != null) {
        sendMessageTo(playerId, "This tile is already part of a city!", true);
        return null;
      }

      ArrayList<Tile> neighbours = hexagonGrid.getNeighbours(packet.purchaseX, packet.purchaseY, false);
      boolean isNeighbour = neighbours.stream().anyMatch(tile -> tile.city != null && tile.city.sameCenterAs(t.city));
      // not neighbouring tile in city
      if (!isNeighbour) {
        sendMessageTo(playerId, "This tile isn't adjacent to this city!", true);
        return null;
      }

      // calculate cost
      double distanceBetween = t.getHexagon().getCenter().distance(purchaseTile.getHexagon().getCenter());
      int tilesBetween = (int) Math.round(distanceBetween / Hexagon.SQRT_3);
      double cost = 50.0 * tilesBetween;
      for (Building building : t.city.buildings) {
        cost *= building.expansionCostMultiplier;
      }
      int goldTotal = getPlayerGoldTotal(t.city.player.id);
      int roundCost = (int) Math.round(cost);
      // not enough money
      if (goldTotal < roundCost) {
        sendMessageTo(playerId, String.format("This tile costs %d gold to purchase, you only have %d!", roundCost, goldTotal), true);
        return null;
      }

      increasePlayerGoldBy(playerId, -roundCost);
      ArrayList<Point2D> grownTo = new ArrayList<>();
      grownTo.add(new Point2D(purchaseTile.x, purchaseTile.y));
      t.city.growTo(grownTo);
      return new Tile[]{};
    }
    return null;
  }

  @Override
  public Tile[] handleTurn(Game game) {
    ArrayList<Tile> updatedTiles = new ArrayList<>();

    for (Unit unit : units) {
      Tile[] unitUpdatedTiles = unit.handleTurn(this);
      if (unitUpdatedTiles != null) Collections.addAll(updatedTiles, unitUpdatedTiles);
    }

    for (City city : cities) {
      Collections.addAll(updatedTiles, city.handleTurn(this));
      if (city.naturalHeal()) {
        updatedTiles.add(city.getCenter());
      }
    }

    waitingForPlayers = false;
    readyPlayers.clear();

    checkDominationVictory();

    sendPlayerStats();
    sendTechDetails();

    return updatedTiles.toArray(new Tile[]{});
  }

  @ClientOnly
  private void sendPlayerStats() {
    if (currentPlayerId != null && playerStatsListener != null) {
      int totalSciencePerTurn = 0;
      int totalGoldPerTurn = 0;
      for (City city : cities) {
        if (city.player.id.equals(currentPlayerId)) {
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

  @ClientOnly
  private void sendTechDetails() {
    if (currentPlayerId != null && techDetailsListener != null) {
      techDetailsListener.accept(new PlayerTechDetails(
        getPlayerUnlockedTechs(currentPlayerId),
        getPlayerUnlockingTech(currentPlayerId),
        getPlayerUnlockingProgress(currentPlayerId)
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
      return handleCityCreatePacket((PacketCityCreate) packet);
    } else if (packet instanceof PacketCityGrow) {
      return handleCityGrowPacket((PacketCityGrow) packet);
    } else if (packet instanceof PacketUnitCreate) {
      return handleUnitCreatePacket((PacketUnitCreate) packet);
    } else if (packet instanceof PacketUnitMove) {
      return handleUnitMovePacket((PacketUnitMove) packet);
    } else if (packet instanceof PacketUnitDelete) {
      return handleUnitDeletePacket((PacketUnitDelete) packet);
    } else if (packet instanceof PacketDamage) {
      return handleUnitDamagePacket((PacketDamage) packet);
    } else if (packet instanceof PacketCityRename) {
      return handleCityRenamePacket((PacketCityRename) packet);
    } else if (packet instanceof PacketCityBuildRequest) {
      return handleCityBuildRequestPacket((PacketCityBuildRequest) packet);
    } else if (packet instanceof PacketWorkerImproveRequest) {
      return handleWorkerImproveRequestPacket((PacketWorkerImproveRequest) packet);
    } else if (packet instanceof PacketPlayerResearchRequest) {
      return handlePlayerResearchRequestPacket((PacketPlayerResearchRequest) packet);
    } else if (packet instanceof PacketUnitUpgrade) {
      return handleUnitUpgradePacket((PacketUnitUpgrade) packet);
    } else if (packet instanceof PacketPurchaseTileRequest) {
      return handlePurchaseTileRequestPacket((PacketPurchaseTileRequest) packet);
    } else if (packet instanceof PacketBlastOff) {
      win(((PacketBlastOff) packet).playerId, VICTORY_REASON_SCIENCE);
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

  public Set<Tech> getPlayerUnlockedTechs(String playerId) {
    return playerUnlockedTechs.getOrDefault(playerId, new HashSet<>());
  }

  public Tech getPlayerUnlockingTech(String playerId) {
    return playerUnlockingTechs.get(playerId);
  }

  public double getPlayerUnlockingProgress(String playerId) {
    Tech unlockingTech = getPlayerUnlockingTech(playerId);
    if (unlockingTech == null) return 0;
    int scienceTotal = getPlayerScienceTotal(playerId);
    int scienceCost = unlockingTech.getScienceCost();
    if (scienceCost == 0) return 0;
    return Math.min((double) scienceTotal / (double) scienceCost, 1);
  }

  public boolean playerHasUnlocked(String playerId, Unlockable unlockable) {
    int unlockId = unlockable.getUnlockId();
    if (unlockId == 0x00) return true;
    Set<Tech> unlockedTechs = getPlayerUnlockedTechs(playerId);
    for (Tech unlockedTech : unlockedTechs) {
      for (Unlockable unlock : unlockedTech.getUnlocks()) {
        if (unlock.getUnlockId() == unlockId) return true;
      }
    }
    return false;
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
    if (counts.containsKey(playerId)) {
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

    Set<Tech> unlockedTechs = getPlayerUnlockedTechs(playerId);
    Tech unlockingTech = getPlayerUnlockingTech(playerId);
    double progress = getPlayerUnlockingProgress(playerId);

    if (unlockingTech != null && progress >= 1 && !unlockedTechs.contains(unlockingTech)) {
      playerUnlockedTechs.putIfAbsent(playerId, new HashSet<>());
      playerUnlockedTechs.get(playerId).add(unlockingTech);
      playerUnlockingTechs.put(playerId, null);
      increasePlayerResourceBy(playerScienceCounts, playerId, -unlockingTech.getScienceCost());
      sendMessageTo(playerId, String.format("%s has been unlocked!", unlockingTech.getName()), false);
    }
  }

  public ArrayList<City> getPlayersCitiesById(String id) {
    return (ArrayList<City>) cities.stream()
      .filter(city -> city.player.id.equals(id))
      .collect(Collectors.toList());
  }
}
