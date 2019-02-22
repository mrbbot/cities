package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.interfaces.Unlockable;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.civilisation.logic.techs.Tech;
import com.mrbbot.civilisation.net.packet.PacketPlayerResearchRequest;
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

import java.util.*;

import static com.mrbbot.civilisation.ui.UIHelpers.colouredBackground;

@ClientOnly
public class UITechTree extends ScrollPane {
  private static final Font TECH_TITLE_FONT = Font.font(Font.getDefault().getFamily(), FontWeight.EXTRA_BOLD, 15);
  private static final Border TECH_BORDER = new Border(
    new BorderStroke(
      Color.BLACK,
      BorderStrokeStyle.SOLID,
      new CornerRadii(5),
      new BorderWidths(5)
    )
  );
  private static final Border TECH_CAN_UNLOCK_BORDER = new Border(
    new BorderStroke(
      Color.LIMEGREEN,
      BorderStrokeStyle.SOLID,
      new CornerRadii(5),
      new BorderWidths(5)
    )
  );
  private static final Border TECH_UNLOCKING_BORDER = new Border(
    new BorderStroke(
      Color.DEEPSKYBLUE,
      BorderStrokeStyle.SOLID,
      new CornerRadii(5),
      new BorderWidths(5)
    )
  );
  private static final double TECH_WIDTH = 150;
  private static final double TECH_HORIZONTAL_SPACING = 150; //100
  private static final double TECH_VERTICAL_SPACING = 320;

  private final String playerId;
  private GraphicsContext lineGraphics;
  private StackPane techPane;
  private Map<Tech, Region> renderedTechs;
  private int lineOffset;

  UITechTree(Game game, String playerId, PlayerTechDetails details, int height) {
    super();
    this.playerId = playerId;

    setVbarPolicy(ScrollBarPolicy.NEVER);
    setHbarPolicy(ScrollBarPolicy.ALWAYS);
    setFitToHeight(true);

    lineOffset = height / 2;
    Canvas lineCanvas = new Canvas((Tech.MAX_X * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 20, height);
    lineGraphics = lineCanvas.getGraphicsContext2D();
    lineGraphics.setLineWidth(5);

    techPane = new StackPane();
    techPane.setAlignment(Pos.CENTER_LEFT);
    renderedTechs = new HashMap<>();
    addTechs(Tech.getRoot());

    StackPane rootPane = new StackPane();
    rootPane.setAlignment(Pos.TOP_LEFT);
    rootPane.getChildren().addAll(lineCanvas, techPane);
    setContent(rootPane);

    setTechDetails(game, details);
  }

  private void addTechs(Tech techToRender) {
    int x = techToRender.getX();
    int y = techToRender.getY();

    double renderX = (x * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 10;
    double renderY = y * TECH_VERTICAL_SPACING;

    if (!renderedTechs.containsKey(techToRender)) {
      VBox tech = new VBox(10);
      tech.setMinWidth(TECH_WIDTH);
      tech.setAlignment(Pos.CENTER);
      tech.setPadding(new Insets(0, 0, techToRender.getUnlocks().size() > 0 ? 10 : 0, 0));
      tech.setMaxSize(0, 0);
      tech.setBorder(TECH_BORDER);
      renderedTechs.put(techToRender, tech);

      Label titleLabel = makeCenteredLabel(techToRender.getName());
      titleLabel.setFont(TECH_TITLE_FONT);
      titleLabel.setPadding(new Insets(10));
      titleLabel.setAlignment(Pos.CENTER);
      titleLabel.setPrefWidth(Double.MAX_VALUE);
      titleLabel.setTextFill(Color.WHITE);
      titleLabel.setBackground(colouredBackground(techToRender.getColour()));
      tech.getChildren().add(titleLabel);

      for (Unlockable unlock : techToRender.getUnlocks()) {
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

    if (techToRender.getRequiredBy().size() > 0) {
      for (Tech child : techToRender.getRequiredBy()) {
        int endX = child.getX();
        int endY = child.getY();

        double endRenderX = (endX * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 10;
        double endRenderY = (endY * TECH_VERTICAL_SPACING / 2) + lineOffset;
        double startX = renderX + TECH_WIDTH;
        double midRenderX = (startX + endRenderX) / 2;

        lineGraphics.setStroke(new LinearGradient(
          0, 0,
          1, 0,
          true, null,
          new Stop(0, techToRender.getColour()),
          new Stop(1, child.getColour()))
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

  void setTechDetails(Game game, PlayerTechDetails details) {
    for (Map.Entry<Tech, Region> entry : renderedTechs.entrySet()) {
      final Tech tech = entry.getKey();
      final Region render = entry.getValue();

      boolean current = tech.equals(details.currentlyUnlocking) && details.percentUnlocked < 1;
      boolean unlocked = tech.getScienceCost() == 0 || details.unlockedTechs.contains(tech) || (tech.equals(details.currentlyUnlocking) && details.percentUnlocked == 1);
      boolean canUnlock = !unlocked &&
        details.currentlyUnlocking == null &&
        tech.canUnlockGivenUnlocked(details.unlockedTechs);

      render.setOpacity(
        unlocked || current || canUnlock
          ? 1
          : 0.2
      );
      render.setBorder(canUnlock ? TECH_CAN_UNLOCK_BORDER : (current ? TECH_UNLOCKING_BORDER : TECH_BORDER));

      render.setOnMouseClicked(canUnlock ? (e) -> {
        PacketPlayerResearchRequest packetPlayerResearchRequest = new PacketPlayerResearchRequest(playerId, tech);
        game.handlePacket(packetPlayerResearchRequest);
        Civilisation.CLIENT.broadcast(packetPlayerResearchRequest);
      } : null);
    }
  }
}
