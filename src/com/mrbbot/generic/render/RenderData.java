package com.mrbbot.generic.render;

/**
 * Render object for that stores some additional data required for rendering.
 * Used by the game's render itself and also for individual tile renders.
 *
 * @param <T> type of the data to be stored along with the render
 */
public class RenderData<T>
  extends Render {
  /**
   * Data that is required for this object to be rendered containing
   * information about how the render should look.
   */
  public T data;

  public RenderData(T data) {
    super();
    this.data = data;
  }
}

