package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;

import java.io.Serializable;
import java.util.ArrayList;

public class Map implements Serializable {
  public HexagonGrid<Tile> hexagonGrid;
  public ArrayList<City> cities;
  public ArrayList<Unit> units;

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

    units = new ArrayList<>();

    units.add(new Unit(hexagonGrid.get(10, 8)));
    units.add(new Unit(hexagonGrid.get(4, 4)));
    units.add(new Unit(hexagonGrid.get(12, 9)));
  }
}
