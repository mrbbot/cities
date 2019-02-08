package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;

import java.io.Serializable;
import java.util.ArrayList;

public class Map implements Serializable {
  public HexagonGrid<Tile> hexagonGrid;
  public ArrayList<City> cities;
  public ArrayList<Unit> units;
  public ArrayList<Player> players;
  public Unit selectedUnit = null;

  public Map() {
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

    //cities.add(new City(hexagonGrid, 5, 5, new Player("hi")));

    units = new ArrayList<>();

    /*units.add(new Unit(hexagonGrid.get(10, 8), UnitType.ARCHER));
    units.add(new Unit(hexagonGrid.get(4, 4)));
    units.add(new Unit(hexagonGrid.get(12, 9)));*/

    players = new ArrayList<>();
  }

  public Player playerById(String id) {
    for (Player player : players) {
      if(player.id.equals(id)) {
        return player;
      }
    }
    return null;
  }

  public java.util.Map<String, Object> toMap() {
    java.util.Map<String, Object> root = new java.util.HashMap<>();

    ArrayList<ArrayList<Double>> terrain = new ArrayList<>();
    int gridWidth = hexagonGrid.getWidth() + 1;
    int gridHeight = hexagonGrid.getHeight();
    for (int y = 0; y < gridHeight; y++) {
      ArrayList<Double> row = new ArrayList<>();
      for (int x = 0; x < gridWidth - ((y + 1) % 2); x++) {
        hexagonGrid.get(x, y);
        row.add(hexagonGrid.get(x, y).getTerrain().height);
      }
      terrain.add(row);
    }
    root.put("terrain", terrain);

    ArrayList<java.util.Map<String, Object>> cities = new ArrayList<>();
    for (City city : this.cities) {
      java.util.Map<String, Object> cityObject = new java.util.HashMap<>();

      ArrayList<java.util.Map<String, Integer>> cityTiles = new ArrayList<>();
      for (Tile cityTile : city.tiles) {
        java.util.Map<String, Integer> point = new java.util.HashMap<>();
        point.put("x", cityTile.x);
        point.put("y", cityTile.y);
        cityTiles.add(point);
      }
      cityObject.put("tiles", cityTiles);

      cityObject.put("health", city.health);
      cityObject.put("owner", city.player.id);
      cities.add(cityObject);
    }
    root.put("cities", cities);

    return root;
  }
}
