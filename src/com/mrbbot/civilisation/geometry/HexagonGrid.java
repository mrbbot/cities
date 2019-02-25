package com.mrbbot.civilisation.geometry;

import com.mrbbot.civilisation.logic.interfaces.Traversable;
import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a 2D grid a hexagons and handles the maths for positioning hexagons relative to each other
 *
 * @param <E> the type of the data associated with each hexagon (should implement
 *            {@link com.mrbbot.civilisation.logic.interfaces.Traversable} for pathfinding)
 */
public class HexagonGrid<E extends Traversable> implements Serializable {
  /**
   * Grid width of the hexagon grid (how many tiles the grid spans)
   */
  private final int width;
  /**
   * Grid height of the hexagon grid (how many tiles the grid spans)
   */
  private final int height;
  /**
   * Radius of each hexagon within the hexagon grid
   */
  private final double radius;

  /**
   * 2D array for storing the data associated with each tile. Objects within this array should be of type E. Every
   * other tile has +-1 element than the previous row.
   */
  private final Object[][] grid;
  /**
   * 2D array for storing the hexagons which contain the data about tile positioning for this grid. Every other tile
   * has +-1 element than the previous row.
   */
  private final Hexagon[][] hexagonGrid;

  /**
   * The distance from the midpoint of one edge to another midpoint of a hexagon
   */
  private final double cw;    // cell width
  /**
   * The distance from the center of a hexagon to the midpoint of an edge of the hexagon
   */
  private final double hcw;   // half cell width
  /**
   * The vertical distance between the center of hexagons on adjacent rows
   */
  private final double ch;    // cell height
  /**
   * The x-coordinate of where the grid starts relative to the origin
   */
  private final double sx;    // start x
  /**
   * The y-coordinate of where the grid starts relative to the origin
   */
  private final double sy;    // start y

  /**
   * Creates a new Hexagon Grid using the default radius of 1
   *
   * @param width  grid width of the grid
   * @param height grid height of the grid
   */
  public HexagonGrid(int width, int height) {
    // Call the other constructor with the default
    this(width, height, 1);
  }

  /**
   * Creates a new Hexagon Grid
   *
   * @param width  grid width of the grid
   * @param height grid height of the grid
   * @param radius radius of each hexagon within the hexagon grid
   */
  public HexagonGrid(int width, int height, double radius) {
    // Initialise class fields
    this.width = width;
    this.height = height;
    this.radius = radius;

    // Construct the grid arrays taking into account the extra element on alternating rows
    grid = new Object[height][width + 1];
    hexagonGrid = new Hexagon[height][width + 1];

    // Calculate constants for the grid that are used when laying out the hexagons
    cw = Hexagon.SQRT_3 * radius;    // cell width
    hcw = cw / 2;                    // half cell width
    ch = 3.0 / 2.0 * radius;         // cell height
    double gw = (width - 1.0) * cw;  // grid width
    double gh = (height - 1.0) * ch; // grid height
    sx = -gw / 2;                    // start x
    sy = gh / 2;                     // start y

    // Calculate the positions of the hexagons on the grid
    calculateHexagonGrid();
  }

  /**
   * Iterates through every position for a hexagon and creates a new {@link Hexagon} for that position.
   */
  private void calculateHexagonGrid() {
    forEach((e, hex, x, y) -> hexagonGrid[y][x] = new Hexagon(
      // Center of the hexagon
      new Point2D(
        // From the start coordinates, add the extra for this position
        sx + (cw * x) - ((y % 2) * hcw),
        sy - (ch * y)
      ),
      // Use the specified radius for hexagons
      radius
    ));
  }

  /**
   * Determines whether a cell actually exists in the hexagon grid, taking into account alternating numbers of
   * elements on each row
   * @param x x-coordinate of cell to check
   * @param y y-coordinate of cell to check
   * @return whether the cell exists
   */
  private boolean cellExists(int x, int y) {
    return 0 <= x && x < grid[0].length && 0 <= y && y < grid.length && !(y % 2 == 0 && x > grid[0].length - 2);
  }

  /**
   * Checks whether the specified neighbouring cell exists, sometimes taking into account the traversability of the
   * cell.
   * @param x x-coordinate of neighbouring cell to check
   * @param y y-coordinate of neighbouring cell to check
   * @param checkTraverse whether to check if the cell is traversable or not (used for pathfinding)
   * @return whether the neighbouring cell exists
   */
  private boolean checkNeighbour(int x, int y, boolean checkTraverse) {
    return cellExists(x, y) && (!checkTraverse || get(x, y).canTraverse());
  }

  /**
   * Gets the data associated with the cell at the coordinate
   * @param x x-coordinate of cell
   * @param y y-coordinate of cell
   * @return data associated with the cell
   */
  @SuppressWarnings("unchecked")
  public E get(int x, int y) {
    // Ensure the cell actually exists in the grid
    assert cellExists(x, y);
    // Return the data, casting it to the data type
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
    while (travelledCost < maxCost && lastListIndex < path.size()) {
      travelledCost += path.get(lastListIndex).getCost();
      lastListIndex++;
    }
    path = path.subList(0, lastListIndex);

    return new Path<>(path, travelledCost);
  }

  public void set(int x, int y, E cell) {
    assert cellExists(x, y);
    grid[y][x] = cell;
  }

  public Hexagon getHexagon(int x, int y) {
    assert cellExists(x, y);
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

  private class HexagonGridIterator implements Iterator<E> {
    private int x, y;

    private HexagonGridIterator() {
      this.x = 0;
      this.y = 0;
    }

    @Override
    public boolean hasNext() {
      return cellExists(x, y);
    }

    @Override
    public E next() {
      E next = get(x, y);
      if (x < grid[0].length - ((y + 1) % 2) - 1) {
        x++;
      } else {
        x = 0;
        y++;
      }
      return next;
    }
  }

  public Iterator<E> iterator() {
    return new HexagonGridIterator();
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
