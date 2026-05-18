package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.ArrayList;
import java.util.List;

public final class GameState {

  private final Level level;
  private final Snake snake;
  private final List<Pin> pins;
  private final Status status;
  private final Direction pendingDirection;

  public GameState(
      Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {

    this.level = level;
    this.snake = snake;
    this.pins = pins;
    this.status = status;
    this.pendingDirection = pendingDirection;
  }

  public Level level() {
    return level;
  }

  public Snake snake() {
    return snake;
  }

  public List<Pin> pins() {
    return pins;
  }

  public Status status() {
    return status;
  }

  public Direction pendingDirection() {
    return pendingDirection;
  }

  public GameState tick() {

    // Early exit
    if (!status.isRunning() || pendingDirection == Direction.NONE) {
      return this;
    }
    Position head = snake.head();
    Position next =
        switch (pendingDirection) {
          case UP -> new Position(head.x(), head.y() - 1);
          case DOWN -> new Position(head.x(), head.y() + 1);
          case LEFT -> new Position(head.x() - 1, head.y());
          case RIGHT -> new Position(head.x() + 1, head.y());
          default -> head;
        };

    // (a) Schlange würde das Spielfeld verlassen: Spiel verloren
    if (!level.isInside(next)) {
      return new GameState(level, snake, pins, Status.LOST_OUT_OF_BOUNDS, Direction.NONE);
    }

    // (b) Schlange würde in ein Wandelement gehen: Blockiert (keine Bewegung, Blickrichtung "none")
    if (level.cellAt(next) == CellType.WALL) {
      return new GameState(level, snake, pins, Status.RUNNING, Direction.NONE);
    }

    // (c) Schlange beisst sich: Spiel verloren
    for (Position p : snake.body()) {
      if (p.x() == next.x() && p.y() == next.y()) {
        return new GameState(level, snake, pins, Status.LOST_SELF_COLLISION, Direction.NONE);
      }
    }

    // (d) Schlange würde auf einen Pin gehen (Pin bereits gesetzt oder Schlange kommt nicht in der
    // Aktivierungsrichtung): Blockiert (keine Bewegung, Blickrichtung "none")

    for (Pin pin : pins) {
      if (pin.position().x() == next.x() && pin.position().y() == next.y()) {
        if (pin.state().isSet()) {
          return new GameState(level, snake, pins, Status.RUNNING, Direction.NONE);
        }
        if (pin.activationDirection() != pendingDirection) {
          return new GameState(level, snake, pins, Status.RUNNING, Direction.NONE);
        }
        List<Pin> newPins = new ArrayList<>(pins.size());
        for (Pin p : pins) {
          if (p == pin) {
            newPins.add(p.withState(Pin.State.HIGH));
          } else {
            newPins.add(p);
          }
        }
        return new GameState(level, snake, newPins, Status.RUNNING, Direction.NONE);
      }
    }
    Snake moved = snake.grow(pendingDirection);
    boolean allSet = true;
    allSet = pins.stream().allMatch(p -> p.state().isSet());

    if (allSet) {
      return new GameState(level, moved, pins, Status.WON, pendingDirection);
    }

    return new GameState(level, moved, pins, Status.RUNNING, pendingDirection);
  }

  public enum Status {
    RUNNING,
    WON,
    LOST_SELF_COLLISION,
    LOST_OUT_OF_BOUNDS;

    public boolean isRunning() {
      return this == RUNNING;
    }
  }
}
