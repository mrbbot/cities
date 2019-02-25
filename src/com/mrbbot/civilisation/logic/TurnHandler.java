package com.mrbbot.civilisation.logic;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.Tile;

public interface TurnHandler {
  Tile[] handleTurn(Game game);
}
