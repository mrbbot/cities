package com.mrbbot.civilisation.render.map;

import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

class RenderTileOverlay extends Render {
  private Box[] parts;
  private boolean[] cityWalls;
  private final Color color;

  RenderTileOverlay(Color color) {
    this.color = color;
    parts = new Box[6];
    cityWalls = new boolean[]{false, false, false, false, false, false};
    for (int i = 0; i < 6; i++) {
      add(makeOverlayHexagonPart(color, i,30 + (60 * i)));
    }
  }

  void setOverlayVisible(boolean visible) {
    for (int i = 0; i < parts.length; i++) {
      parts[i].setVisible(visible || cityWalls[i]);
    }
  }

  void setCityWalls(boolean[] walls) {
    this.cityWalls = walls;
    for (int i = 0; i < parts.length; i++) {
      parts[i].setMaterial(new PhongMaterial(walls[i] ? Color.YELLOW : color));
      parts[i].setVisible(parts[i].isVisible() || walls[i]);
    }
  }

  private Render makeOverlayHexagonPart(Color color, int i, double angle) {
    Render render = new Render();
    render.rotateZ.setAngle(angle);

    Box overlayPart = new Box(1, 0.2, 0.1);
    overlayPart.setMaterial(new PhongMaterial(color));
    overlayPart.setTranslateY(0.77);
    overlayPart.setTranslateZ(0.05);
    overlayPart.setVisible(false);
    render.add(overlayPart);

    parts[i] = overlayPart;

    return render;
  }
}
