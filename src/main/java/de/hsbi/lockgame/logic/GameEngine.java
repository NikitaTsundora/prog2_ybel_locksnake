package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import de.hsbi.lockgame.ui.GamePanel;
import java.util.ArrayList;
import java.util.List;

public final class GameEngine {

  private final List<GameStateObserver> observers = new ArrayList<>();
  private GameState state;

  private GamePanel panel;

  public GameEngine(Level level) {
    Snake snake = new Snake(List.of(level.snakeStart()));
    this.state =
        new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.NONE);
  }

  public GameState state() {
    return state;
  }

  public void addObserver(GameStateObserver obs) {
    observers.add(obs);
  }

  private void notifyObservers() {
    observers.forEach(obs -> obs.onGameStateChanged(state));
  }

  public void setGamePanel(GamePanel panel) {
    this.panel = panel;
  }

  private void notifyObserver() {
    if (panel != null) {
      panel.update(state);
    }
  }

  public void update(Direction d) {
    this.state = new GameState(state.level(), state.snake(), state.pins(), state.status(), d);
    List<Runnable> actions = List.of(this::notifyObserver, this::notifyObservers);
    actions.forEach(Runnable::run);
  }

  // Zeigt einmal lösung mit Referenz und einmal mit Lambda

  public void tick() {
    this.state = state.tick();
    Runnable r =
        () -> {
          notifyObserver();
          notifyObservers();
        };
    r.run();
  }
}
