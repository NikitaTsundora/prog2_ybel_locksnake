package de.hsbi.lockgame.ui;

import de.hsbi.lockgame.logic.DirectionObserver;
import de.hsbi.lockgame.logic.GameEngine;
import de.hsbi.lockgame.logic.GameState;
import de.hsbi.lockgame.logic.GameStateObserver;
import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.settings.GameConstants;
import de.hsbi.lockgame.settings.InputConstants;
import de.hsbi.lockgame.ui.render.GameRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GamePanel extends JPanel implements GameStateObserver {

  private GameState state;
  private final GameRenderer renderer;

  // Für alte Main:
  private GameEngine gameEngine;

  // Für neues Observer-Pattern:
  private final List<DirectionObserver> directionObservers = new ArrayList<>();

  public GamePanel(GameState initialState, GameRenderer renderer) {
    this.state = initialState;
    this.renderer = renderer;

    int width = initialState.level().width() * GameConstants.TILE_SIZE;
    int height = initialState.level().height() * GameConstants.TILE_SIZE;

    setPreferredSize(new Dimension(width, height));
    setBackground(Color.BLACK);

    setFocusable(true);

    // Keybindings aus InputConstants
    InputConstants.BINDINGS.forEach(this::setupKeyBindings);
  }

  // --- UI wird automatisch aktualisiert ---
  @Override
  public void onGameStateChanged(GameState state) {
    this.state = state;
    repaint();
  }

  // --- Für alte Main ---
  public void update(GameState newState) {
    this.state = newState;
    repaint();
  }

  // --- Für alte Main ---
  public void setGameEngine(GameEngine engine) {
    this.gameEngine = engine;
  }

  // --- Neues Observer-Pattern: Engine beobachtet UI ---
  public void addDirectionObserver(DirectionObserver obs) {
    directionObservers.add(obs);
  }

  private void notifyDirection(Direction d) {
    for (DirectionObserver obs : directionObservers) {
      obs.onDirectionChanged(d);
    }
  }

  // --- KeyBindings ---
  private void setupKeyBindings(Direction direction, Iterable<Integer> keyCodes) {

    var inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    var actionMap = getActionMap();

    String actionKey = "move_" + direction.name();

    // Swing Action
    var swingAction =
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {

            // Neues Observer-Pattern:
            notifyDirection(direction);

            // Alte Main:
            if (gameEngine != null) {
              gameEngine.update(direction);
            }
          }
        };

    // KeyStroke → Action
    for (int keyCode : keyCodes) {
      inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), actionKey);
    }

    actionMap.put(actionKey, swingAction);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    renderer.render((Graphics2D) g, state, GameConstants.TILE_SIZE);
  }
}
