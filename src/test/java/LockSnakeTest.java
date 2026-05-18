import static org.junit.jupiter.api.Assertions.*;

import de.hsbi.lockgame.logic.GameState;
import de.hsbi.lockgame.model.*;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LockSnakeTest {

  private Level simpleLevel() {
    CellType[][] cells = {
      {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY}, // x = 0
      {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY}, // x = 1
      {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY} // x = 2
    };
    return new Level(3, 3, cells, List.of(), new Position(1, 1));
  }

  private Snake snakeAt(int x, int y) {
    return new Snake(List.of(new Position(x, y)));
  }

  // 1. Early exit: Status nicht RUNNING
  @Test
  void givenNotRunning_whenTick_thenStateUnchanged() {
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(1, 1), level.pins(), GameState.Status.WON, Direction.UP);
    var result = state.tick();
    assertSame(state, result);
  }

  // 2. Early exit: pendingDirection == NONE
  @Test
  void givenNoDirection_whenTick_thenStateUnchanged() {
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(1, 1), level.pins(), GameState.Status.RUNNING, Direction.NONE);
    var result = state.tick();
    assertSame(state, result);
  }

  // 3. LOST_OUT_OF_BOUNDS
  @Test
  void givenMoveOutOfBounds_whenTick_thenLostOutOfBounds() {
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(0, 0), level.pins(), GameState.Status.RUNNING, Direction.UP);
    var result = state.tick();
    assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, result.status());
  }

  // 4. Wand blockiert Bewegung
  @Test
  void givenWallAhead_whenTick_thenNoMovementAndDirectionNone() {
    CellType[][] cells = {
      {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY}, // x=0
      {CellType.WALL, CellType.EMPTY, CellType.EMPTY}, // x=1
      {CellType.EMPTY, CellType.EMPTY, CellType.EMPTY} // x=2
    };
    Pin dummy = new Pin(new Position(2, 2), Pin.State.LOW, Direction.UP);
    var level = new Level(3, 3, cells, List.of(dummy), new Position(1, 1));
    var state =
        new GameState(level, snakeAt(1, 1), level.pins(), GameState.Status.RUNNING, Direction.UP);
    var result = state.tick();
    assertEquals(GameState.Status.RUNNING, result.status());
    assertEquals(Direction.NONE, result.pendingDirection());
    assertEquals(1, result.snake().head().x());
    assertEquals(1, result.snake().head().y());
  }

  // 5. Beisst sich
  @Test
  void givenSelfCollision_whenTick_thenLostSelfCollision() {
    var snake = new Snake(List.of(new Position(1, 1), new Position(1, 2)));
    var level = simpleLevel();
    var state = new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.DOWN);
    var result = state.tick();
    assertEquals(GameState.Status.LOST_SELF_COLLISION, result.status());
  }

  // 6. Pin blockiert wenn HIGH
  @Test
  void givenPinHigh_whenTick_thenBlocked() {
    var pin = new Pin(new Position(1, 0), Pin.State.HIGH, Direction.UP);
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(1, 1), List.of(pin), GameState.Status.RUNNING, Direction.UP);
    var result = state.tick();
    assertEquals(GameState.Status.RUNNING, result.status());
    assertEquals(Direction.NONE, result.pendingDirection());
    assertEquals(1, result.snake().head().x());
    assertEquals(1, result.snake().head().y());
  }

  // 7. Pin blockiert wenn falsche Richtung
  @Test
  void givenPinWrongDirection_whenTick_thenBlocked() {
    var pin = new Pin(new Position(1, 0), Pin.State.LOW, Direction.DOWN);
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(1, 1), List.of(pin), GameState.Status.RUNNING, Direction.UP);
    var result = state.tick();
    assertEquals(GameState.Status.RUNNING, result.status());
    assertEquals(Direction.NONE, result.pendingDirection());
  }

  // 8. Pin wird gesetzt wenn richtige Richtung
  @Test
  void givenPinCorrectDirection_whenTick_thenPinBecomesHigh() {
    var pin = new Pin(new Position(1, 0), Pin.State.LOW, Direction.UP);
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(1, 1), List.of(pin), GameState.Status.RUNNING, Direction.UP);
    var result = state.tick();
    assertTrue(result.pins().get(0).state().isSet());
    assertEquals(Direction.NONE, result.pendingDirection());
  }

  // 9. Schlange bewegt sich normal
  @Test
  void givenFreeSpace_whenTick_thenSnakeMoves() {
    var level = simpleLevel();
    var state =
        new GameState(level, snakeAt(1, 1), level.pins(), GameState.Status.RUNNING, Direction.UP);
    var result = state.tick();
    assertEquals(1, result.snake().head().x());
    assertEquals(0, result.snake().head().y());
  }

  // 10. Schlange beisst sich, wenn sie rückwärts geht
  @Test
  void givenReverseDirection_whenTick_thenBlockedAndDirectionNone() {
    var level = simpleLevel();
    var snake = new Snake(List.of(new Position(1, 1), new Position(1, 2)));
    var state = new GameState(level, snake, level.pins(), GameState.Status.RUNNING, Direction.DOWN);
    var result = state.tick();
    assertEquals(1, result.snake().head().x());
    assertEquals(1, result.snake().head().y());
    assertEquals(Direction.NONE, result.pendingDirection());
    assertEquals(GameState.Status.LOST_SELF_COLLISION, result.status());
  }
}
