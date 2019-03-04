package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.techs.Unlockable;
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

/**
 * Tech tree UI. Overlaid on top of the entire game interface when shown
 * so that it takes up the full screen. Extends scroll pane to enable
 * horizontal scrolling.
 */
@ClientOnly
public class UITechTree extends ScrollPane {
  /**
   * Font to be used for rendering the name of technologies in the tree
   */
  private static final Font TECH_TITLE_FONT = Font.font(
    Font.getDefault().getFamily(),
    FontWeight.EXTRA_BOLD,
    15
  );
  /**
   * Default border for technologies in the tree
   */
  private static final Border TECH_BORDER = new Border(
    new BorderStroke(
      Color.BLACK,
      BorderStrokeStyle.SOLID,
      new CornerRadii(5),
      new BorderWidths(5)
    )
  );
  /**
   * Border for technologies that can be unlocked in the tree. Only used when
   * the player is able to select a new technology (i.e. when one isn't being
   * researched).
   */
  private static final Border TECH_CAN_UNLOCK_BORDER = new Border(
    new BorderStroke(
      Color.LIMEGREEN,
      BorderStrokeStyle.SOLID,
      new CornerRadii(5),
      new BorderWidths(5)
    )
  );
  /**
   * Border for the technology that is currently being unlocked in the tree
   */
  private static final Border TECH_UNLOCKING_BORDER = new Border(
    new BorderStroke(
      Color.DEEPSKYBLUE,
      BorderStrokeStyle.SOLID,
      new CornerRadii(5),
      new BorderWidths(5)
    )
  );
  /**
   * Width of the rounded rectangle that represents a technology in the tree
   */
  private static final double TECH_WIDTH = 150;
  /**
   * Horizontal spacing between horizontally adjacent techs (spacing between
   * each level of techs). See {@link Tech} for level information.
   */
  private static final double TECH_HORIZONTAL_SPACING = 150;
  /**
   * Vertical spacing between vertically adjacent techs (spacing between
   * techs on the same level). See {@link Tech} for level information.
   */
  private static final double TECH_VERTICAL_SPACING = 320;

  /**
   * ID of the current player
   */
  private final String playerId;
  /**
   * Graphics context for rendering the connecting curves between technologies
   * in the tree.
   */
  private GraphicsContext lineGraphics;
  /**
   * Pane containing the rounded rectangles for each of the techs in the tree.
   */
  private StackPane techPane;
  /**
   * Map mapping techs to their rounded rectangles in the UI.
   */
  private Map<Tech, Region> renderedTechs;
  /**
   * Middle of the screen. Screen y-coordinate to render techs with a
   * y-coordinate of 0. See {@link Tech#getY()}.
   */
  private int lineOffset;

  /**
   * Create a new tech tree UI
   *
   * @param game     game containing the current player
   * @param playerId ID of the current player
   * @param details  the current player's tech details
   * @param height   the screen height
   */
  UITechTree(
    Game game,
    String playerId,
    PlayerTechDetails details,
    int height
  ) {
    super();
    this.playerId = playerId;

    // Always show the horizontal scroll bar
    setVbarPolicy(ScrollBarPolicy.NEVER);
    setHbarPolicy(ScrollBarPolicy.ALWAYS);
    setFitToHeight(true);

    // Position techs with a y-coordinate of 0 in the middle of the screen
    lineOffset = height / 2;
    // Create the canvas and context for rendering connecting curves between
    // the techs
    Canvas lineCanvas = new Canvas(
      // Max it wide enough to contain all the lines
      (Tech.MAX_X * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 20,
      height
    );
    lineGraphics = lineCanvas.getGraphicsContext2D();
    lineGraphics.setLineWidth(5);

    // Create the pane/map that all tech rounded rectangles should be added to
    techPane = new StackPane();
    techPane.setAlignment(Pos.CENTER_LEFT);
    renderedTechs = new HashMap<>();
    // Traverse the tech tree, adding all techs to the UI along the way
    addTechs(Tech.getRoot());

    // Stack the tech rectangles on top of the connecting lines
    StackPane rootPane = new StackPane();
    rootPane.setAlignment(Pos.TOP_LEFT);
    rootPane.getChildren().addAll(lineCanvas, techPane);
    setContent(rootPane);

    // Set the initial player tech details, making certain techs clickable in
    // the tree.
    setTechDetails(game, details);
  }

  /**
   * Adds techs' rounded rectangles to the UI so they can be seen/selected by
   * the user.
   *
   * @param techToRender root of the tech tree containing children that will
   *                     be recursively passed back to this function to render
   *                     their children and so fourth
   */
  private void addTechs(Tech techToRender) {
    // Calculate the position to render this tech in
    int x = techToRender.getX();
    int y = techToRender.getY();

    double renderX = (x * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 10;
    double renderY = y * TECH_VERTICAL_SPACING;

    // Make sure each tech is only rendered once
    if (!renderedTechs.containsKey(techToRender)) {
      // Create the rounded rectangle for this tech with some vertical spacing
      // between its subcomponents
      VBox tech = new VBox(10);
      tech.setMinWidth(TECH_WIDTH);
      tech.setAlignment(Pos.CENTER);
      tech.setPadding(new Insets(
        0,
        0,
        // Give the tech some padding if it unlocks things so the unlock list
        // is centered (if it exists)
        techToRender.getUnlocks().size() > 0 ? 10 : 0,
        0
      ));
      tech.setMaxSize(0, 0);
      tech.setBorder(TECH_BORDER);
      // Store the render so the tech isn't rendered again and so it can be
      // updated later
      renderedTechs.put(techToRender, tech);

      // Create/add a label for the name of the tech
      Label titleLabel = makeCenteredLabel(techToRender.getName());
      titleLabel.setFont(TECH_TITLE_FONT);
      titleLabel.setPadding(new Insets(10));
      titleLabel.setAlignment(Pos.CENTER);
      titleLabel.setPrefWidth(Double.MAX_VALUE);
      titleLabel.setTextFill(Color.WHITE);
      titleLabel.setBackground(colouredBackground(techToRender.getColour()));
      tech.getChildren().add(titleLabel);

      // Create/add labels for each of the techs unlocks
      for (Unlockable unlock : techToRender.getUnlocks()) {
        tech.getChildren().add(makeCenteredLabel(unlock.getName()));
      }

      // Position the tech on the screen
      StackPane.setMargin(tech, new Insets(
        renderY,
        10,
        0,
        renderX
      ));
      // Add it to the UI
      techPane.getChildren().add(tech);
    }

    // Update renderY for line coordinates
    renderY = (renderY / 2) + lineOffset;

    // Render the connections between this tech and any children. Then render
    // those children if they haven't already been.
    for (Tech child : techToRender.getRequiredBy()) {
      // Calculate the end coordinates of the connecting line
      int endX = child.getX();
      int endY = child.getY();

      double endRenderX =
        (endX * (TECH_WIDTH + TECH_HORIZONTAL_SPACING)) + 10;
      double endRenderY = (endY * TECH_VERTICAL_SPACING / 2) + lineOffset;
      double startX = renderX + TECH_WIDTH;
      double midRenderX = (startX + endRenderX) / 2;

      // Set the stroke colour to a linear gradient of the different techs'
      // colours
      lineGraphics.setStroke(new LinearGradient(
        0, 0,
        1, 0,
        true, null,
        new Stop(0, techToRender.getColour()),
        new Stop(1, child.getColour()))
      );
      // Start drawing the connecting curve
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

      // Actually draw the line to the canvas
      lineGraphics.stroke();
      lineGraphics.closePath();

      // Add the child to the UI along with any of its children (recursive call)
      addTechs(child);
    }
  }

  /**
   * Creates a label with centered text
   *
   * @param text text of the label
   * @return label containing the specified text in the center
   */
  private Label makeCenteredLabel(String text) {
    Label label = new Label(text);
    label.setTextAlignment(TextAlignment.CENTER);
    return label;
  }

  /**
   * Update the rounded rectangles representing the different technologies.
   *
   * @param game    game the current player is contained within
   * @param details current player's technology details
   */
  void setTechDetails(Game game, PlayerTechDetails details) {
    // Iterate through all of the tech renders
    for (Map.Entry<Tech, Region> entry : renderedTechs.entrySet()) {
      final Tech tech = entry.getKey();
      final Region render = entry.getValue();

      // Check if this tech is currently being unlocked
      boolean current = tech.equals(details.currentlyUnlocking)
        && details.percentUnlocked < 1;
      // Check if this tech is already unlocked
      boolean unlocked = tech.getScienceCost() == 0
        || details.unlockedTechs.contains(tech)
        || (tech.equals(details.currentlyUnlocking)
        && details.percentUnlocked == 1);
      unlocked = true;
      // Check if the player can unlock this tech given its requirements
      boolean canUnlock = !unlocked &&
        details.currentlyUnlocking == null &&
        tech.canUnlockGivenUnlocked(details.unlockedTechs);

      // Make the tech opaque only if any of these conditions are true
      render.setOpacity(
        unlocked || current || canUnlock
          ? 1
          : 0.2
      );
      // Set the tech's border depending on these conditions
      render.setBorder(
        canUnlock
          ? TECH_CAN_UNLOCK_BORDER
          : (current ? TECH_UNLOCKING_BORDER : TECH_BORDER)
      );

      // Register a click listener if this tech can be unlocked
      render.setOnMouseClicked(
        canUnlock
          ? (e) -> {
          // Request the player start researching this technology
          PacketPlayerResearchRequest packetPlayerResearchRequest =
            new PacketPlayerResearchRequest(playerId, tech);
          // Handle it locally and broadcast it to keep the game state in sync
          game.handlePacket(packetPlayerResearchRequest);
          Civilisation.CLIENT.broadcast(packetPlayerResearchRequest);
        }
          : null
      );
    }
  }
}
