package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Tile;

/**
 * Interface describing an object that contains logic that should be executed
 * at the beginning of every turn. This might be healing a unit, growing a
 * city, etc
 */
public interface TurnHandler {
  /**
   * Turn handler function definition
   *
   * @param game game the turn is taking place in
   * @return an array of tiles to rerender, if empty, should rerender all
   * tiles, if null, should rerender no tiles
   */
  Tile[] handleTurn(Game game);
}
