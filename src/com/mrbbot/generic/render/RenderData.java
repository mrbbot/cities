package com.mrbbot.generic.render;

@SuppressWarnings("WeakerAccess")
public class RenderData<T>
  extends Render {
  public T data;

  public RenderData(T data) {
    super();
    this.data = data;
  }

}

