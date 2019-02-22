package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.ArrayList;
import java.util.Map;

@ClientOnly
public class RenderImprovement extends RenderData<Improvement> {
  private final Tile tile;

  public RenderImprovement(Tile tile, ArrayList<Tile> adjacentTiles) {
    super(tile.improvement);
    this.tile = tile;
    setImprovement(data, tile.improvementMetadata, adjacentTiles);
  }

  public void setImprovement(Improvement data, Map<String, Object> metadata, ArrayList<Tile> adjacentTiles) {
    this.data = data;
    this.getChildren().clear();
    this.reset();

    if (Improvement.CAPITAL.equals(data)) {
      add(new RenderImprovementRoad(tile, adjacentTiles));
      add(new RenderImprovementHouse(tile.city.wallColour));
    } else if (Improvement.FARM.equals(data)) {
      add(new RenderImprovementFarm(metadata));
    } else if (Improvement.TREE.equals(data)) {
      add(new RenderImprovementTree());
    } else if (Improvement.MINE.equals(data)) {
      add(new RenderImprovementMine(metadata));
    } else if (Improvement.PASTURE.equals(data)) {
      add(new RenderImprovementPasture());
    } else if (Improvement.ROAD.equals(data)) {
      add(new RenderImprovementRoad(tile, adjacentTiles));
    }
  }
}
