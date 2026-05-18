package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;

public interface DirectionObserver {
  void onDirectionChanged(Direction d);
}
