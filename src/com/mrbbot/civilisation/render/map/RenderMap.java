package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.net.packet.PacketUnitMove;
import com.mrbbot.civilisation.net.serializable.SerializablePoint2D;
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

public class RenderMap extends RenderData<Map> {
  public RenderMap(Map data) {
    super(data);

    final PriorityQueue<RenderTile> tilesToAdd = new PriorityQueue<>((a, b) -> {
      if (a.isTranslucent() && !b.isTranslucent()) return 1;
      else if (b.isTranslucent() && !a.isTranslucent()) return -1;
      else return 0;
    });

    data.hexagonGrid.forEach((tile, hex, x, y) -> {
      RenderTile renderTile = new RenderTile(tile);
      tile.renderer = renderTile;

      SerializablePoint2D center = hex.getCenter();
      String coord = "(" + center.getX() + ", " + center.getY() + ")";
      renderTile.setOnMouseClicked((e) -> {
        //System.out.println("You clicked on the tile at " + coord);

        if(tile.city != null) {
          if(e.getButton() == MouseButton.PRIMARY) {
            tile.city.grow(1);
            updateTileRenders();
          } else if(e.getButton() == MouseButton.SECONDARY) {
            if(tile.improvement == Improvement.NONE) {
              tile.setImprovement(Improvement.FARM);
            } else if(tile.improvement == Improvement.FARM) {
              tile.setImprovement(Improvement.NONE);
            }
          }
        }
      });

      renderTile.setOnMouseDragged((e) -> {
        if (e.getButton() == MouseButton.SECONDARY) {
          if(start == null && renderTile.data.unit != null) {
            start = renderTile;
            start.setOverlayVisible(true);
          }

          if (start != null) {
            RenderTile pickedTile = getTileFromPickResult(e.getPickResult());
            if ((pickedTile == null || pickedTile != end) && end != null) {
              resetPathfindingEnd();
            }
            if (pickedTile != null) {
              end = pickedTile;
              end.setOverlayVisible(true);

              List<Tile> path = data.hexagonGrid.findPath(start.data.x, start.data.y, end.data.x, end.data.y);
              path.forEach((p) -> p.renderer.setOverlayVisible(true));
            }
          }
        }
      });

      tilesToAdd.add(renderTile);
    });

    SerializablePoint2D topLeftCenter = data.hexagonGrid.get(0, 0).getHexagon().getCenter();
    Box ground = new Box(Math.abs(topLeftCenter.getX() * 2) + 4.5, Math.abs(topLeftCenter.getY() * 2) + 3, 1);
    ground.setTranslateZ(-0.5);
    ground.setMaterial(new PhongMaterial(Color.WHITESMOKE));
    add(ground);

    RenderTile t;
    while ((t = tilesToAdd.poll()) != null) add(t);
  }

  private void updateTileRenders() {
    data.hexagonGrid.forEach((gridTile, _hex, _x, _y) -> gridTile.renderer.updateRender());
  }

  private RenderTile start, end;

  private RenderTile getTileFromPickResult(PickResult result) {
    if (result == null) return null;
    Node node = result.getIntersectedNode();
    if (node == null) return null;

    do {
      if (node instanceof RenderTile) return (RenderTile) node;
      node = node.getParent();
    } while (node != null);

    return null;
  }

  private void resetPathfindingEnd() {
    if(end != null) {
      end.setOverlayVisible(false);
      data.hexagonGrid.forEach((t, _hex, _x, _y) -> t.renderer.setOverlayVisible(false));
      end = null;
    }
  }

  public void resetPathfinding() {
    if(start != null && end != null) {
      List<Tile> path = data.hexagonGrid.findPath(start.data.x, start.data.y, end.data.x, end.data.y);
      if(path.size() > 1) {
        Civilisation.CLIENT.broadcast(new PacketUnitMove(start.data.x, start.data.y, end.data.x, end.data.y));
        start.data.unit.tile = end.data;
        end.data.unit = start.data.unit;
        start.data.unit = null;
        start.updateRender();
        end.updateRender();
      }
    }

    if (start != null) {
      start.setOverlayVisible(false);
      start = null;
    }
    if (end != null) {
      resetPathfindingEnd();
    }
  }

  public void handleUnitMove(PacketUnitMove packet) {
    Tile startTile = data.hexagonGrid.get(packet.startX, packet.startY);
    Tile endTile = data.hexagonGrid.get(packet.endX, packet.endY);

    startTile.unit.tile = endTile;
    endTile.unit = startTile.unit;
    startTile.unit = null;

    startTile.renderer.updateRender();
    endTile.renderer.updateRender();
  }
}
