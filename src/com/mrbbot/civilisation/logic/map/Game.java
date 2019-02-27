package com.mrbbot.civilisation.logic.map;

import com.mrbbot.civilisation.geometry.Hexagon;
import com.mrbbot.civilisation.geometry.HexagonGrid;
import com.mrbbot.civilisation.logic.CityBuildable;
import com.mrbbot.civilisation.logic.Living;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.PlayerStats;
import com.mrbbot.civilisation.logic.Mappable;
import com.mrbbot.civilisation.logic.TurnHandler;
import com.mrbbot.civilisation.logic.techs.Unlockable;
import com.mrbbot.civilisation.logic.map.tile.Building;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Terrain;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.civilisation.logic.techs.Tech;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.net.ServerOnly;
import javafx.geometry.Point2D;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Main game logic class. Connects other parts of game logic together.
 */
public class Game implements Mappable, TurnHandler {
  /**
   * Message shown to user if someone wins by putting their cities on every
   * single tile
   */
  private static final String VICTORY_REASON_DOMINATION
    = "conquering every single tile";
  /**
   * Message shown to user if someone wins by researching, building and
   * launching a rocket unit
   */
  private static final String VICTORY_REASON_SCIENCE
    = "blasting off into space";

  /**
   * Name of the game, chosen by the user on initial create
   */
  private String name;
  /**
   * Hexagon grid of the game, used for positioning tiles
   */
  public HexagonGrid<Tile> hexagonGrid;
  /**
   * List of all the cities in the game (regardless of player)
   */
  public ArrayList<City> cities;
  /**
   * List of all the units in the game (regardless of player)
   */
  public ArrayList<Unit> units;
  /**
   * List of all the players in the game
   */
  public ArrayList<Player> players;
  /**
   * Total amount of science each player has. Maps players' ids to science
   * counts.
   */
  private Map<String, Integer> playerScienceCounts;
  /**
   * Total amount of gold each player has. Maps players' ids to gold counts.
   */
  private Map<String, Integer> playerGoldCounts;
  /**
   * Techs unlocked by each player. Map players' ids to sets of unlocked techs.
   * Sets are used as each technology can only be unlocked once by each player.
   */
  private Map<String, Set<Tech>> playerUnlockedTechs;
  /**
   * Techs currently being unlocked by each player. Maps players' ids to techs
   * currently being unlocked.
   */
  private Map<String, Tech> playerUnlockingTechs;
  /**
   * Players that have marked themselves as ready. Maps players' ids to their
   * ready state (true = ready).
   */
  @ServerOnly
  public Map<String, Boolean> readyPlayers = new HashMap<>();
  /**
   * The currently selected unit. Units can be selected by clicking on them.
   */
  @ClientOnly
  public Unit selectedUnit = null;
  /**
   * Whether the game is waiting for other players to click the ready button.
   * This is set to true when the user clicks the "Next Turn" button.
   */
  @ClientOnly
  public boolean waitingForPlayers = false;
  /**
   * The ID of the current player.
   */
  @ClientOnly
  private String currentPlayerId;
  /**
   * A function to be called whenever the current player's stats (gold/science
   * counts) change.
   */
  @ClientOnly
  private Consumer<PlayerStats> playerStatsListener;
  /**
   * A function to be called whenever the current player's tech state changes.
   * This may be progress in researching a
   * technology or the actual unlocking of a technology.
   */
  @ClientOnly
  private Consumer<PlayerTechDetails> techDetailsListener;
  /**
   * A function to be called whenever a message is to be sent to the user. On
   * the client, this is handled by displaying a message box.
   * <p>
   * The first parameter is the message to be displayed, and the second is
   * whether the message is an error or not (true = error).
   */
  @ClientOnly
  private BiConsumer<String, Boolean> messageListener;

  /**
   * Constructor for a new game (not loaded from a file)
   *
   * @param name    name of the game
   * @param mapSize size of the game (contains information on width/height)
   */
  public Game(String name, MapSize mapSize) {
    // Store the name
    this.name = name;

    // Create the hexagon grid
    hexagonGrid = new HexagonGrid<>(mapSize.width, mapSize.height, 1);
    // Create a tile object for every tile in the grid
    hexagonGrid.forEach((_tile, hex, x, y) ->
      hexagonGrid.set(x, y, new Tile(hex, x, y))
    );

    // Create empty lists
    cities = new ArrayList<>();
    units = new ArrayList<>();
    players = new ArrayList<>();

    // Create empty maps
    playerScienceCounts = new HashMap<>();
    playerGoldCounts = new HashMap<>();
    playerUnlockedTechs = new HashMap<>();
    playerUnlockingTechs = new HashMap<>();
  }

  /**
   * Constructor for a game (loaded from a file/over the network)
   *
   * @param map map containing details of game
   */
  public Game(Map<String, Object> map) {
    // Load the name of the game
    this.name = (String) map.get("name");

    // Load the player list
    //noinspection unchecked
    this.players = (ArrayList<Player>) ((List<String>) map.get("players"))
      .stream()
      // Create a new player for each player id
      .map(Player::new)
      .collect(Collectors.toList());

    // Load the terrain
    //noinspection unchecked
    List<List<Double>> terrainList = (List<List<Double>>) map.get("terrain");
    //noinspection unchecked
    List<List<Integer>> treeList = (List<List<Integer>>) map.get("trees");
    int height = terrainList.size();
    for (int y = 0; y < height; y++) {
      List<Double> terrainRow = terrainList.get(y);
      List<Integer> treeRow = treeList.get(y);
      int width = terrainRow.size();

      // If the hexagon grid hasn't been initialised yet, we now have all the
      // information required to make one
      if (hexagonGrid == null) {
        hexagonGrid = new HexagonGrid<>(width, height, 1);
      }

      for (int x = 0; x < width; x++) {
        double terrainHeight = terrainRow.get(x);
        boolean tree = treeRow.get(x) == 1;

        // Put the tile into the grid with the loaded height and tree state
        hexagonGrid.set(x, y, new Tile(
          hexagonGrid.getHexagon(x, y),
          x, y,
          terrainHeight, tree
        ));
      }
    }

    // Make sure the hexagon grid has been set
    assert hexagonGrid != null;

    // Load the cities
    //noinspection unchecked
    this.cities = (ArrayList<City>)
      ((List<Map<String, Object>>) map.get("cities"))
        .stream()
        .map(m -> new City(hexagonGrid, m))
        .collect(Collectors.toList());

    // Load the units
    //noinspection unchecked
    this.units = (ArrayList<Unit>)
      ((List<Map<String, Object>>) map.get("units"))
        .stream()
        .map(m -> new Unit(hexagonGrid, m))
        .collect(Collectors.toList());

    // Load player resource counts
    //noinspection unchecked
    this.playerScienceCounts = (Map<String, Integer>) map.get("science");
    //noinspection unchecked
    this.playerGoldCounts = (Map<String, Integer>) map.get("gold");

    // Load player unlocked techs. These are stored as strings by default not
    // tech names so have to be converted.
    //noinspection unchecked
    Map<String, ArrayList<String>> unlockedTechs =
      (Map<String, ArrayList<String>>) map.get("unlockedTechs");
    this.playerUnlockedTechs = new HashMap<>();
    for (Map.Entry<String, ArrayList<String>> e : unlockedTechs.entrySet()) {
      // Convert the list of strings to a list of techs
      ArrayList<Tech> list = (ArrayList<Tech>) e.getValue()
        .stream()
        .map(Tech::fromName)
        .collect(Collectors.toList());

      // Store these techs in a set to ensure uniqueness
      this.playerUnlockedTechs.put(
        e.getKey(),
        new HashSet<>(list)
      );
    }

    // Load player unlocking techs. Again techs are stored as strings so have
    // to be converted.
    //noinspection unchecked
    Map<String, String> unlockingTechs =
      (Map<String, String>) map.get("unlockingTechs");
    this.playerUnlockingTechs = new HashMap<>();
    for (Map.Entry<String, String> e : unlockingTechs.entrySet()) {
      // Store the tech, converting the name to a recognised tech object
      this.playerUnlockingTechs.put(e.getKey(), Tech.fromName(e.getValue()));
    }
  }

  /**
   * Sets the current player of the game. Also registers the player stats
   * listener so the interface can be updated.
   *
   * @param currentPlayerId     id of the current player
   * @param playerStatsListener function to be called when player stats change
   */
  @ClientOnly
  public void setCurrentPlayer(
    String currentPlayerId,
    Consumer<PlayerStats> playerStatsListener
  ) {
    this.currentPlayerId = currentPlayerId;
    this.playerStatsListener = playerStatsListener;
    // Send the initial player stats straight away
    sendPlayerStats();
  }

  /**
   * Registers the tech details listener.
   *
   * @param techDetailsListener function to be called when tech progress
   *                            changes
   */
  @ClientOnly
  public void setTechDetailsListener(
    Consumer<PlayerTechDetails> techDetailsListener
  ) {
    this.techDetailsListener = techDetailsListener;
    // Send the initial tech details straight away
    sendTechDetails();
  }

  /**
   * Registers the listener for messages
   *
   * @param messageListener function to be called when a message is sent
   *                        (1st parameter: message, 2nd parameter: isError)
   */
  @ClientOnly
  public void setMessageListener(BiConsumer<String, Boolean> messageListener) {
    this.messageListener = messageListener;
  }

  /**
   * Sends a message to the client if there is one
   *
   * @param message message to be sent
   * @param isError whether this message represents an error
   */
  private void sendMessage(String message, boolean isError) {
    // Only sends the message if the message listener is defined
    if (this.messageListener != null) {
      this.messageListener.accept(message, isError);
    }
  }

  /**
   * Sends a message to the client if there is one for a specific user
   *
   * @param forPlayerId id of player to send the message to
   * @param message     message to be sent
   * @param isError     whether this message represents an error
   */
  private void sendMessageTo(
    String forPlayerId,
    String message,
    boolean isError
  ) {
    // Only sends the message if the message listener is defined and if the
    // current player matches the message recipient
    if (this.messageListener != null
      && Objects.equals(forPlayerId, currentPlayerId)) {
      this.messageListener.accept(message, isError);
    }
  }

  /**
   * Stores the game state in a map so that it can be restored later. Used for
   * sending the game state over a network or for storing it in a file.
   *
   * @return map representing the game state
   */
  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();

    // Store the game name
    map.put("name", name);

    // Store the list of player ids
    List<String> playerList = players.stream()
      .map(player -> player.id)
      .collect(Collectors.toList());
    map.put("players", playerList);

    // Store the terrain
    ArrayList<ArrayList<Double>> terrainList = new ArrayList<>();
    ArrayList<ArrayList<Integer>> treeList = new ArrayList<>();
    int gridWidth = hexagonGrid.getWidth() + 1;
    int gridHeight = hexagonGrid.getHeight();
    for (int y = 0; y < gridHeight; y++) {
      ArrayList<Double> terrainRow = new ArrayList<>();
      // Trees are stored as integers to reduce file size
      ArrayList<Integer> treeRow = new ArrayList<>();
      for (int x = 0; x < gridWidth - ((y + 1) % 2); x++) {
        Terrain terrain = hexagonGrid.get(x, y).getTerrain();
        terrainRow.add(terrain.height);
        treeRow.add(terrain.hasTree ? 1 : 0);
      }
      terrainList.add(terrainRow);
      treeList.add(treeRow);
    }
    map.put("terrain", terrainList);
    map.put("trees", treeList);

    // Store the cities
    List<Map<String, Object>> cityList = cities.stream()
      // Delegate to the the city's toMap function to store it
      .map(City::toMap)
      .collect(Collectors.toList());
    map.put("cities", cityList);

    // Store the units
    List<Map<String, Object>> unitList = units.stream()
      // Delegate to the the units's toMap function to store it
      .map(Unit::toMap)
      .collect(Collectors.toList());
    map.put("units", unitList);

    // Store player science/gold counts
    map.put("science", playerScienceCounts);
    map.put("gold", playerGoldCounts);

    // Store player unlocked techs. As raw techs cannot be stored, these must
    // be converted to tech names first.
    Map<String, ArrayList<String>> unlockedTechsMap = new HashMap<>();
    for (Map.Entry<String, Set<Tech>> e : playerUnlockedTechs.entrySet()) {
      unlockedTechsMap.put(
        e.getKey(),
        (ArrayList<String>) e.getValue().stream()
          // Convert the techs to just their names
          .map(Tech::getName)
          .collect(Collectors.toList())
      );
    }
    map.put("unlockedTechs", unlockedTechsMap);

    // Store player unlocking techs. Again, raw techs cannot be stored so must
    // be converted.
    Map<String, String> unlockingTechsMap = new HashMap<>();
    for (Map.Entry<String, Tech> e : playerUnlockingTechs.entrySet()) {
      // Only store the tech if the player is currently unlocking something
      if (e.getValue() != null) {
        unlockingTechsMap.put(
          e.getKey(),
          // Convert the tech to just its name
          e.getValue().getName()
        );
      }
    }
    map.put("unlockingTechs", unlockingTechsMap);

    return map;
  }

  /**
   * Send a message indicating a player has won the game
   *
   * @param playerId id of the winning player
   * @param reason   reason the player has won the game (one of
   *                 {@link #VICTORY_REASON_DOMINATION} or
   *                 {@link #VICTORY_REASON_SCIENCE})
   */
  private void win(String playerId, String reason) {
    // Determine whether it's the current player that has won the game
    boolean victory = currentPlayerId == null
      || playerId.equals(currentPlayerId);
    String messageStart = victory ? "Victory!" : "Defeat!";
    String playerPart = playerId.equals(currentPlayerId)
      ? "You've"
      : String.format("%s has", playerId);
    // Send the formatted message
    sendMessage(
      String.format(
        "%s %s won the game by %s!",
        messageStart, playerPart, reason
      ),
      !victory
    );
  }

  /**
   * Checks if a player has won by covering the map with their cities and sends
   * a victory message if they have.
   */
  private void checkDominationVictory() {
    // Player id of potential winner
    String playerId = null;

    // Create a new iterator to iterate over the hexagon grid
    Iterator<Tile> tileIterator = hexagonGrid.iterator();
    while (tileIterator.hasNext()) {
      // Get the next tile to check
      Tile tile = tileIterator.next();
      // If the tile doesn't have a city, then there are tiles that aren't
      // covered. In that case, not all tiles are covered by cities.
      if (tile.city == null) return;
      // Set the potential winner to the first tile with a city
      if (playerId == null) playerId = tile.city.player.id;
      // Check that each subsequent tile with a city is owned by the same
      // player
      if (!playerId.equals(tile.city.player.id)) return;
    }

    // If there was a winning player, send the victory message
    if (playerId != null) {
      win(playerId, VICTORY_REASON_DOMINATION);
    }
  }

  /**
   * Checks if the player already contains a player with the specified ID
   *
   * @param id id to check
   * @return whether a player with that ID already exists
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean containsPlayerWithId(String id) {
    for (Player player : players) {
      if (player.id.equals(id)) return true;
    }
    return false;
  }

  /*
   * START PACKET HANDLING
   */

  /**
   * Creates the unit described by the packet in the tile closest to the
   * desired location
   *
   * @param packet packet containing unit details
   * @return array containing tile the unit was placed on, or null if a tile
   * couldn't be found
   */
  private Tile[] handleUnitCreatePacket(PacketUnitCreate packet) {
    // List of already checked tiles
    ArrayList<Tile> checkedTiles = new ArrayList<>();
    // Queue of tiles to check for placement
    Queue<Tile> placementTilesToCheck = new LinkedList<>();
    // Add the desired location as the first tile to check
    placementTilesToCheck.add(hexagonGrid.get(packet.x, packet.y));

    // While there are still tiles to check...
    Tile tileToCheck;
    while ((tileToCheck = placementTilesToCheck.poll()) != null) {
      // Determine if the tile is a capital of a city (we don't want to place
      // the unit there if it is)
      boolean tileIsCapital = tileToCheck.city != null
        && tileToCheck.city.getCenter().samePositionAs(tileToCheck);
      // If it's not a capital, there isn't a unit there, and we can traverse
      // it, it's a suitable tile, so break out of the search loop
      if (!tileIsCapital
        && tileToCheck.unit == null
        && tileToCheck.canTraverse()
      ) {
        break;
      }

      // Otherwise mark the tile as checked
      checkedTiles.add(tileToCheck);

      // Add all of the tiles neighbours that haven't already been checked to
      // the queue for checking
      placementTilesToCheck.addAll(
        hexagonGrid.getNeighbours(
          tileToCheck.x,
          tileToCheck.y,
          false
        ).stream()
          .filter(tile -> !checkedTiles.contains(tile))
          .collect(Collectors.toList())
      );
    }

    // If a suitable tile was found...
    if (tileToCheck != null) {
      // Create the unit on that tile
      Player player = new Player(packet.id);
      Unit unit = new Unit(player, tileToCheck, packet.getUnitType());
      units.add(unit);

      // And update that tile's render
      return new Tile[]{tileToCheck};
    }

    // Otherwise return null
    return null;
  }

  /**
   * Moves a unit described in the packet to a new location, subtracting the
   * amount of movement points required for the operation.
   *
   * @param packet packet containing movement details
   * @return start and end tile of the movement for updating
   */
  private Tile[] handleUnitMovePacket(PacketUnitMove packet) {
    // Get the start/end tiles of the movement
    Tile startTile = hexagonGrid.get(packet.startX, packet.startY);
    Tile endTile = hexagonGrid.get(packet.endX, packet.endY);

    // Subtract the movement points from the unit's remaining this turn
    startTile.unit.remainingMovementPointsThisTurn -= packet.usedMovementPoints;
    // Check the the remaining points aren't negative (should never happen)
    assert startTile.unit.remainingMovementPointsThisTurn >= 0;

    // Move the unit to a new tile, marking the old tile as empty
    startTile.unit.tile = endTile;
    endTile.unit = startTile.unit;
    startTile.unit = null;

    // Update the start/end tiles
    return new Tile[]{startTile, endTile};
  }

  /**
   * Deletes the unit described in the packet, removing it from the game.
   *
   * @param packet packet containing deletion details
   * @return tile the unit was previously occupying
   */
  private Tile[] handleUnitDeletePacket(PacketUnitDelete packet) {
    // Get the tile the unit is currently occupying
    Tile tile = hexagonGrid.get(packet.x, packet.y);

    // Get the unit on the tile and check it exists
    Unit unit = tile.unit;
    if (unit != null) {
      // If it does, remove it from the tile and the game
      tile.unit = null;
      units.remove(unit);
    }

    return new Tile[]{tile};
  }

  /**
   * Attacks a living object (city/unit) as described by the packet. This may
   * cause the living object and/or the attacker to loose health. If a city was
   * attacked, and it runs out of health, it is given to the player who last
   * attacked it.
   *
   * @param packet packet containing attack details
   * @return tiles the attacker and target are occupying
   */
  private Tile[] handleUnitDamagePacket(PacketDamage packet) {
    // Get the attacker/target tiles
    Tile attackerTile = hexagonGrid.get(packet.attackerX, packet.attackerY);
    Tile targetTile = hexagonGrid.get(packet.targetX, packet.targetY);

    // Determine if the target is a city
    boolean targetIsCity = targetTile.city != null
      && targetTile.city.getCenter().samePositionAs(targetTile);

    // Get the attacker
    Unit attacker = attackerTile.unit;
    // Get the target, prioritising units over cities if a unit is occupying a
    // city (i.e. defending it)
    Living target = targetTile.unit == null
      ? (targetIsCity ? targetTile.city : null)
      : targetTile.unit;

    // Check if the attacker exists and can attack
    if (attacker == null || !attacker.canAttack()) return null;
    // If the target doesn't exist, send a message stating such
    if (target == null) {
      sendMessageTo(
        attacker.player.id,
        "You can't attack nothing!",
        true
      );
      return null;
    }
    // If the target and attacker are owned by the same player, send a message
    // stating such
    if (attacker.getOwner().equals(target.getOwner())) {
      sendMessageTo(
        attacker.player.id,
        "You can't attack yourself!",
        true
      );
      return null;
    }
    // If the attacker has already attacked this turn, send a message stating
    // such
    if (attacker.hasAttackedThisTurn) {
      sendMessageTo(
        attacker.player.id,
        "You've already attacked this turn!",
        true
      );
      return null;
    }

    // Get the number of tiles between the attacker and target
    Point2D targetPos = target.getPosition();
    Point2D attackerPos = attacker.getPosition();
    double distanceBetween = targetPos.distance(attackerPos);
    int tilesBetween = (int) Math.round(distanceBetween / Hexagon.SQRT_3);

    // Check if a melee attack could take place, if so, perform it
    if (
      attacker.hasAbility(UnitAbility.ABILITY_ATTACK)
        && tilesBetween <= 1
    ) {
      target.onAttacked(attacker, false);
      attacker.hasAttackedThisTurn = true;
    }

    // Check if a ranged attack could take place, if so, perform it
    if (
      attacker.hasAbility(UnitAbility.ABILITY_RANGED_ATTACK)
        && tilesBetween <= 2
    ) {
      target.onAttacked(attacker, true);
      attacker.hasAttackedThisTurn = true;
    }

    // Check if the target is a city, if it is and is now dead, give the city
    // to the player that attacked it last
    if (target instanceof City && target.isDead()) {
      City targetCity = (City) target;
      // Increase the cities health
      targetCity.setHealth(10);
      // Set the city's owner
      targetCity.setOwner(targetCity.lastAttacker.player);
      // Check for a domination victory
      checkDominationVictory();
      // Mark every tile for update (re-rendering city walls)
      return new Tile[]{};
    }

    // Mark the attacker and target tile for update
    return new Tile[]{attackerTile, targetTile};
  }

  /**
   * Creates a city at the location described by the packet.
   *
   * @param packet packet containing city creation information
   * @return an empty array of tiles signalling that all tiles should be
   * updated
   */
  private Tile[] handleCityCreatePacket(PacketCityCreate packet) {
    Player player = new Player(packet.id);
    // Create the new city
    cities.add(new City(hexagonGrid, packet.x, packet.y, player));
    // Recalculate player stats with this new city in place
    sendPlayerStats();
    // Signal an update of every tile
    return new Tile[]{};
  }

  /**
   * Grows a city to a specific set of points
   *
   * @param packet packet containing locations of points to grow to
   * @return an empty array of tiles signalling that all tiles should be
   * updated
   */
  private Tile[] handleCityGrowPacket(PacketCityGrow packet) {
    // Get tile with city
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.city != null) {
      t.city.growTo(packet.getGrownTo());
    }
    // Recalculate player stats with new tiles
    sendPlayerStats();
    // Signal an update of every tile
    return new Tile[]{};
  }

  /**
   * Renames a city described by the packet
   *
   * @param packet packet containing rename information
   * @return null signalling no tiles need to be updated
   */
  private Tile[] handleCityRenamePacket(PacketCityRename packet) {
    // Get tile with city
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.city != null) {
      // Rename the city
      t.city.name = packet.newName;
    }
    // No need to update any tiles
    return null;
  }

  /**
   * Requests that a city start building something or purchase an item with
   * gold.
   *
   * @param packet packet containing build information
   * @return an array of tiles to be updated, or null if no tiles are to be
   * updated
   */
  private Tile[] handleCityBuildRequestPacket(PacketCityBuildRequest packet) {
    // Get tile with city
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.city != null) {
      // Get thing to be built by the city
      CityBuildable buildable = packet.getBuildable();
      // If the city is going to build this with production...
      if (packet.withProduction) {
        // Mark it as the current build
        t.city.currentlyBuilding = buildable;
      } else if (
        // Otherwise if they can afford to buy it with gold
        buildable.canBuildWithGold(getPlayerGoldTotal(t.city.player.id))
      ) {
        // Subtract that amount from the player's gold balance
        increasePlayerGoldBy(t.city.player.id, -buildable.getGoldCost());
        // Build the thing, getting the tile that was updated
        Tile placed = buildable.build(t.city, this);
        // If there was an update, return it
        if (placed != null) return new Tile[]{placed};
      }
    }
    // No need to update any tiles
    return null;
  }

  /**
   * Requests that a worker build an improvement on a tile
   *
   * @param packet packet describing the improvement request
   * @return tiles updated by the improvement or null if no tiles are to be
   * updated
   */
  private Tile[] handleWorkerImproveRequestPacket(
    PacketWorkerImproveRequest packet
  ) {
    // Get the tile containing the worker
    Tile t = hexagonGrid.get(packet.x, packet.y);
    if (t.unit != null) {
      // Request that the worker start building the improvement
      t.unit.startWorkerBuilding(packet.getImprovement());
      // Return the updated tile
      return new Tile[]{t};
    }
    // Otherwise, no need to update any tiles
    return null;
  }

  /**
   * Requests that a player start researching a new technology
   *
   * @param packet packet describing the research request
   * @return null as no tiles need to be updated
   */
  private Tile[] handlePlayerResearchRequestPacket(
    PacketPlayerResearchRequest packet
  ) {
    // Set the unlocking tech to the one described in the packet
    playerUnlockingTechs.put(packet.playerId, packet.getTech());
    // Update the UI to reflect this change
    sendTechDetails();
    return null;
  }

  /**
   * Upgrades a unit to a more advanced type with better abilities.
   *
   * @param packet packet describing the upgrade request
   * @return array of tiles to be updated or null if no tiles need to be
   */
  private Tile[] handleUnitUpgradePacket(PacketUnitUpgrade packet) {
    // Get te tile containing the unit to be upgraded
    Tile t = hexagonGrid.get(packet.x, packet.y);
    // Check the unit can be upgraded
    if (t.unit != null && t.unit.unitType.getUpgrade() != null) {
      // Upgrade the unit
      t.unit.unitType = t.unit.unitType.getUpgrade();
      // Set the new health proportional to its current health
      t.unit.setBaseHealth(t.unit.unitType.getBaseHealth());
      return new Tile[]{t};
    }
    return null;
  }

  /**
   * Brings a tile adjacent a city into that city's territory using gold.
   *
   * @param packet packet describing the purchase request
   * @return empty array signalling all tiles should be updated or null if the
   * tile couldn't be bought
   */
  private Tile[] handlePurchaseTileRequestPacket(
    PacketPurchaseTileRequest packet
  ) {
    // Get the capital tile for the target city
    Tile t = hexagonGrid.get(packet.cityX, packet.cityY);
    if (t.city != null) {
      // Get the id of the city owner
      String playerId = t.city.player.id;

      // Get the tile requested to be purchased
      Tile purchaseTile = hexagonGrid.get(packet.purchaseX, packet.purchaseY);

      // Check if the tile is already part of a city, if it is, send a message
      // stating such
      if (purchaseTile.city != null) {
        sendMessageTo(
          playerId,
          "This tile is already part of a city!",
          true
        );
        return null;
      }

      // Check the tile is adjacent to the cities borders
      ArrayList<Tile> neighbours = hexagonGrid.getNeighbours(
        packet.purchaseX,
        packet.purchaseY,
        false
      );
      boolean isNeighbour = neighbours.stream()
        .anyMatch(tile -> tile.city != null && tile.city.sameCenterAs(t.city));
      // If it's not, send a message stating such
      if (!isNeighbour) {
        sendMessageTo(
          playerId,
          "This tile isn't adjacent to this city!",
          true
        );
        return null;
      }

      // Calculate the gold cost of purchasing the tile based on the tile
      // distance between it and the city center
      double distanceBetween = t.getHexagon().getCenter()
        .distance(purchaseTile.getHexagon().getCenter());
      int tilesBetween = (int) Math.round(distanceBetween / Hexagon.SQRT_3);
      double cost = 50.0 * tilesBetween;
      for (Building building : t.city.buildings) {
        cost *= building.expansionCostMultiplier;
      }
      int goldTotal = getPlayerGoldTotal(t.city.player.id);
      int roundCost = (int) Math.round(cost);
      // If the player doesn't have enough money to buy the tile, send them a
      // message stating such
      if (goldTotal < roundCost) {
        sendMessageTo(
          playerId,
          String.format(
            "This tile costs %d gold to purchase, you only have %d!",
            roundCost,
            goldTotal
          ),
          true
        );
        return null;
      }

      // Otherwise, decrease the players gold count by that amount
      increasePlayerGoldBy(playerId, -roundCost);
      // ...and grow the city to encompass that tile
      ArrayList<Point2D> grownTo = new ArrayList<>();
      grownTo.add(new Point2D(purchaseTile.x, purchaseTile.y));
      t.city.growTo(grownTo);
      // Update all tiles to regenerate city walls
      return new Tile[]{};
    }
    return null;
  }

  /**
   * Main turn handler for the game. Updates units (health, attack state,
   * remaining movement), cities (health, build progress) and players
   * (research).
   *
   * @param game game to update (redundant but required by the
   *             {@link TurnHandler} interface)
   * @return an array of tiles to be updated
   */
  @Override
  public Tile[] handleTurn(Game game) {
    ArrayList<Tile> updatedTiles = new ArrayList<>();

    // Update units
    for (Unit unit : units) {
      Tile[] unitUpdatedTiles = unit.handleTurn(this);
      // Add updated tiles to the list
      if (unitUpdatedTiles != null)
        Collections.addAll(updatedTiles, unitUpdatedTiles);
    }

    // Update cities
    for (City city : cities) {
      // Add updated tiles to the list
      Collections.addAll(updatedTiles, city.handleTurn(this));
      // If the city was low on health, and was healed, add the center to the
      // update list (for health bar re-render)
      if (city.naturalHeal()) {
        updatedTiles.add(city.getCenter());
      }
    }

    // Reset ready states
    waitingForPlayers = false;
    readyPlayers.clear();

    // Check to see if a player has won from a city growing
    checkDominationVictory();

    // Send player stats and tech details
    sendPlayerStats();
    sendTechDetails();

    // Update required tiles
    return updatedTiles.toArray(new Tile[]{});
  }

  /**
   * Packet handler. Delegates handling to one of the methods in this class
   * depending on the packet type.
   *
   * @param packet packet to process
   * @return an array of tiles to rerender, if empty, should rerender all
   * tiles, if null, should rerender no tiles
   */
  public Tile[] handlePacket(Packet packet) {
    // Checks the type of the packet and handles it accordingly
    if (packet instanceof PacketPlayerChange) {
      String newId = ((PacketPlayerChange) packet).id;
      if (!containsPlayerWithId(newId)) {
        players.add(new Player(newId));
      }
    } else if (packet instanceof PacketCityCreate) {
      return handleCityCreatePacket((PacketCityCreate) packet);
    } else if (packet instanceof PacketCityGrow) {
      return handleCityGrowPacket((PacketCityGrow) packet);
    } else if (packet instanceof PacketUnitCreate) {
      return handleUnitCreatePacket((PacketUnitCreate) packet);
    } else if (packet instanceof PacketUnitMove) {
      return handleUnitMovePacket((PacketUnitMove) packet);
    } else if (packet instanceof PacketUnitDelete) {
      return handleUnitDeletePacket((PacketUnitDelete) packet);
    } else if (packet instanceof PacketDamage) {
      return handleUnitDamagePacket((PacketDamage) packet);
    } else if (packet instanceof PacketCityRename) {
      return handleCityRenamePacket((PacketCityRename) packet);
    } else if (packet instanceof PacketCityBuildRequest) {
      return handleCityBuildRequestPacket((PacketCityBuildRequest) packet);
    } else if (packet instanceof PacketWorkerImproveRequest) {
      return handleWorkerImproveRequestPacket(
        (PacketWorkerImproveRequest) packet
      );
    } else if (packet instanceof PacketPlayerResearchRequest) {
      return handlePlayerResearchRequestPacket(
        (PacketPlayerResearchRequest) packet
      );
    } else if (packet instanceof PacketUnitUpgrade) {
      return handleUnitUpgradePacket((PacketUnitUpgrade) packet);
    } else if (packet instanceof PacketPurchaseTileRequest) {
      return handlePurchaseTileRequestPacket(
        (PacketPurchaseTileRequest) packet
      );
    } else if (packet instanceof PacketBlastOff) {
      win(((PacketBlastOff) packet).playerId, VICTORY_REASON_SCIENCE);
    } else if (packet instanceof PacketReady) {
      return handleTurn(this);
    }
    return null;
  }
  /*
   * END PACKET HANDLING
   */

  /**
   * Calculates the player's gold/science per turn and sends them along with
   * the gold total.
   */
  @ClientOnly
  private void sendPlayerStats() {
    if (currentPlayerId != null && playerStatsListener != null) {
      int totalSciencePerTurn = 0;
      int totalGoldPerTurn = 0;
      for (City city : cities) {
        if (city.player.id.equals(currentPlayerId)) {
          totalSciencePerTurn += city.getSciencePerTurn();
          totalGoldPerTurn += city.getGoldPerTurn();
        }
      }
      playerStatsListener.accept(new PlayerStats(
        totalSciencePerTurn,
        getPlayerGoldTotal(currentPlayerId),
        totalGoldPerTurn
      ));
    }
  }

  /**
   * Sends the current players unlocked techs, and the currently researching
   * tech with its progress.
   */
  @ClientOnly
  private void sendTechDetails() {
    if (currentPlayerId != null && techDetailsListener != null) {
      techDetailsListener.accept(new PlayerTechDetails(
        getPlayerUnlockedTechs(currentPlayerId),
        getPlayerUnlockingTech(currentPlayerId),
        getPlayerUnlockingProgress(currentPlayerId)
      ));
    }
  }

  /**
   * Creates the packets for creating a player's starting units
   *
   * @param playerId player id to create units for
   * @return packets that create the starting units
   */
  @ServerOnly
  public PacketUnitCreate[] createStartingUnits(String playerId) {
    // Calculate the coordinate of the starting location based on the number
    // of players already in the game
    int numPlayers = players.size();
    int gridWidth = hexagonGrid.getWidth();
    int gridHeight = hexagonGrid.getHeight();
    int x = gridWidth / 2;
    int y = gridHeight / 2;
    switch (numPlayers) {
      case 1:
        x = 1;
        y = 1;
        break;
      case 2:
        x = gridWidth - 3;
        y = gridHeight - 3;
        break;
      case 3:
        x = gridWidth - 3;
        y = 1;
        break;
      case 4:
        x = 1;
        y = gridHeight - 3;
        break;
    }

    // Create the packets
    PacketUnitCreate[] packetUnitCreates = new PacketUnitCreate[]{
      new PacketUnitCreate(playerId, x, y, UnitType.SETTLER),
      new PacketUnitCreate(playerId, x, y, UnitType.WARRIOR)
    };
    // Handle them (server side only)
    for (PacketUnitCreate packetUnitCreate : packetUnitCreates)
      handlePacket(packetUnitCreate);
    // Return them so they can be sent to the clients
    return packetUnitCreates;
  }

  /**
   * Checks whether all players have marked themselves as ready
   *
   * @return whether all players have clicked the "Next Turn" button
   */
  @ServerOnly
  public boolean allPlayersReady() {
    for (Player player : players) {
      if (!readyPlayers.getOrDefault(player.id, false)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the techs researched by the specified player
   *
   * @param playerId id of the player to check the techs of
   * @return researched techs by that player
   */
  public Set<Tech> getPlayerUnlockedTechs(String playerId) {
    return playerUnlockedTechs.getOrDefault(playerId, new HashSet<>());
  }

  /**
   * Gets the tech currently being researched by the specified player
   *
   * @param playerId id of the player to check the tech of
   * @return currently researching tech of that player, or null if they're not
   * researching anything
   */
  public Tech getPlayerUnlockingTech(String playerId) {
    return playerUnlockingTechs.get(playerId);
  }

  /**
   * Gets the progress through the research stage the specified player is
   *
   * @param playerId id of the player to check the progress of
   * @return progress of the currently researching tech, or 0 if not currently
   * researching
   */
  public double getPlayerUnlockingProgress(String playerId) {
    // Get the current tech, and check it exists
    Tech unlockingTech = getPlayerUnlockingTech(playerId);
    if (unlockingTech == null) return 0;

    int scienceTotal = getPlayerScienceTotal(playerId);
    int scienceCost = unlockingTech.getScienceCost();
    if (scienceCost == 0) return 0;

    // Calculate the percent unlock, clamping the value to 1
    return Math.min((double) scienceTotal / (double) scienceCost, 1);
  }

  /**
   * Check whether a player has unlocked the specified item
   *
   * @param playerId   id of the player to check
   * @param unlockable unlockable item to check if unlocked
   * @return whether the item has been unlocked
   */
  public boolean playerHasUnlocked(String playerId, Unlockable unlockable) {
    // Get the unlock id of the item, and check it's not unlocked by default
    int unlockId = unlockable.getUnlockId();
    if (unlockId == 0x00) return true;
    // Get the player's unlocked techs
    Set<Tech> unlockedTechs = getPlayerUnlockedTechs(playerId);
    for (Tech unlockedTech : unlockedTechs) {
      // Check if the tech includes the specified unlockable
      for (Unlockable unlock : unlockedTech.getUnlocks()) {
        if (unlock.getUnlockId() == unlockId) return true;
      }
    }
    return false;
  }

  /**
   * Gets a player resource count with a default value of 0
   *
   * @param counts   map containing player resource information
   * @param playerId id of the player to get the value of
   * @return player's count of that resource
   */
  private int getPlayerResource(Map<String, Integer> counts, String playerId) {
    return counts.getOrDefault(playerId, 0);
  }

  /**
   * Gets a players gold total with a default value of 0
   *
   * @param playerId id of the player to get the value of
   * @return player's gold total
   */
  public int getPlayerGoldTotal(String playerId) {
    return getPlayerResource(playerGoldCounts, playerId);
  }

  /**
   * Gets a players science total with a default value of 0
   *
   * @param playerId id of the player to get the value of
   * @return player's science total
   */
  @SuppressWarnings("WeakerAccess")
  public int getPlayerScienceTotal(String playerId) {
    return getPlayerResource(playerScienceCounts, playerId);
  }

  /**
   * Increases a player's resource count by the specified amount
   *
   * @param counts   map containing player resource information
   * @param playerId id of the player to increase
   * @param amount   amount to increase by (can be negative)
   */
  private void increasePlayerResourceBy(
    Map<String, Integer> counts,
    String playerId,
    int amount
  ) {
    if (counts.containsKey(playerId)) {
      counts.put(playerId, counts.get(playerId) + amount);
    } else {
      counts.put(playerId, amount);
    }
    sendPlayerStats();
  }

  /**
   * Increases a player's gold total by the specified amount
   *
   * @param playerId id of the player to increase
   * @param gold     amount to increase by (can be negative)
   */
  public void increasePlayerGoldBy(String playerId, int gold) {
    increasePlayerResourceBy(playerGoldCounts, playerId, gold);
  }

  /**
   * Increases a player's science total by the specified amount
   *
   * @param playerId id of the player to increase
   * @param science  amount to increase by (can be negative)
   */
  public void increasePlayerScienceBy(String playerId, int science) {
    increasePlayerResourceBy(playerScienceCounts, playerId, science);

    // Get player's technology details
    Set<Tech> unlockedTechs = getPlayerUnlockedTechs(playerId);
    Tech unlockingTech = getPlayerUnlockingTech(playerId);
    double progress = getPlayerUnlockingProgress(playerId);

    // Check if a tech has now been unlocked
    if (unlockingTech != null && progress >= 1
      && !unlockedTechs.contains(unlockingTech)) {
      // Unlock the tech if it has
      playerUnlockedTechs.putIfAbsent(playerId, new HashSet<>());
      playerUnlockedTechs.get(playerId).add(unlockingTech);
      // Reset the current research
      playerUnlockingTechs.put(playerId, null);
      // Reduce science by the cost of the tech
      increasePlayerResourceBy(
        playerScienceCounts,
        playerId,
        -unlockingTech.getScienceCost()
      );
      // Send a message to the player
      sendMessageTo(
        playerId,
        String.format("%s has been unlocked!", unlockingTech.getName()),
        false
      );
    }
  }

  /**
   * Gets a player's cities
   *
   * @param id id of the player to get cities of
   * @return list of cities belonging to the player
   */
  public ArrayList<City> getPlayersCitiesById(String id) {
    return (ArrayList<City>) cities.stream()
      .filter(city -> city.player.id.equals(id))
      .collect(Collectors.toList());
  }
}
