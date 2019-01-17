package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;

class RenderTileOverlay extends Render {
  private RenderTileOverlayPart[] parts;
  private boolean[] cityWalls;
  private final Color color;

  RenderTileOverlay(Color color) {
    this.color = color;
    parts = new RenderTileOverlayPart[6];
    cityWalls = new boolean[]{false, false, false, false, false, false};
    for (int i = 0; i < 6; i++) {
      double angle = 30 + (60 * i);

      RenderTileOverlayPart part = new RenderTileOverlayPart(angle, color);
      parts[i] = part;
      add(part);
    }
  }

  void setOverlayVisible(boolean visible) {
    for (int i = 0; i < parts.length; i++) {
      parts[i].setWallVisible(visible || cityWalls[i]);
      parts[i].setJoinVisible(cityWalls[i]);
    }
  }

  void setCityWalls(City city, boolean[] walls, double tileHeight) {
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
