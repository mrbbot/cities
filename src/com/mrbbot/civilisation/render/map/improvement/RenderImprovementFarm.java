package com.mrbbot.civilisation.render.map.improvement;

import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@ClientOnly
public class RenderImprovementFarm extends Render {
  private static final double SIZE = 0.7;
  private static final Color FENCE_COLOUR = Color.BROWN.darker().darker();
  private static final Color GRASS_COLOUR = Color.GREEN;
  private static final Color SOIL_COLOUR = Color.BROWN.darker();

  RenderImprovementFarm(Map<String, Object> metadata) {
    double numStrips = (int) metadata.get("strips");

    double stripSize = SIZE / numStrips;
    double startTranslate = -(numStrips - 1) / 2.0 * stripSize;
    for (int i = 0; i < numStrips; i++) {
      Box strip = new Box(stripSize, SIZE, 0.1);
      strip.setTranslateX(startTranslate + (i * stripSize));
      strip.setMaterial(new PhongMaterial(i % 2 == 0 ? GRASS_COLOUR : SOIL_COLOUR));
      add(strip);
    }

    add(makeWall(0));
    add(makeWall(90));
    add(makeWall(180));
    add(makeWall(270));

    translate.setZ(0.05);
    rotateZ.setAngle((int) metadata.get("angle"));

    /*RenderImprovementHouse house = new RenderImprovementHouse(null);
    //house.translate.setZ(2);
    house.scaleTo(0.5);
    add(house);*/
  }

  private Render makeWall(double angle) {
    Render wallHolder = new Render();
    wallHolder.rotateZ.setAngle(angle);

    Box box = new Box(0.1, SIZE + 0.2, 0.15);
    box.setTranslateX((SIZE / 2) + 0.05);
    box.setTranslateZ(0.05);

    box.setMaterial(new PhongMaterial(FENCE_COLOUR));

    wallHolder.add(box);

    return wallHolder;
  }
}
