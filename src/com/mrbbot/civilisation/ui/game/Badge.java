package com.mrbbot.civilisation.ui.game;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * Pane showing a "badge", a coloured circle with text on top.
 */
class Badge extends StackPane {
  /**
   * Size of the badge's coloured circle
   */
  private static final int BADGE_SIZE = 16;

  /**
   * Creates a new badge
   *
   * @param badgeType type of the badge containing information on the colour
   *                  and text
   */
  Badge(BadgeType badgeType) {
    super();
    setAlignment(Pos.CENTER);

    // Create the coloured circle
    Canvas canvas = new Canvas(BADGE_SIZE, BADGE_SIZE);
    GraphicsContext g = canvas.getGraphicsContext2D();
    g.setFill(badgeType.color);
    g.fillOval(0, 0, BADGE_SIZE, BADGE_SIZE);
    g.fill();

    // Create the text label
    Label label = new Label(badgeType.text);
    label.setTextFill(badgeType.textColor);
    label.setFont(new Font(10));

    // Stack them on top of each other
    getChildren().addAll(canvas, label);
  }
}
