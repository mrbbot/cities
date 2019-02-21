package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.Map;

@ClientOnly
public class RenderImprovement extends RenderData<Improvement> {
  private final Tile tile;

  public RenderImprovement(Tile tile) {
    super(tile.improvement);
    this.tile = tile;
    setImprovement(data, tile.improvementMetadata);
  }

  public void setImprovement(Improvement data, Map<String, Object> metadata) {
    this.data = data;
    this.getChildren().clear();
    this.reset();

    switch(data) {
      case NONE:
        break;
      case CAPITAL:
        /*Box c = new Box(1, 1, 2);
        c.setTranslateZ(1);
        c.setMaterial(new PhongMaterial(tile.city.wallColour));
        add(c);*/
        add(new RenderImprovementHouse(tile.city.wallColour));
        break;
      case FARM:
        add(new RenderImprovementFarm(metadata));
        break;
      case TREE:
        add(new RenderImprovementTree());
        break;
    }
  }
}
