package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.tile.Terrain;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.generic.render.Render;
import com.mrbbot.generic.render.RenderData;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Translate;

public class RenderTile extends RenderData<Tile> {
  private static Color colourForTerrain(Terrain terrain) {
    Color min = Color.BLACK;
    Color max = Color.BLACK;
    switch (terrain.level) {
      case MOUNTAIN:
        min = Color.GRAY;
        max = Color.WHITE;
        break;
      case PLAIN:
        min = Color.GREEN;
        max = Color.LIGHTGREEN;
        break;
      case BEACH:
        min = Color.GOLDENROD;
        max = Color.LIGHTGOLDENRODYELLOW;
        break;
      case OCEAN:
        max = new Color(0, 0.66, 1, 0.5);
        break;
    }
    double t = (terrain.height - terrain.level.minHeight) / (terrain.level.maxHeight - terrain.level.minHeight);
    return min.interpolate(max, t);
  }

  private Color colour;
  private Render aboveGround;
  private RenderTileOverlay overlay;

  RenderTile(Tile data) {
    super(data);

    Point2D center = data.getHexagon().getCenter();
    translateTo(center.getX(), center.getY(), 0);

    colour = colourForTerrain(data.getTerrain());

    double height = (data.getTerrain().height * 2) + 1; // 1 <= height <= 3
    Shape3D ground = data.getHexagon().getPrism(height);
    ground.getTransforms().add(new Translate(0, height / 2, 0));
    ground.setMaterial(new PhongMaterial(colour));
    add(ground);

    aboveGround = new Render();
    aboveGround.translateTo(0, 0, height);
    add(aboveGround);

    overlay = new RenderTileOverlay(data.canTraverse() ? Color.WHITE : Color.INDIANRED);
    aboveGround.add(overlay);

    if(data.city != null) {
      overlay.setCityWalls(data.adjacentCityTiles());
      System.out.println("walls");
    }

}

  void setOverlayVisible(boolean visible) {
    overlay.setOverlayVisible(visible);
  }

  boolean isTranslucent() {
    return colour.getOpacity() < 1;
  }
}
