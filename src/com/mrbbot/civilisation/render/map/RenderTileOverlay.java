package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;

@ClientOnly
class RenderTileOverlay extends Render {
  private RenderTileOverlayPart[] parts;
  private City city;
  private boolean[] cityWalls;
  private Color color;
  private boolean visible;
  private boolean selected;

  RenderTileOverlay(Color color) {
    this.color = color;
    parts = new RenderTileOverlayPart[6];
    city = null;
    cityWalls = new boolean[]{false, false, false, false, false, false};
    selected = false;
    for (int i = 0; i < 6; i++) {
      double angle = 30 + (60 * i);

      RenderTileOverlayPart part = new RenderTileOverlayPart(angle, color);
      parts[i] = part;
      add(part);
    }
  }

  private void updateVisibilities() {
    for (int i = 0; i < parts.length; i++) {
      parts[i].setWallVisible(visible || selected || cityWalls[i]);
      parts[i].setJoinVisible(cityWalls[i]);
    }
  }

  void setOverlayVisible(boolean visible) {
    this.visible = visible;
    updateVisibilities();
  }

  void setSelected(boolean selected) {
    this.selected = selected;
    updateVisibilities();
  }

  void setColor(Color color) {
    this.color = color;
    Color wallColour = city == null ? color : city.wallColour;
    Color joinColour = city == null ? color : city.joinColour;
    for (int i = 0; i < parts.length; i++) {
      boolean walled = this.cityWalls[i];
      RenderTileOverlayPart part = parts[i];
      part.setWallColour(walled ? wallColour : color);
      part.setJoinColour(walled ? joinColour : color);
    }
  }

  void setCityWalls(City city, boolean[] walls, double tileHeight) {
    this.city = city;
    for (int i = 0; i < parts.length; i++) {
      boolean oldWalled = this.cityWalls[i];
      boolean walled = walls[i];
      double wallHeight = city.greatestTileHeight + 0.2 - tileHeight;
      RenderTileOverlayPart part = parts[i];

      part.setWallColour(walled ? city.wallColour : color);
      part.setJoinColour(walled ? city.joinColour : color);

      part.setWallVisible((part.isWallVisible() && !oldWalled) || walled);
      part.setJoinVisible(walled);

      double targetHeight = walled ? wallHeight : 0.1;
      part.setWallHeight(targetHeight, tileHeight, city.greatestTileHeight);
    }
    this.cityWalls = walls;
  }
}
