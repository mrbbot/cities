package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;

/**
 * Render object representing a tile overlay. Used for highlighting the
 * selected path, and a city's boundaries.
 */
@ClientOnly
class RenderTileOverlay extends Render {
  /**
   * Array of the 6 different parts of the overlay (one for each hexagonal
   * edge)
   */
  private RenderTileOverlayPart[] parts;
  /**
   * The city this overlay covers (may be null)
   */
  private City city;
  /**
   * Array containing information about whether adjacent tiles are part of the
   * same city.
   * <p>
   * [top left, left, bottom left, bottom right, right, top right]
   */
  private boolean[] cityWalls;
  /**
   * Color to render the overlay (depends on unit selection and traversability)
   */
  private Color color;
  /**
   * Whether the overlay should be visible. City walls are always visible.
   */
  private boolean visible;
  /**
   * Whether the tile this overlay represents has been selected
   */
  private boolean selected;

  /**
   * Constructor for a new tile overlay
   *
   * @param color starting colour for the overlay
   */
  RenderTileOverlay(Color color) {
    this.color = color;
    // Create an array to hold to parts
    parts = new RenderTileOverlayPart[6];

    // Set default values
    city = null;
    cityWalls = new boolean[]{false, false, false, false, false, false};
    selected = false;

    // Create the part for each hexagonal edge
    for (int i = 0; i < 6; i++) {
      double angle = 30 + (60 * i);

      RenderTileOverlayPart part = new RenderTileOverlayPart(angle, color);
      parts[i] = part;
      add(part);
    }
  }

  /**
   * Recalculates the visibility of each part from whether or not the overlay's
   * been marked as visible, it's selected, or there are city walls.
   */
  private void updateVisibilities() {
    for (int i = 0; i < parts.length; i++) {
      parts[i].setWallVisible(visible || selected || cityWalls[i]);
      // Joins should only be visible for city walls
      parts[i].setJoinVisible(cityWalls[i]);
    }
  }

  /**
   * Sets the overlay's visibility for pathfinding
   *
   * @param visible whether all overlay parts should be visible
   */
  void setOverlayVisible(boolean visible) {
    this.visible = visible;
    // Recalculate visibilities
    updateVisibilities();
  }

  /**
   * Sets the overlay's visibility for unit selection
   *
   * @param selected whether all overlay parts should be visible
   */
  void setSelected(boolean selected) {
    this.selected = selected;
    // Recalculate visibilities
    updateVisibilities();
  }

  /**
   * Sets the colour of the overlay if it's been selected or it's part of the
   * path
   *
   * @param color colour of the overlay
   */
  void setColor(Color color) {
    this.color = color;
    Color wallColour = city == null ? color : city.wallColour;
    Color joinColour = city == null ? color : city.joinColour;

    // Set the colour for each of the overlay parts
    for (int i = 0; i < parts.length; i++) {
      boolean walled = this.cityWalls[i];
      RenderTileOverlayPart part = parts[i];
      part.setWallColour(walled ? wallColour : color);
      part.setJoinColour(walled ? joinColour : color);
    }
  }

  /**
   * Sets this overlay's city walls containing information about whether
   * adjacent tiles are part of the same city.
   *
   * @param city       city the overlay is part of
   * @param walls      array containing wall information.
   *                   [top left, left, bottom left,
   *                   bottom right, right, top right]
   * @param tileHeight height of the tile this overlay covers
   */
  void setCityWalls(City city, boolean[] walls, double tileHeight) {
    this.city = city;
    for (int i = 0; i < parts.length; i++) {
      boolean walled = walls[i];
      double wallHeight = city.greatestTileHeight + 0.2 - tileHeight;
      RenderTileOverlayPart part = parts[i];

      // Update wall state for each part
      part.setWallColour(walled ? city.wallColour : color);
      part.setJoinColour(walled ? city.joinColour : color);

      part.setWallVisible(walled);
      part.setJoinVisible(walled);

      // Set the height of the part to be different depending on whether this
      // is just a selection/path route
      double targetHeight = walled ? wallHeight : 0.1;
      part.setWallHeight(targetHeight, tileHeight, city.greatestTileHeight);
    }
    this.cityWalls = walls;
  }
}
