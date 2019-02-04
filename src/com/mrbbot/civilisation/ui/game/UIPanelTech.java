package com.mrbbot.civilisation.ui.game;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UIPanelTech extends BorderPane {
  Label currentlyResearching;
  ProgressIndicator progressBar;

  UIPanelTech(Border border) {
    super();
    //setMaxSize(150, 0);

    Label currentlyResearchingHeading = new Label("Currently researching:");
    currentlyResearching = new Label("Industrialisation");
    currentlyResearching.setFont(new Font(24));
    currentlyResearching.setPadding(new Insets(0, 0, 5, 0));
    progressBar = new ProgressIndicator(0.5);
    progressBar.setPadding(new Insets(5, 5, 5, 0));
    Button openTechTree = new Button("Open Tech Tree");
    openTechTree.setPrefWidth(250);

    setTop(currentlyResearchingHeading);
    setLeft(progressBar);
    setCenter(currentlyResearching);
    setBottom(openTechTree);

    /*getChildren().addAll(currentlyResearchingHeading, currentlyResearching, progressBar, openTechTree);
*/
    setPadding(new Insets(10));
    setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0, 0, 20, 0, false), null)));
    setBorder(border);
  }
}
