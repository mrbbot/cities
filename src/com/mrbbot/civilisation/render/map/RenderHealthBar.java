package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

/**
 * Render object for a health bar. Used by units and cities.
 */
@ClientOnly
public class RenderHealthBar extends RenderData<Living> {
  /**
   * Whether an extended health bar should be used. Primarily for cities.
   */
  private final boolean extended;
  /**
   * Cylinder representing the healthy part of the health bar
   */
  private Cylinder healthPart;
  /**
   * Cylinder representing the damaged part of the health bar
   */
  private Cylinder remainingPart;

  /**
   * Constructor for health bar render object
   *
   * @param data     living object to render health for
   * @param extended whether an extended health bar should be used
   */
  public RenderHealthBar(Living data, boolean extended) {
    super(data);
    this.extended = extended;

    // Translate the health bar up
    translateTo(0, 0, extended ? 1.6 : 0.8);
    rotateTo(0, 0, 90);

    // Create the cylinders
    healthPart = new Cylinder(0.1, extended ? 2 : 1);
    remainingPart = new Cylinder(0.05, 0);

    remainingPart.setMaterial(new PhongMaterial(Color.SLATEGREY));

    add(healthPart);
    add(remainingPart);

    // Update the state of the health bar render
    updateRender(data);
  }

  /**
   * Calculate the colour for the healthy part of the bar
   *
   * @param healthPercent percentage health of the living object
   * @return colour to be used for rendering the health bar
   */
  private Color colorForHealthPercent(double healthPercent) {
    // Use a different colour depending on the interval the health percent is
    // in
    if (healthPercent > 0.6) {
      return Color.LIMEGREEN;
    } else if (healthPercent > 0.4) {
      return Color.YELLOW;
    } else if (healthPercent > 0.2) {
      return Color.ORANGERED;
    } else {
      return Color.RED;
    }
  }

  /**
   * Updates the render's state based on the health of the living object it
   * represents
   *
   * @param living living object containing health data
   */
  void updateRender(Living living) {
    // If the living doesn't exist, or it's at max health, hide the bar
    if (living == null || living.getHealth() == living.getBaseHealth()) {
      setVisible(false);
    } else {
      double length = extended ? 2 : 1;

      double healthPercent = living.getHealthPercent();
      double remainingPercent = 1 - healthPercent;

      // Otherwise set the colour based on the health percent
      healthPart.setMaterial(
        new PhongMaterial(colorForHealthPercent(healthPercent))
      );

      // Set the size and position of the bar based on the health percent
      healthPart.setHeight(healthPercent * length);
      remainingPart.setHeight(remainingPercent * length);

      healthPart.setTranslateY(remainingPercent * length / 2.0);
      remainingPart.setTranslateY(-healthPercent * length / 2.0);

      // Make the bar visible
      setVisible(true);
    }
  }
}
