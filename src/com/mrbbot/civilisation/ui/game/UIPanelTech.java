package com.mrbbot.civilisation.ui.game;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class UIPanelTech extends BorderPane {
  private Label currentlyResearching;
  private ProgressIndicator progress;

  UIPanelTech() {
    super();

    Label currentlyResearchingHeading = new Label("Currently researching:");
    currentlyResearching = new Label("Industrialisation");
    currentlyResearching.setFont(new Font(24));
    currentlyResearching.setPadding(new Insets(0, 0, 5, 0));
    progress = new ProgressIndicator(0.5);
    progress.setPadding(new Insets(5, 5, 5, 0));
    Button openTechTree = new Button("Open Tech Tree");
    openTechTree.setPrefWidth(230);

    setTop(currentlyResearchingHeading);
    setLeft(progress);
    setCenter(currentlyResearching);
    setBottom(openTechTree);
  }

  void setCurrentlyResearching(String currentlyResearching, double progress) {
    this.currentlyResearching.setText(currentlyResearching);
    setProgress(progress);
  }

  void setProgress(double progress) {
    this.progress.setProgress(progress);
  }
}
