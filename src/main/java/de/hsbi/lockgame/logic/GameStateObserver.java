package de.hsbi.lockgame.logic;

public interface GameStateObserver {
  void onGameStateChanged(GameState state);
}
