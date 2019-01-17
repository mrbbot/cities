package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.logic.map.tile.Tile;
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

      Point2D center = hex.getCenter();
      String coord = "(" + center.getX() + ", " + center.getY() + ")";
      renderTile.setOnMouseClicked((e) -> {
        //System.out.println("You clicked on the tile at " + coord);

        if(tile.city != null) {
          tile.city.grow(1);
          data.hexagonGrid.forEach((gridTile, _hex, _x, _y) -> gridTile.renderer.updateOverlay());
        }
      });

      renderTile.setOnMouseDragged((e) -> {
        if (e.getButton() == MouseButton.SECONDARY) {
          if (start == null) {
            start = renderTile;
            start.setOverlayVisible(true);
          }

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
      });

      tilesToAdd.add(renderTile);
    });

    Point2D topLeftCenter = data.hexagonGrid.get(0, 0).getHexagon().getCenter();
    Box ground = new Box(Math.abs(topLeftCenter.getX() * 2) + 4.5, Math.abs(topLeftCenter.getY() * 2) + 3, 1);
    ground.setTranslateZ(-0.5);
    ground.setMaterial(new PhongMaterial(Color.WHITESMOKE));
    add(ground);

    RenderTile t;
    while ((t = tilesToAdd.poll()) != null) add(t);
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
    if (start != null) {
      start.setOverlayVisible(false);
      start = null;
    }
    if (end != null) {
      resetPathfindingEnd();
    }
  }
}
