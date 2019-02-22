package com.mrbbot.civilisation.geometry;

import com.mrbbot.civilisation.logic.interfaces.Traversable;
import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.*;

public class HexagonGrid<E extends Traversable> implements Serializable {
  private final int width, height;
  private final double radius;

  private final Object[][] grid;
  private final Hexagon[][] hexagonGrid;

  private final double cw;    // cell width
  private final double hcw;   // half cell width
  private final double ch;    // cell height
  private final double gw;    // grid width
  private final double gh;    // grid height
  private final double sx;    // start x
  private final double sy;    // start y

  public HexagonGrid(int width, int height) {
    this(width, height, 1);
  }

  public HexagonGrid(int width, int height, double radius) {
    this.width = width;
    this.height = height;
    this.radius = radius;
    grid = new Object[height][width + 1];
    hexagonGrid = new Hexagon[height][width + 1];

    cw = Hexagon.SQRT_3 * radius;    // cell width
    hcw = cw / 2;                    // half cell width
    ch = 3.0 / 2.0 * radius;         // cell height
    gw = (width - 1.0) * cw;         // grid width
    gh = (height - 1.0) * ch;        // grid height
    sx = -gw / 2;                    // start x
    sy = gh / 2;                     // start y

    calculateHexagonGrid();
  }

  private void calculateHexagonGrid() {
    forEach((e, hex, x, y) -> hexagonGrid[y][x] = new Hexagon(new Point2D(sx + (cw * x) - ((y % 2) * hcw), sy - (ch * y)), radius));
  }

  private boolean cellExists(int x, int y) {
    return 0 <= x && x < grid[0].length && 0 <= y && y < grid.length && !(y % 2 == 0 && x > grid[0].length - 2);
  }

  private void checkCell(int x, int y) {
    if (!cellExists(x, y)) {
      throw new IndexOutOfBoundsException();
    }
  }

  private boolean checkNeighbour(int x, int y, boolean checkTraverse) {
    return cellExists(x, y) && (!checkTraverse || get(x, y).canTraverse());
  }

  @SuppressWarnings("unchecked")
  public E get(int x, int y) {
    checkCell(x, y);
    return (E) grid[y][x];
  }

  private E getAdjacent(int x, int y, boolean checkTraverse, int dx) {
    return checkNeighbour(x + dx, y, checkTraverse) ? get(x + dx, y) : null;
  }

  public E getTopLeft(int x, int y, boolean checkTraverse) {
    return getAdjacent(x, y - 1, checkTraverse, -(y % 2));
  }

  public E getTopRight(int x, int y, boolean checkTraverse) {
    return getAdjacent(x + 1, y - 1, checkTraverse, -(y % 2));
  }

  public E getLeft(int x, int y, boolean checkTraverse) {
    return getAdjacent(x - 1, y, checkTraverse, 0);
  }

  public E getRight(int x, int y, boolean checkTraverse) {
    return getAdjacent(x + 1, y, checkTraverse, 0);
  }

  public E getBottomLeft(int x, int y, boolean checkTraverse) {
    return getAdjacent(x, y + 1, checkTraverse, -(y % 2));
  }

  public E getBottomRight(int x, int y, boolean checkTraverse) {
    return getAdjacent(x + 1, y + 1, checkTraverse, -(y % 2));
  }

  public ArrayList<E> getNeighbours(int x, int y, boolean checkTraverse) {
    ArrayList<E> list = new ArrayList<>();

    E topLeft = getTopLeft(x, y, checkTraverse);
    E topRight = getTopRight(x, y, checkTraverse);
    E left = getLeft(x, y, checkTraverse);
    E right = getRight(x, y, checkTraverse);
    E bottomLeft = getBottomLeft(x, y, checkTraverse);
    E bottomRight = getBottomRight(x, y, checkTraverse);

    if (topLeft != null) list.add(topLeft);
    if (topRight != null) list.add(topRight);
    if (left != null) list.add(left);
    if (right != null) list.add(right);
    if (bottomLeft != null) list.add(bottomLeft);
    if (bottomRight != null) list.add(bottomRight);

    return list;
  }

  public Path<E> findPath(int x1, int y1, int x2, int y2, int maxCost) {
    Map<E, Integer> costs = new HashMap<>();
    PriorityQueue<E> frontier = new PriorityQueue<>(Comparator.comparingInt(costs::get));

    Map<E, E> cameFrom = new HashMap<>();
    Map<E, Integer> costSoFar = new HashMap<>();

    E start = get(x1, y1);
    E goal = get(x2, y2);

    costs.put(start, 0);
    frontier.add(start);

    cameFrom.put(start, null);
    costSoFar.put(start, 0);

    while (!frontier.isEmpty()) {
      E current = frontier.remove();
      if (current == null || current == goal) break;
      for (E next : getNeighbours(current.getX(), current.getY(), true)) {
        int newCost = costSoFar.get(current) + next.getCost();
        if (!cameFrom.containsKey(next) || newCost < costSoFar.get(next)) {
          costSoFar.put(next, newCost);
          costs.put(next, newCost);
          frontier.add(next);
          cameFrom.put(next, current);
        }
      }
    }

    List<E> path = new ArrayList<>();
    E current = goal;
    while (current != null) {
      path.add(current);
      current = cameFrom.get(current);
    }

    Collections.reverse(path);
    int travelledCost = 0;
    int lastListIndex = 1;
    while(travelledCost < maxCost && lastListIndex < path.size()) {
      travelledCost += path.get(lastListIndex).getCost();
      lastListIndex++;
    }
    path = path.subList(0, lastListIndex);

    return new Path<>(path, travelledCost);
  }

  public void set(int x, int y, E cell) {
    checkCell(x, y);
    grid[y][x] = cell;
  }

  public Hexagon getHexagon(int x, int y) {
    checkCell(x, y);
    return hexagonGrid[y][x];
  }

  @SuppressWarnings("unchecked")
  public void forEach(HexagonConsumer<E> consumer) {
    for (int y = 0; y < grid.length; y++) {
      for (int x = 0; x < grid[0].length - ((y + 1) % 2); x++) {
        consumer.accept((E) grid[y][x], hexagonGrid[y][x], x, y);
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int y = 0; y < grid.length; y++) {
      if (y % 2 == 0) {
        builder.append(" ");
      }
      for (int x = 0; x < grid[0].length - ((y + 1) % 2); x++) {
        builder.append(get(x, y) == null ? "-" : "#").append(" ");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
