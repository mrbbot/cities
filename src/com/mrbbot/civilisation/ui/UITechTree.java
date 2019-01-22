package com.mrbbot.civilisation.ui;

import com.mrbbot.civilisation.logic.techs.Tech;
import com.mrbbot.civilisation.logic.techs.TechTree;
import com.mrbbot.civilisation.logic.techs.Unlockable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.mrbbot.civilisation.ui.UIHelpers.colouredBackground;

public class UITechTree extends ScrollPane {
  Random random = new Random();

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

  private StackPane pane;
  private Set<Tech> renderedTechs;

  UITechTree() {
    super();

    pane = new StackPane();
    //pane.setBackground(colouredBackground(Color.DARKGRAY));
    pane.setAlignment(Pos.CENTER_LEFT);

    setVbarPolicy(ScrollBarPolicy.NEVER);
    setHbarPolicy(ScrollBarPolicy.ALWAYS);

    setFitToHeight(true);

    renderedTechs = new HashSet<>();
    addTechs(TechTree.ROOT);

    /*Node tech1 = makeTech("Tech 1");
    Node tech2 = makeTech("Tech 2");

    StackPane.setMargin(tech1, new Insets(0, 0, 0, 10));
    StackPane.setMargin(tech2, new Insets(-50, 0, 0, 150));

    Line line = new Line(0, 0, 10, 10);
    StackPane.setMargin(line, new Insets(0, 0, 0, 350));
    line.setStrokeWidth(5);
    line.setStrokeLineCap(StrokeLineCap.ROUND);
    pane.getChildren().add(line);

    pane.getChildren().addAll(tech1, tech2);*/

    setContent(pane);
  }

  private void addTechs(TechTree tree) {
    int x = tree.tech.getX();
    int y = tree.tech.getY();

    if(!renderedTechs.contains(tree.tech)) {
      renderedTechs.add(tree.tech);

      VBox tech = new VBox(10);
      tech.setMinWidth(150);
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
        y * 320,
        10,
        0,
        (x * 250) + 10
      ));
      pane.getChildren().add(tech);
    }

    if(tree.children.length > 0) {
      for (TechTree child : tree.children) {
        int endX = child.tech.getX();
        int endY = child.tech.getY();

        int xChange = endX - x;
        int yChange = endY - y;

        Line startLine = new Line(0, 0, ((xChange - 1) * 250) + 50, 0);
        StackPane.setMargin(startLine, new Insets(y * 320, 0, 0, (x * 250) + 10 + 150));
        startLine.setStrokeWidth(5);

        Line endLine = new Line(0, 0, 50, 0);
        StackPane.setMargin(endLine, new Insets(endY * 320, 0, 0, (endX * 250) - 45));
        endLine.setStrokeWidth(5);

        System.out.println(yChange);

        Line joinLine = new Line(0, 0, 0, yChange * 160); //TODO: may need to be 320
        StackPane.setMargin(joinLine, new Insets(y * 320, 0, 0, (endX * 250) - 45));
        joinLine.setStrokeWidth(5);
        joinLine.setStroke(new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1.0));

        pane.getChildren().addAll(startLine, endLine, joinLine);

        addTechs(child);
      }
    }
  }

/*
  private Node makeTech(String title) {
    Label titleLabel = makeCenteredLabel(title);
    titleLabel.setFont(TECH_TITLE_FONT);
    titleLabel.setPadding(new Insets(10));
    titleLabel.setAlignment(Pos.CENTER);
    titleLabel.setPrefWidth(Double.MAX_VALUE);
    titleLabel.setTextFill(Color.WHITE);
    titleLabel.setBackground(colouredBackground(Color.GREY.deriveColor(0, 1, 0.5, 1)));

    VBox tech = new VBox(
      10,
      titleLabel,
      new Label("Thing 1"),
      new Label("Thing 2"),
      new Label("Thing 3")
    );
    tech.setMinWidth(150);
    tech.setAlignment(Pos.CENTER);
    tech.setPadding(new Insets(0, 0, 10, 0));

    tech.setMaxSize(0, 0);
    tech.setBorder(TECH_BORDER);
    return tech;
  }
*/

  private Label makeCenteredLabel(String text) {
    Label label = new Label(text);
    label.setTextAlignment(TextAlignment.CENTER);
    return label;
  }
}
