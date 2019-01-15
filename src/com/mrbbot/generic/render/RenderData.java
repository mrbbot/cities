package com.mrbbot.generic.render;

@SuppressWarnings("WeakerAccess")
public class RenderData<T>
  extends Render {
  public final T data;

  public RenderData(T data) {
    super();
    this.data = data;
  }

}

