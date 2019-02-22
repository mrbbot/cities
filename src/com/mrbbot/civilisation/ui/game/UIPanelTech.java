package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.generic.net.ClientOnly;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

@ClientOnly
public class UIPanelTech extends BorderPane {
  private Label currentlyResearching;
  private ProgressIndicator progress;
  private Button openTechTree;

  UIPanelTech() {
    super();

    Label currentlyResearchingHeading = new Label("Currently researching:");
    currentlyResearching = new Label("Industrialisation");
    currentlyResearching.setFont(new Font(24));
    currentlyResearching.setPadding(new Insets(0, 0, 5, 0));
    progress = new ProgressIndicator(0.5);
    progress.setPadding(new Insets(5, 5, 5, 0));
    openTechTree = new Button("Open Tech Tree");
    openTechTree.setPrefWidth(230);

    setTop(currentlyResearchingHeading);
    setLeft(progress);
    setCenter(currentlyResearching);
    setBottom(openTechTree);
  }

  void setTechDetails(PlayerTechDetails details) {
    this.currentlyResearching.setText(details.currentlyUnlocking == null
      ? "Nothing"
      : details.currentlyUnlocking.getName()
    );
    this.progress.setProgress(details.percentUnlocked);
  }

  void setOnOpenTechTree(EventHandler<ActionEvent> value) {
    openTechTree.setOnAction(value);
  }
}
