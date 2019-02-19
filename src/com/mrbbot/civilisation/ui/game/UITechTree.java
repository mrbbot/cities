package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.techs.Tech;
import com.mrbbot.civilisation.logic.techs.TechTree;
import com.mrbbot.civilisation.logic.interfaces.Unlockable;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.Set;

import static com.mrbbot.civilisation.ui.UIHelpers.colouredBackground;

@ClientOnly
public class UITechTree extends ScrollPane {
  private static final Font TECH_TITLE_FONT = Font.font(Font.getDefault().getFamily(), FontWeight.EXTRA_BOLD, 20);
  private static final CornerRadii TECH_CORNERS = new CornerRadii(5);
  private static final BorderWidths TECH_BORDERS = new BorderWidths(5);
  private static final Border TECH_BORDER = new Border(
    new BorderStroke(
      Color.BLACK,
      BorderStrokeStyle.SOLID,
      TECH_CORNERS,
      TECH_BORDERS
    )
  );
  private static final double TECH_WIDTH = 150;
  private static final double TECH_HORIZONTAL_SPACING = 100;
  private static final double TECH_VERTICAL_SPACING = 320;

  private GraphicsContext lineGraphics;
  private StackPane techPane;
  private Set<Tech> renderedTechs;
  private int lineOffset;

  UITechTree(int height) {
    super();
    setVbarPolicy(ScrollBarPolicy.NEVER);
    setHbarPolicy(ScrollBarPolicy.ALWAYS);
    setFitToHeight(true);

    lineOffset = height / 2;
    Canvas lineCanvas = new Canvas((TechTree.MAX_X * 250) + 20, height);
    lineGraphics = lineCanvas.getGraphicsContext2D();
    lineGraphics.setLineWidth(5);

    techPane = new StackPane();
    techPane.setAlignment(Pos.CENTER_LEFT);
    renderedTechs = new HashSet<>();
    addTechs(TechTree.ROOT);

    StackPane rootPane = new StackPane();
    rootPane.setAlignment(Pos.TOP_LEFT);
    rootPane.getChildren().addAll(lineCanvas, techPane);
    setContent(rootPane);
  }

  private void addTechs(TechTree tree) {
    int x = tree.tech.getX();
    int y = tree.tech.getY();

    double renderX = (x * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 10;
    double renderY = y * TECH_VERTICAL_SPACING;

    if (!renderedTechs.contains(tree.tech)) {
      renderedTechs.add(tree.tech);

      VBox tech = new VBox(10);
      tech.setMinWidth(TECH_WIDTH);
      tech.setAlignment(Pos.CENTER);
      tech.setPadding(new Insets(0, 0, tree.tech.getUnlocks().length > 0 ? 10 : 0, 0));
      tech.setMaxSize(0, 0);
      tech.setBorder(TECH_BORDER);

      Label titleLabel = makeCenteredLabel(tree.tech.getName());
      titleLabel.setFont(TECH_TITLE_FONT);
      titleLabel.setPadding(new Insets(10));
      titleLabel.setAlignment(Pos.CENTER);
      titleLabel.setPrefWidth(Double.MAX_VALUE);
      titleLabel.setTextFill(Color.WHITE);
      titleLabel.setBackground(colouredBackground(tree.tech.getColour()));
      tech.getChildren().add(titleLabel);

      for (Unlockable unlock : tree.tech.getUnlocks()) {
        tech.getChildren().add(makeCenteredLabel(unlock.getName()));
      }

      StackPane.setMargin(tech, new Insets(
        renderY,
        10,
        0,
        renderX
      ));
      techPane.getChildren().add(tech);
    }

    // Update renderY for line coordinates
    renderY = (renderY / 2) + lineOffset;

    if (tree.children.length > 0) {
      for (TechTree child : tree.children) {
        int endX = child.tech.getX();
        int endY = child.tech.getY();

        double endRenderX = (endX * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 10;
        double endRenderY = (endY * TECH_VERTICAL_SPACING / 2) + lineOffset;
        double startX = renderX + TECH_WIDTH;
        double midRenderX = (startX + endRenderX) / 2;

        lineGraphics.setStroke(new LinearGradient(
          0, 0,
          1, 0,
          true, null,
          new Stop(0, tree.tech.getColour()),
          new Stop(1, child.tech.getColour()))
        );
        lineGraphics.beginPath();
        // Start Coordinates
        lineGraphics.moveTo(startX, renderY);
        lineGraphics.bezierCurveTo(
          // Control Point 1
          midRenderX, renderY,
          // Control Point 2
          midRenderX, endRenderY,
          // End Coordinates
          endRenderX, endRenderY
        );
        lineGraphics.stroke();
        lineGraphics.closePath();

        addTechs(child);
      }
    }
  }

  private Label makeCenteredLabel(String text) {
    Label label = new Label(text);
    label.setTextAlignment(TextAlignment.CENTER);
    return label;
  }
}
