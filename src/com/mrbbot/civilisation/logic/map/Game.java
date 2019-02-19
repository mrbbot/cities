package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.interfaces.Mappable;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.net.ServerOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Game implements Mappable {
  private String name;
  public HexagonGrid<Tile> hexagonGrid;
  public ArrayList<City> cities;
  public ArrayList<Unit> units;
  public ArrayList<Player> players;
  @ServerOnly
  public Map<String, Boolean> readyPlayers = new HashMap<>();
  @ClientOnly
  public Unit selectedUnit = null;
  @ClientOnly
  public boolean waitingForPlayers = false;

  public Game(String name) {
    this.name = name;
//    hexagonGrid = new HexagonGrid<>(40, 34, 1);
    hexagonGrid = new HexagonGrid<>(20, 17, 1);
//    hexagonGrid = new HexagonGrid<>(100, 40, 1);
//    hexagonGrid = new HexagonGrid<>(1, 1, 1);
    hexagonGrid.forEach((_tile, hex, x, y) -> hexagonGrid.set(x, y, new Tile(hex, x, y)));

    cities = new ArrayList<>();

//    City middleCity = new City(hexagonGrid, 10, 10, Color.RED);
//    middleCity.grow(2);
//    cities.add(middleCity);

//    cities.add(new City(hexagonGrid, 5, 5, Color.GOLDENROD));
//    cities.add(new City(hexagonGrid, 17, 9, Color.DODGERBLUE));
//    cities.add(new City(hexagonGrid, 10, 0, Color.PURPLE));

//    cities.add(new City(hexagonGrid, 5, 5, new Player("hi")));
//    cities.add(new City(hexagonGrid, 7, 5, new Player("hi")));

    units = new ArrayList<>();

    /*units.add(new Unit(hexagonGrid.get(10, 8), UnitType.ARCHER));
    units.add(new Unit(hexagonGrid.get(4, 4)));
    units.add(new Unit(hexagonGrid.get(12, 9)));*/

    players = new ArrayList<>();
    //players.add(new Player("hi"));
  }

  public Game(Map<String, Object> map) {
    this.name = (String) map.get("name");

    //noinspection unchecked
    this.players = (ArrayList<Player>) ((List<String>) map.get("players")).stream()
      .map(Player::new)
      .collect(Collectors.toList());

    //noinspection unchecked
    List<List<Double>> terrainList = (List<List<Double>>) map.get("terrain");
    int height = terrainList.size();
    for (int y = 0; y < height; y++) {
      List<Double> row = terrainList.get(y);
      int width = row.size();

      if (hexagonGrid == null) {
        hexagonGrid = new HexagonGrid<>(width, height, 1);
      }

      for (int x = 0; x < width; x++) {
        double terrainHeight = row.get(x);
        hexagonGrid.set(x, y, new Tile(hexagonGrid.getHexagon(x, y), x, y, terrainHeight));
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
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    map.put("name", name);

    List<String> playerList = players.stream()
      .map(player -> player.id)
      .collect(Collectors.toList());
    map.put("players", playerList);

    ArrayList<ArrayList<Double>> terrainList = new ArrayList<>();
    int gridWidth = hexagonGrid.getWidth() + 1;
    int gridHeight = hexagonGrid.getHeight();
    for (int y = 0; y < gridHeight; y++) {
      ArrayList<Double> row = new ArrayList<>();
      for (int x = 0; x < gridWidth - ((y + 1) % 2); x++) {
        hexagonGrid.get(x, y);
        row.add(hexagonGrid.get(x, y).getTerrain().height);
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

    return map;
  }

  public boolean containsPlayerWithId(String id) {
    for (Player player : players) {
      if (player.id.equals(id)) return true;
    }
    return false;
  }

  private Tile[] handleUnitCreatePacket(PacketUnitCreate packet) {
    Tile tile = hexagonGrid.get(packet.x, packet.y);

    Player player = new Player(packet.id);
    Unit unit = new Unit(player, tile, packet.unitType);
    units.add(unit);

    return new Tile[]{tile};
  }

  private Tile[] handleUnitMovePacket(PacketUnitMove packet) {
    Tile startTile = hexagonGrid.get(packet.startX, packet.startY);
    Tile endTile = hexagonGrid.get(packet.endX, packet.endY);

    startTile.unit.remainingMovementPoints -= packet.usedMovementPoints;
    assert startTile.unit.remainingMovementPoints >= 0;
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

  private Tile[] handleCityCreate(PacketCityCreate packet) {
    Player player = new Player(packet.id);
    cities.add(new City(hexagonGrid, packet.x, packet.y, player));
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
    return new Tile[]{};
  }

  private void handlePacketReady(PacketReady packet) {
    if(!packet.ready) {
      for (Unit unit : units) {
        unit.remainingMovementPoints = unit.unitType.movementPoints;
      }
      waitingForPlayers = false;
    }
    readyPlayers.clear();
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
    } else if(packet instanceof PacketReady) {
      handlePacketReady((PacketReady) packet);
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
      new PacketUnitCreate(playerId, x + 1, y, UnitType.WARRIOR)
    };
    for (PacketUnitCreate packetUnitCreate : packetUnitCreates) handlePacket(packetUnitCreate);
    return packetUnitCreates;
  }

  @ServerOnly
  public boolean allPlayersReady() {
    for (Player player : players) {
      if(!readyPlayers.getOrDefault(player.id, false)) {
        return false;
      }
    }
    return true;
  }


}
