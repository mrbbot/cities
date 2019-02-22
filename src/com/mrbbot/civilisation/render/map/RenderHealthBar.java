package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

@ClientOnly
public class RenderHealthBar extends RenderData<Living> {
  private final boolean extended;
  private Cylinder healthPart, remainingPart;

  public RenderHealthBar(Living data, boolean extended) {
    super(data);
    this.extended = extended;
    translateTo(0, 0, extended ? 1.6 : 0.8);
    rotateTo(0, 0, 90);

    healthPart = new Cylinder(0.1, extended ? 2 : 1);
    remainingPart = new Cylinder(0.05, 0);

    remainingPart.setMaterial(new PhongMaterial(Color.SLATEGREY));

    add(healthPart);
    add(remainingPart);

    updateRender(data);
  }

  private Color colorForHealthPercent(double healthPercent) {
    if(healthPercent > 0.6) {
      return Color.LIMEGREEN;
    } else if(healthPercent > 0.4) {
      return Color.YELLOW;
    } else if(healthPercent > 0.2) {
      return Color.ORANGERED;
    } else {
      return Color.RED;
    }
  }

  public void updateRender(Living living) {
    if(living == null || living.getHealth() == living.getBaseHealth()) {
      setVisible(false);
    } else {
      double length = extended ? 2 : 1;

      double healthPercent = living.getHealthPercent();
      double remainingPercent = 1 - healthPercent;

      healthPart.setMaterial(new PhongMaterial(colorForHealthPercent(healthPercent)));

      healthPart.setHeight(healthPercent * length);
      remainingPart.setHeight(remainingPercent * length);

      healthPart.setTranslateY(remainingPercent * length / 2.0);
      remainingPart.setTranslateY(-healthPercent * length / 2.0);

      setVisible(true);
    }
  }
}
