package com.mrbbot.civilisation.render.map;

import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;

class RenderTileOverlay extends Render {
  private RenderTileOverlayPart[] parts;
  private boolean[] cityWalls;
  private final Color color;

  RenderTileOverlay(Color color) {
    this.color = color;
    parts = new RenderTileOverlayPart[6];
    cityWalls = new boolean[]{false, false, false, false, false, false};
    for (int i = 0; i < 6; i++) {

      Render partHolder = new Render();
      partHolder.rotateZ.setAngle(30 + (60 * i));
      partHolder.translate.setY(0.77);

      RenderTileOverlayPart part = new RenderTileOverlayPart(color);
      partHolder.add(part);
      parts[i] = part;

      add(partHolder);
    }
  }

  void setOverlayVisible(boolean visible) {
    for (int i = 0; i < parts.length; i++) {
      parts[i].setWallVisible(visible || cityWalls[i]);
    }
  }

  void setCityWalls(boolean[] walls, double tileHeight, double greatestTileHeight) {
    this.cityWalls = walls;
    for (int i = 0; i < parts.length; i++) {
      boolean walled = walls[i];
      double wallHeight = greatestTileHeight + 0.2 - tileHeight;
      RenderTileOverlayPart part = parts[i];

      part.setPartColour(walled ? Color.YELLOW : color);
      part.setWallVisible(part.isWallVisible() || walled);

      double targetHeight = walled ? wallHeight : 0.1;
      part.setPartHeight(targetHeight);

      /*for (Shape3D part : parts[i]) {
        part.setMaterial(new PhongMaterial(walled ? Color.YELLOW : color));
        part.setVisible(part.isVisible() || walled);

        double targetHeight = walled ? wallHeight : 0.1;

        if(part instanceof Box) {
          ((Box) part).setDepth(targetHeight);
        } else if(part instanceof Cylinder) {
          ((Cylinder) part).setHeight(targetHeight);
        }

        part.setTranslateZ(targetHeight / 2);
      }*/
    }
  }

 /* private Render makeOverlayHexagonPart(Color color, int i, double angle) {
    Render render = new Render();
    render.rotateZ.setAngle(angle);

    parts[i] = new Shape3D[3];

    Box overlayPart = new Box(1, 0.2, 0.1);
    overlayPart.setMaterial(new PhongMaterial(color));
    overlayPart.setTranslateY(0.77);
    overlayPart.setTranslateZ(0.05);
    overlayPart.setVisible(false);
    render.add(overlayPart);
    parts[i][0] = overlayPart;

    render.add(makeOverlayHexagonJoinerPart(color, i, 0, -30));
    render.add(makeOverlayHexagonJoinerPart(color, i, 1, 30));

    return render;
  }

  private Render makeOverlayHexagonJoinerPart(Color color, int i, int joinIndex, double angle) {
    Render render = new Render();
    render.rotateZ.setAngle(angle);

    Render joinRotate = new Render();
    joinRotate.rotateX.setAngle(270);

    Cylinder joinerPart = new Cylinder(0.2, 0.1);
    joinerPart.setMaterial(new PhongMaterial(color));
    //joinerPart.setTranslateY(0.77);
    joinerPart.setTranslateZ(0.05);
    joinRotate.add(joinerPart);
    render.add(joinRotate);
    parts[i][joinIndex + 1] = joinerPart;

    return render;
  }*/
}
