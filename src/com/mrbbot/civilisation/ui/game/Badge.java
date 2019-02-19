package com.mrbbot.civilisation.ui.game;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

class Badge extends StackPane {
  private static final int BADGE_SIZE = 16;

  static Badge makeScienceBadge() {
    return new Badge(Color.DEEPSKYBLUE, 'S');
  }

  static Badge makeGoldBadge() {
    return new Badge(Color.GOLD, '£');
  }

  static Badge makeProductionBadge() {
    return new Badge(Color.ORANGE, 'P');
  }

  static Badge makeFoodBadge() {
    return new Badge(Color.GREEN, '@');
  }

  static Badge makeHealthBadge() {
    return new Badge(Color.MAGENTA, 'H');
  }

  Badge(Color colour, char character) {
    super();
    setAlignment(Pos.CENTER);

    Canvas canvas = new Canvas(BADGE_SIZE, BADGE_SIZE);
    GraphicsContext g = canvas.getGraphicsContext2D();
    g.setFill(colour);
    g.fillOval(0, 0, BADGE_SIZE, BADGE_SIZE);
    g.fill();

    Label label = new Label(String.valueOf(character));
    label.setTextFill(colour.darker());
    label.setFont(new Font(10));
    getChildren().addAll(canvas, label);
  }
}
