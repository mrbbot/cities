package com.mrbbot.civilisation.ui.game;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

class Badge extends StackPane {
  private static final int BADGE_SIZE = 16;

  Badge(BadgeType badgeType) {
    super();
    setAlignment(Pos.CENTER);

    Canvas canvas = new Canvas(BADGE_SIZE, BADGE_SIZE);
    GraphicsContext g = canvas.getGraphicsContext2D();
    g.setFill(badgeType.color);
    g.fillOval(0, 0, BADGE_SIZE, BADGE_SIZE);
    g.fill();

    Label label = new Label(String.valueOf(badgeType.character));
    label.setTextFill(badgeType.textColor);
    label.setFont(new Font(10));
    getChildren().addAll(canvas, label);
  }
}
