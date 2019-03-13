package com.mrbbot.civilisation.geometry;

import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a 2D grid of hexagons and handles the maths for positioning
 * hexagons relative to each other
 *
 * @param <E> the type of the data associated with each hexagon (should
 *            implement {@link Traversable} for pathfinding)
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
   * 2D array for storing the data associated with each tile. Objects within
   * this array should be of type E. Every other tile has +-1 element than the
   * previous row.
   */
  private final Object[][] grid;
  /**
   * 2D array for storing the hexagons which contain the data about tile
   * positioning for this grid. Every other tile has +-1 element than the
   * previous row.
   */
  private final Hexagon[][] hexagonGrid;

  /**
   * The distance from the midpoint of one edge to another midpoint of a
   * hexagon
   */
  private final double cw;    // cell width
  /**
   * The distance from the center of a hexagon to the midpoint of an edge of
   * the hexagon
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
  @SuppressWarnings("unused")
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

    // Construct the grid arrays taking into account the extra element on
    // alternating rows
    grid = new Object[height][width + 1];
    hexagonGrid = new Hexagon[height][width + 1];

    // Calculate constants for the grid that are used when laying out the
    // hexagons
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
   * Iterates through every position for a hexagon and creates a new
   * {@link Hexagon} for that position.
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
   * Determines whether a cell actually exists in the hexagon grid, taking into
   * account alternating numbers of elements on each row
   *
   * @param x x-coordinate of cell to check
   * @param y y-coordinate of cell to check
   * @return whether the cell exists
   */
  private boolean cellExists(int x, int y) {
    return 0 <= x && x < grid[0].length
      && 0 <= y && y < grid.length
      && !(y % 2 == 0 && x > grid[0].length - 2);
  }

  /**
   * Checks whether the specified neighbouring cell exists, sometimes taking
   * into account the traversability of the cell.
   *
   * @param x             x-coordinate of neighbouring cell to check
   * @param y             y-coordinate of neighbouring cell to check
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return whether the neighbouring cell exists
   */
  private boolean checkNeighbour(int x, int y, boolean checkTraverse) {
    return cellExists(x, y) && (!checkTraverse || get(x, y).canTraverse());
  }

  /**
   * Gets the data associated with the cell at the coordinate
   *
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

  /**
   * Gets the data associated with a cell called in the context of an adjacency
   * check
   *
   * @param x             x-coordinate of cell
   * @param y             y-coordinate of cell
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @param dx            offset added to the x-coordinate of the cell
   * @return the data associated with the cell or null if the cell doesn't
   * exist
   */
  private E getAdjacent(int x, int y, boolean checkTraverse, int dx) {
    // Check if the cell exists, if it does return it, otherwise return null
    return checkNeighbour(x + dx, y, checkTraverse)
      ? get(x + dx, y)
      : null;
  }

  /**
   * Gets the cell to the top-left of the specified coordinate
   *
   * @param x             x-coordinate to check to the top-left of
   * @param y             y-coordinate to check to the top-left of
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return the data associated with the top-left or null if the cell doesn't
   * exist
   */
  public E getTopLeft(int x, int y, boolean checkTraverse) {
    return getAdjacent(x, y - 1, checkTraverse, -(y % 2));
  }

  /**
   * Gets the cell to the top-right of the specified coordinate
   *
   * @param x             x-coordinate to check to the top-right of
   * @param y             y-coordinate to check to the top-right of
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return the data associated with the top-right or null if the cell doesn't
   * exist
   */
  public E getTopRight(int x, int y, boolean checkTraverse) {
    return getAdjacent(x + 1, y - 1, checkTraverse, -(y % 2));
  }

  /**
   * Gets the cell to the left of the specified coordinate
   *
   * @param x             x-coordinate to check to the left of
   * @param y             y-coordinate to check to the left of
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return the data associated with the left or null if the cell doesn't
   * exist
   */
  public E getLeft(int x, int y, boolean checkTraverse) {
    return getAdjacent(x - 1, y, checkTraverse, 0);
  }

  /**
   * Gets the cell to the right of the specified coordinate
   *
   * @param x             x-coordinate to check to the right of
   * @param y             y-coordinate to check to the right of
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return the data associated with the right or null if the cell doesn't
   * exist
   */
  public E getRight(int x, int y, boolean checkTraverse) {
    return getAdjacent(x + 1, y, checkTraverse, 0);
  }

  /**
   * Gets the cell to the bottom-left of the specified coordinate
   *
   * @param x             x-coordinate to check to the bottom-left of
   * @param y             y-coordinate to check to the bottom-left of
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return the data associated with the bottom-left or null if the cell
   * doesn't exist
   */
  public E getBottomLeft(int x, int y, boolean checkTraverse) {
    return getAdjacent(x, y + 1, checkTraverse, -(y % 2));
  }

  /**
   * Gets the cell to the bottom-right of the specified coordinate
   *
   * @param x             x-coordinate to check to the bottom-right of
   * @param y             y-coordinate to check to the bottom-right of
   * @param checkTraverse whether to check if the cell is traversable or not
   *                      (used for pathfinding)
   * @return the data associated with the bottom-right or null if the cell
   * doesn't exist
   */
  public E getBottomRight(int x, int y, boolean checkTraverse) {
    return getAdjacent(x + 1, y + 1, checkTraverse, -(y % 2));
  }

  /**
   * Gets a list of the neighbouring cells to the specified coordinate
   *
   * @param x             x-coordinate to get neighbours of
   * @param y             y-coordinate to get neighbours of
   * @param checkTraverse whether to check if the cells are traversable or not
   *                      (used for pathfinding)
   * @return ArrayList of the neighbours' data
   */
  public ArrayList<E> getNeighbours(int x, int y, boolean checkTraverse) {
    // Create an empty list to add to
    ArrayList<E> list = new ArrayList<>();

    // Get the adjacent cells
    E topLeft = getTopLeft(x, y, checkTraverse);
    E topRight = getTopRight(x, y, checkTraverse);
    E left = getLeft(x, y, checkTraverse);
    E right = getRight(x, y, checkTraverse);
    E bottomLeft = getBottomLeft(x, y, checkTraverse);
    E bottomRight = getBottomRight(x, y, checkTraverse);

    // Add the cells to the list if they aren't null
    if (topLeft != null) list.add(topLeft);
    if (topRight != null) list.add(topRight);
    if (left != null) list.add(left);
    if (right != null) list.add(right);
    if (bottomLeft != null) list.add(bottomLeft);
    if (bottomRight != null) list.add(bottomRight);

    // Return the list
    return list;
  }

  /**
   * Finds the shortest path between (x1, y1) and (x2, y2) that does not exceed
   * maxCost using Dijkstra's algorithm. If maxCost is exceeded, the function
   * returns the path up until the cost is exceeded.
   *
   * @param x1      start x-coordinate
   * @param y1      start y-coordinate
   * @param x2      end x-coordinate
   * @param y2      end y-coordinate
   * @param maxCost max cost of the path
   * @return a path object containing information of the tiles in the path and
   * the total cost of the path
   */
  public Path<E> findPath(int x1, int y1, int x2, int y2, int maxCost) {
    // Create a map for storing the cost of certain tiles for sorting the queue
    Map<E, Integer> costs = new HashMap<>();
    // Create the queue for the frontier, sorting elements by their cost so the
    // cheapest elements are at the front
    PriorityQueue<E> frontier =
      new PriorityQueue<>(Comparator.comparingInt(costs::get));

    // Create a map for storing the path back to the beginning
    Map<E, E> cameFrom = new HashMap<>();
    // Create a map for storing the costs of the path to a cell so far
    Map<E, Integer> costSoFar = new HashMap<>();

    // Get the start/end of the path
    E start = get(x1, y1);
    E goal = get(x2, y2);

    // Set the cost of the start to 0 and add it to the queue
    costs.put(start, 0);
    frontier.add(start);
    cameFrom.put(start, null);
    costSoFar.put(start, 0);

    // While there are still tiles to explore...
    while (!frontier.isEmpty()) {
      // Get the cheapest tile
      E current = frontier.remove();
      // Check if this is the goal or doesn't exist
      if (current == null || current == goal) break;
      // For every neighbour of the current tile... (making sure that the
      // neighbour is traversable)
      for (E next : getNeighbours(
        current.getX(),
        current.getY(),
        true
      )) {
        // Calculate the cost of getting to this tile
        int newCost = costSoFar.get(current) + next.getCost();
        // If this is a new tile or the cost of using this route is cheaper
        if (!cameFrom.containsKey(next) || newCost < costSoFar.get(next)) {
          // Store the more efficient path
          costSoFar.put(next, newCost);
          costs.put(next, newCost);
          frontier.add(next);
          cameFrom.put(next, current);
        }
      }
    }

    // Build the path list of tiles travelled
    List<E> path = new ArrayList<>();
    E current = goal;
    while (current != null) {
      path.add(current);
      current = cameFrom.get(current);
    }
    // Reverse the order of this path so the start is at the beginning and the
    // end is at the end
    Collections.reverse(path);

    // Make sure the path doesn't exceed the max cost
    int travelledCost = 0;
    int lastListIndex = 1;
    while (travelledCost < maxCost && lastListIndex < path.size()) {
      travelledCost += path.get(lastListIndex).getCost();
      lastListIndex++;
    }
    // Get the path up to the point where the max cost is exceeded
    path = path.subList(0, lastListIndex);

    // Return a path object with details on the path
    return new Path<>(path, travelledCost);
  }

  /**
   * Sets the data associated with the cell at the coordinate
   *
   * @param x    x-coordinate of cell
   * @param y    y-coordinate of cell
   * @param cell data associated with the cell
   */
  public void set(int x, int y, E cell) {
    assert cellExists(x, y);
    grid[y][x] = cell;
  }

  /**
   * Gets the hexagon (with position data) associated with the cell at the
   * coordinate
   *
   * @param x x-coordinate of cell
   * @param y y-coordinate of cell
   * @return {@link Hexagon} associated with the cell
   */
  public Hexagon getHexagon(int x, int y) {
    assert cellExists(x, y);
    return hexagonGrid[y][x];
  }

  /**
   * Iterates through all possible hexes in the hexagon grid and calls the
   * consumer with data for each cell
   *
   * @param consumer function to be called with details about the cell
   */
  @SuppressWarnings("unchecked")
  public void forEach(HexagonConsumer<E> consumer) {
    // For every row...
    for (int y = 0; y < grid.length; y++) {
      // For every column... (taking into account the alternating numbers of
      // columns in rows)
      for (int x = 0; x < grid[0].length - ((y + 1) % 2); x++) {
        // Send the details on the cell
        consumer.accept((E) grid[y][x], hexagonGrid[y][x], x, y);
      }
    }
  }

  /**
   * Implementation of {@link Iterator} for iterating over the cells in a
   * hexagon grid
   */
  private class HexagonGridIterator implements Iterator<E> {
    /**
     * Current x-coordinate state of the iterator
     */
    private int x;
    /**
     * Current y-coordinate state of the iterator
     */
    private int y;

    /**
     * Constructor for iterator. Initialises the state to (0, 0).
     */
    private HexagonGridIterator() {
      this.x = 0;
      this.y = 0;
    }

    /**
     * Checks if the iterator has another cell to give
     *
     * @return whether the current state of the iterator is valid
     */
    @Override
    public boolean hasNext() {
      return cellExists(x, y);
    }

    /**
     * Gets the next cell and increments the iterator state
     *
     * @return cell currently pointed to by the iterator
     */
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

  /**
   * Creates a new {@link HexagonGridIterator} for this hexagon grid
   *
   * @return the created iterator
   */
  public Iterator<E> iterator() {
    return new HexagonGridIterator();
  }

  @Override
  public String toString() {
    // Builder for the output
    StringBuilder builder = new StringBuilder();
    // For every row...
    for (int y = 0; y < grid.length; y++) {
      // Padding the row if needed to make a hexagon grid shape in the string
      if (y % 2 == 0) {
        builder.append(" ");
      }
      // For every column...
      for (int x = 0; x < grid[0].length - ((y + 1) % 2); x++) {
        // Set the output depending on whether the tile exists
        builder.append(get(x, y) == null ? "-" : "#").append(" ");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  /**
   * Gets the grid width of this hexagon grid (how many tiles the grid spans)
   *
   * @return grid width of this hexagon grid
   */
  public int getWidth() {
    return width;
  }

  /**
   * Gets the grid height of this hexagon grid (how many tiles the grid spans)
   *
   * @return grid height this hexagon grid
   */
  public int getHeight() {
    return height;
  }
}
