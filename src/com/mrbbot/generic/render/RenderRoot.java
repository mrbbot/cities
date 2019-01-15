package com.mrbbot.generic.render;

import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;

public class RenderRoot<T extends Render>
  extends Render {
  public final T root;
  public final RenderCamera camera;
  public final SubScene subScene;

  public RenderRoot(T root, int width, int height) {
    super();

    this.root = root;
    camera = new RenderCamera();

    add(root, camera);

    subScene = new SubScene(
      this,
      width,
      height,
      true,
      SceneAntialiasing.BALANCED
    );
    subScene.setFill(Color.WHITE);
    subScene.setCamera(camera.camera);
  }

}

