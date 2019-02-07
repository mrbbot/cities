package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.tile.Terrain;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.net.serializable.SerializablePoint2D;
import com.mrbbot.civilisation.render.map.improvement.RenderImprovement;
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
  private RenderImprovement improvement;
  private RenderTileOverlay overlay;
  private RenderUnit unit;
  private double height;

  RenderTile(Tile data) {
    super(data);

    SerializablePoint2D center = data.getHexagon().getCenter();
    translateTo(center.getX(), center.getY(), 0);

    colour = colourForTerrain(data.getTerrain());

    height = data.getHeight();
    Shape3D ground = data.getHexagon().getPrism(height);
    ground.getTransforms().add(new Translate(0, height / 2, 0));
    ground.setMaterial(new PhongMaterial(colour));
    add(ground);

    aboveGround = new Render();
    aboveGround.translateTo(0, 0, height);
    add(aboveGround);

    improvement = new RenderImprovement(data);
    aboveGround.add(improvement);

    overlay = new RenderTileOverlay(Color.WHITE);
    unit = new RenderUnit(data.unit);
    unit.setVisible(data.unit != null);
    aboveGround.add(overlay, unit);

    updateRender();
  }

  void updateRender() {
    overlay.setColor(data.selected ? Color.LIGHTBLUE : (data.canTraverse() ? Color.WHITE : Color.INDIANRED));
    if(data.city != null) {
      overlay.setCityWalls(data.city, data.getCityWalls(), height);
    }
    overlay.setSelected(data.selected);
    unit.updateRender(data.unit);
    unit.setVisible(data.unit != null);
  }

  public void updateImprovement() {
    improvement.setImprovement(data.improvement);
  }

  void setOverlayVisible(boolean visible) {
    overlay.setOverlayVisible(visible);
  }

  boolean isTranslucent() {
    return colour.getOpacity() < 1;
  }
}
