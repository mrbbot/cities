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

/**
 * Render object for a tile's improvement. All tiles have this object, but it
 * only contains other renders if the improvement isn't equal to
 * {@link Improvement#NONE}.
 */
@ClientOnly
public class RenderImprovement extends RenderData<Improvement> {
  /**
   * Tile the improvement is situated on
   */
  private final Tile tile;

  /**
   * Creates a new render object for the specified tile's improvement
   *
   * @param tile          tile the improvement is situated on
   * @param adjacentTiles adjacent tiles to this tile
   */
  public RenderImprovement(Tile tile, ArrayList<Tile> adjacentTiles) {
    super(tile.improvement);
    this.tile = tile;
    // Set the render's initial state
    setImprovement(data, tile.improvementMetadata, adjacentTiles);
  }

  /**
   * Sets the improvement details for this render, updating what it's showing
   *
   * @param data          new improvement of the tile
   * @param metadata      metadata of the improvement, angle, size, etc
   * @param adjacentTiles adjacent tiles to the improvement's tile
   */
  public void setImprovement(
    Improvement data,
    Map<String, Object> metadata,
    ArrayList<Tile> adjacentTiles
  ) {
    this.data = data;
    // Remove all the previous children renders for the old improvement
    this.getChildren().clear();
    // Reset transformations to their default values
    this.reset();

    // Add the correct render objects for the new improvement
    if (Improvement.CAPITAL.equals(data)) {
      // Capitals automatically have a road, so add it underneath the capital
      // render
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
