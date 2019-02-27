package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.geometry.Path;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderData;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

/**
 * Main render object for representing the game state. Handles rendering tiles
 * (terrain, improvements, units, cities).
 */
@ClientOnly
public class RenderGame extends RenderData<Game> {
  /**
   * Function to be called when the player (de)selects a unit. Null should be
   * passed on deselection.
   */
  private final Consumer<Unit> selectedUnitListener;
  /**
   * Function to be called when the player (de)selects a city. Null should be
   * passed on deselection.
   */
  private final Consumer<City> selectedCityListener;

  /**
   * The current player of the game for this client
   */
  public Player currentPlayer;
  /**
   * The city that has been selected by the player. Null if no city is
   * selected.
   */
  private City selectedCity;

  /**
   * Creates a new game render object
   *
   * @param data                 game to render
   * @param id                   id of the current player
   * @param selectedUnitListener function to be called when the selected unit
   *                             changes
   * @param selectedCityListener function to be called when the selected city
   *                             changes
   */
  public RenderGame(
    Game data,
    String id,
    Consumer<Unit> selectedUnitListener,
    Consumer<City> selectedCityListener
  ) {
    super(data);
    this.selectedUnitListener = selectedUnitListener;
    this.selectedCityListener = selectedCityListener;

    // Find the current player
    for (Player player : data.players) {
      if (player.id.equals(id)) {
        currentPlayer = player;
        break;
      }
    }
    if (currentPlayer == null) {
      throw new IllegalStateException(
        "current player not found in player list"
      );
    }

    // Priority queue that ensures translucent tiles are added to the render
    // last. This is required for the translucency effect to work.
    final PriorityQueue<RenderTile> tilesToAdd = new PriorityQueue<>(
      // Orders tiles with translucent tiles at the end of the queue
      (a, b) -> {
        if (a.isTranslucent() && !b.isTranslucent()) return 1;
        else if (b.isTranslucent() && !a.isTranslucent()) return -1;
        else return 0;
      }
    );

    // Iterates through every possible hex tile position
    data.hexagonGrid.forEach((tile, hex, x, y) -> {
      // Creates the render object for that tile
      RenderTile renderTile = new RenderTile(
        tile,
        data.hexagonGrid.getNeighbours(x, y, false)
      );
      tile.renderer = renderTile;

      // Registers click listener for this tile
      renderTile.setOnMouseClicked((e) -> {
        // Ignore the click if we're waiting for other players
        if (data.waitingForPlayers) return;

        if (e.getButton() == MouseButton.PRIMARY) {
          // If this was a left click, the user is trying to select this tile
          if (tile.city != null
            && tile.city.getCenter().samePositionAs(tile)
            && tile.city.player.equals(currentPlayer)) {
            // Prioritise selecting a capital city is there is one
            setSelectedCity(tile.city);
            setSelectedUnit(null);
            return;
          }
          // Otherwise select the unit on this tile
          setSelectedCity(null);
          // tile.unit will be null if there isn't a unit, deselecting any
          // previously selected units
          setSelectedUnit(tile.unit);
        } else if (e.getButton() == MouseButton.SECONDARY) {
          // If this was a right click, the user is trying to purchase a tile
          // or attack something
          if (selectedCity != null) {
            // If there's a selected city, this must be a purchase request
            PacketPurchaseTileRequest packetPurchaseTileRequest
              = new PacketPurchaseTileRequest(
              selectedCity.getX(),
              selectedCity.getY(),
              tile.x,
              tile.y
            );
            // Try the purchase request, if null was received, then it didn't
            // work so there's no need to broadcast it
            if (handlePacket(packetPurchaseTileRequest) != null) {
              // If it did work, update the game state of other clients
              Civilisation.CLIENT.broadcast(packetPurchaseTileRequest);
            }
          } else if (data.selectedUnit != null) {
            // If there's a selected unit, try and attack with it
            PacketDamage packetDamage = new PacketDamage(
              data.selectedUnit.tile.x,
              data.selectedUnit.tile.y,
              tile.x,
              tile.y
            );
            // If that returned null, the attack didn't work, so there's no
            // need to broadcast it
            if (handlePacket(packetDamage) != null) {
              // If it did work, update the game state of other clients
              Civilisation.CLIENT.broadcast(packetDamage);
            }
          }
        }
      });

      // Registers drag listener for this tile
      renderTile.setOnMouseDragged((e) -> {
        // Ignore the click if we're waiting for other players
        if (data.waitingForPlayers) return;

        // If this was a right click drag, the user is trying to path-find
        if (e.getButton() == MouseButton.SECONDARY) {
          // If there isn't a start to the path yet, mark this initial tile
          // as it, providing it contains a unit that can be moved
          if (pathStartTile == null
            && renderTile.data.unit != null
            && renderTile.data.unit.player.equals(currentPlayer)
            && renderTile.data.unit.hasAbility(UnitAbility.ABILITY_MOVEMENT)) {
            pathStartTile = renderTile;
            // Show the pathfinding overlay
            pathStartTile.setOverlayVisible(true);
          }

          // If there's a starting tile
          if (pathStartTile != null) {
            // Get the tile the user is dragging over and check it exists
            RenderTile pickedTile = getTileFromPickResult(e.getPickResult());
            if ((pickedTile == null || pickedTile != pathEndTile)
              && pathEndTile != null) {
              // If it's different to the last end, reset the pathfinding end
              resetPathfindingEnd();
            }
            // If there was a tile
            if (pickedTile != null) {
              // Check if a path can be made to the tile
              RenderTile potentialEnd = pickedTile;

              List<Tile> path = data.hexagonGrid.findPath(
                pathStartTile.data.x,
                pathStartTile.data.y,
                potentialEnd.data.x,
                potentialEnd.data.y,
                pathStartTile.data.unit.remainingMovementPointsThisTurn
              ).path;

              // Mark the path for the user
              path.forEach((p) -> p.renderer.setOverlayVisible(true));

              // Set the end if there's a valid path
              if (path.size() > 1) {
                potentialEnd = path.get(path.size() - 1).renderer;
              }
              pathEndTile = potentialEnd;
              pathEndTile.setOverlayVisible(path.size() > 1);
            }
          }
        }
      });

      // Adds the tile to the render queue
      tilesToAdd.add(renderTile);
    });

    // Add a box underneath the tiles to represent a boardgame board that the
    // game is being played on
    Point2D topLeftCenter =
      data.hexagonGrid.get(0, 0).getHexagon().getCenter();
    Box gameBoard = new Box(
      Math.abs(topLeftCenter.getX() * 2) + 4.5,
      Math.abs(topLeftCenter.getY() * 2) + 3,
      1
    );
    gameBoard.setTranslateZ(-0.5);
    gameBoard.setMaterial(new PhongMaterial(Color.WHITESMOKE));
    add(gameBoard);

    // Add all the renders of the tiles, adding translucent tiles last
    RenderTile t;
    while ((t = tilesToAdd.poll()) != null) add(t);
  }

  /**
   * Sets the selected unit, notifying the listener of the change
   *
   * @param unit new selected unit, can be null if a unit has been deselected
   */
  public void setSelectedUnit(Unit unit) {
    // Check if the unit exists and belongs to the current player (you can't
    // selected other player's units, that would be unfair)
    if (unit != null && unit.player.equals(currentPlayer)) {
      // Check if there's already a selected unit and deselect it if there is
      if (data.selectedUnit != null) {
        data.selectedUnit.tile.selected = false;
        // Rerender the containing tile
        data.selectedUnit.tile.renderer.updateRender();
      }
      // Mark the new unit as selected and rerender the containing tile
      unit.tile.selected = true;
      unit.tile.renderer.updateRender();
      // Update the game state and notify the unit listener
      data.selectedUnit = unit.tile.unit;
      selectedUnitListener.accept(data.selectedUnit);
    } else {
      // Otherwise, if the unit doesn't exist or doesn't belong to the current
      // player, deselect the current unit
      if (data.selectedUnit != null) {
        data.selectedUnit.tile.selected = false;
        // Rerender the containing tile
        data.selectedUnit.tile.renderer.updateRender();
        // Update the game state and notify the unit listener
        data.selectedUnit = null;
        selectedUnitListener.accept(null);
      }
    }
  }

  /**
   * Sets the selected city, notifying the listener of the change. There's no
   * need to check the owner here, as this is done in the click handler.
   *
   * @param city new selected city, can be null if a city has been deselected
   */
  public void setSelectedCity(City city) {
    // Update the game state and notify the city listener
    selectedCity = city;
    selectedCityListener.accept(city);
  }

  /**
   * Deletes a unit from the game, updating the containing tile render
   *
   * @param unit      unit to delete
   * @param broadcast whether to broadcast this change to other clients
   */
  public void deleteUnit(Unit unit, boolean broadcast) {
    if (unit != null) {
      // Deselect this unit if it was the selected unit
      if (data.selectedUnit == unit) setSelectedUnit(null);

      // Remove the unit from the tile and rerender the tile
      unit.tile.unit = null;
      unit.tile.renderer.updateRender();

      // Remove the unit and broadcast the change if required
      data.units.remove(unit);
      if (broadcast) {
        Civilisation.CLIENT.broadcast(new PacketUnitDelete(
          unit.tile.x,
          unit.tile.y
        ));
      }
    }
  }

  /**
   * Requests that all tiles be rerendered following a big game state changed
   * (i.e. city growth)
   */
  public void updateTileRenders() {
    data.hexagonGrid.forEach(
      (gridTile, _hex, _x, _y) -> gridTile.renderer.updateRender()
    );
  }

  /**
   * Start tile of the selected path
   */
  private RenderTile pathStartTile;
  /**
   * End tile of the selected path
   */
  private RenderTile pathEndTile;

  /**
   * Gets a tile render object from a raycast result
   *
   * @param result result of a raycast
   * @return render tile hit by the ray or null if no tile was hit
   */
  private RenderTile getTileFromPickResult(PickResult result) {
    // Check if there even was a selected node
    if (result == null) return null;
    Node node = result.getIntersectedNode();
    if (node == null) return null;

    // Traverse up the tree until a tile render object is found, returning it
    // if it was
    do {
      if (node instanceof RenderTile) return (RenderTile) node;
      node = node.getParent();
    } while (node != null);

    // Otherwise return null if no tile render could be found
    return null;
  }

  /**
   * Reset the path, so that a new path can be drawn in its place
   */
  private void resetPathfindingEnd() {
    if (pathEndTile != null) {
      // Resets the path overlays for the path
      pathEndTile.setOverlayVisible(false);
      data.hexagonGrid.forEach(
        (t, _hex, _x, _y) -> t.renderer.setOverlayVisible(false)
      );
      pathEndTile = null;
    }
  }

  /**
   * Resets the pathfinding state, moving a unit if a path was found. Called
   * when the mouse is released.
   */
  public void resetPathfinding() {
    // Check if a path was found
    if (pathStartTile != null && pathEndTile != null) {
      Path<Tile> path = data.hexagonGrid.findPath(
        pathStartTile.data.x,
        pathStartTile.data.y,
        pathEndTile.data.x,
        pathEndTile.data.y,
        pathStartTile.data.unit.remainingMovementPointsThisTurn
      );
      // Check if the path was valid
      if (path.path.size() > 1) {
        int usedMovementPoints = path.totalCost;

        // Subtract the required movement points, checking the value didn't go
        // below 0
        pathStartTile.data.unit.remainingMovementPointsThisTurn -=
          usedMovementPoints;
        assert pathStartTile.data.unit.remainingMovementPointsThisTurn >= 0;

        // Update the game state of this and other clients
        Civilisation.CLIENT.broadcast(new PacketUnitMove(
          pathStartTile.data.x,
          pathStartTile.data.y,
          pathEndTile.data.x,
          pathEndTile.data.y,
          usedMovementPoints
        ));
        // Move the unit
        pathStartTile.data.unit.tile = pathEndTile.data;
        pathEndTile.data.unit = pathStartTile.data.unit;
        pathStartTile.data.unit = null;

        // Move the unit selection if the unit was selected
        pathEndTile.data.selected = pathStartTile.data.selected;
        pathStartTile.data.selected = false;

        // Update the tile renders to reflect the departure/arrival of units
        pathStartTile.updateRender();
        pathEndTile.updateRender();

        // Notify the selected unit listener of the change too
        selectedUnitListener.accept(data.selectedUnit);
      }
    }

    // Reset the pathfinding overlays
    if (pathStartTile != null) {
      pathStartTile.setOverlayVisible(false);
      pathStartTile = null;
    }
    if (pathEndTile != null) {
      resetPathfindingEnd();
    }
  }

  /**
   * Handle an incoming packet that affects the game state. Updates the
   * updated tiles' renders too.
   *
   * @param packet packet to handle
   * @return a list of tiles that were updated, an empty array if all tiles
   * were updated, or null if no tiles were updated
   */
  public Tile[] handlePacket(Packet packet) {
    // Handle the packet
    Tile[] tilesToUpdate = data.handlePacket(packet);
    // Rerender the required tiles
    if (tilesToUpdate != null) {
      if (tilesToUpdate.length == 0) {
        updateTileRenders();
      } else for (Tile tile : tilesToUpdate) {
        // Check if any units died, removing them if they did
        if (tile.unit != null && tile.unit.isDead()) {
          deleteUnit(tile.unit, false);
        } else {
          tile.renderer.updateRender();
        }
      }
    }
    return tilesToUpdate;
  }
}
