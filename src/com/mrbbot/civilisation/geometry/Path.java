package com.mrbbot.civilisation.geometry;

import com.mrbbot.civilisation.logic.interfaces.Traversable;

import java.util.List;

public class Path<E extends Traversable> {
  public final List<E> path;
  public final int totalCost;

  Path(List<E> path, int totalCost) {
    this.path = path;
    this.totalCost = totalCost;
  }
}
