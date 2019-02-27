package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.render.map.improvement.RenderImprovement;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import com.mrbbot.generic.render.RenderData;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Translate;

import java.util.ArrayList;

/**
 * Render object for a hexagonal tile in the hexagon grid. Handles rendering
 * terrain, improvements, units, selections and city walls.
 */
@ClientOnly
public class RenderTile extends RenderData<Tile> {
  /**
   * Colour to render the hexagonal prism for this tile
   */
  private Color colour;
  /**
   * Render object translated so that z=0 is on the surface of the tile. Holds
   * improvements and units.
   */
  private Render aboveGround;
  /**
   * Render object for the tile's improvement. May not contain any children
   * if the tile doesn't have an improvement.
   */
  private RenderImprovement improvement;
  /**
   * Render object for the unit selection overlay. Also handles rendering of
   * city boundary walls, and pathfinding routes.
   */
  private RenderTileOverlay overlay;
  /**
   * Render object for the unit occupying this tile. Hidden if the tile doesn't
   * currently contain a unit.
   */
  private RenderUnit unit;
  /**
   * Render object for a capital cities health bar. Null by default and only
   * created if there's a capital city on the tile.
   */
  private RenderHealthBar cityHealthBar;
  /**
   * Height of the hexagonal prism used to represent this tile.
   */
  private double height;
  /**
   * Tiles that are adjacent to this tile on the map.
   */
  private final ArrayList<Tile> adjacentTiles;

  /**
   * Constructor for a new render object. This will be used as long as the
   * game is open and isn't destroyed/recreated when a tile update occurs.
   *
   * @param data          tile this render represents
   * @param adjacentTiles tiles adjacent to the tile this render represents
   */
  RenderTile(Tile data, ArrayList<Tile> adjacentTiles) {
    super(data);
    this.adjacentTiles = adjacentTiles;

    // Translate this render to the appropriate position on the hex grid
    Point2D center = data.getHexagon().getCenter();
    translateTo(center.getX(), center.getY(), 0);

    // Calculate the colour for the terrain
    colour = data.getTerrain().getColour();

    // Add a hexagonal prism representing the terrain
    height = data.getHeight();
    Shape3D ground = data.getHexagon().getPrism(height);
    ground.getTransforms().add(new Translate(0, height / 2, 0));
    ground.setMaterial(new PhongMaterial(colour));
    add(ground);

    // Create the render object linked to the top of the terrain
    aboveGround = new Render();
    aboveGround.translateTo(0, 0, height);
    add(aboveGround);

    // Create the improvement render object
    improvement = new RenderImprovement(data, adjacentTiles);
    aboveGround.add(improvement);

    // Create the overlay render object for unit selection/city walls/
    // pathfinding
    overlay = new RenderTileOverlay(Color.WHITE);

    // Create the unit render object
    unit = new RenderUnit(data.unit);
    unit.setVisible(data.unit != null);
    aboveGround.add(overlay, unit);

    // Update the details of all the render objects so they reflect the tile
    // data
    updateRender();
  }

  void updateRender() {
    // Set the overlay colour depending on whether this tile is selected and
    // traversable by units
    overlay.setColor(
      data.selected
        ? Color.LIGHTBLUE
        : (data.canTraverse() ? Color.WHITE : Color.INDIANRED)
    );

    // Check if there's a city on this tile
    if (data.city != null) {
      // Update the city walls
      overlay.setCityWalls(data.city, data.getCityWalls(), height);

      // If this is a capital city, and the health bar hasn't been added yet,
      // add it
      if (data.city.getCenter().samePositionAs(data)) {
        if (cityHealthBar == null) {
          aboveGround.add(
            cityHealthBar = new RenderHealthBar(data.city, true)
          );
        }
        // Update the health bar render regardless of whether is was created
        // now
        cityHealthBar.updateRender(data.city);
      }
    }

    // Mark the overlay as selected if it is
    overlay.setSelected(data.selected);

    // Update the unit render object hiding it if there isn't a unit present
    // or changing its colours if there is one
    unit.updateRender(data.unit);
    unit.setVisible(data.unit != null);

    // Update the improvement render object
    improvement.setImprovement(
      data.improvement,
      data.improvementMetadata,
      adjacentTiles
    );
  }

  /**
   * Marks an overlay as visible. Used when a path dragged out by the user
   * covers this tile.
   *
   * @param visible whether the overlay should always be visible regardless of
   *                its selection state
   */
  void setOverlayVisible(boolean visible) {
    overlay.setOverlayVisible(visible);
  }

  /**
   * Checks if the colour of this tile is translucent. Translucent objects must
   * be added to the render tree last for the translucency effects to work
   *
   * @return whether the tiles terrain is translucent
   */
  boolean isTranslucent() {
    return colour.getOpacity() < 1;
  }
}
