package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;

import java.util.ArrayList;

public class Map {
  public HexagonGrid<Tile> hexagonGrid;
  public ArrayList<City> cities;

  public Map() {
    hexagonGrid = new HexagonGrid<>(20, 17, 1);
//    hexagonGrid = new HexagonGrid<>(1, 1, 1);
    hexagonGrid.forEach((_tile, hex, x, y) -> hexagonGrid.set(x, y, new Tile(hex, x, y)));

    cities = new ArrayList<>();
    cities.add(new City(hexagonGrid, 10, 10));
  }
}
