package com.mrbbot.generic.render;

import javafx.scene.PerspectiveCamera;

public class RenderCamera
  extends Render {

  PerspectiveCamera camera;

  RenderCamera() {
    super();
    camera = new PerspectiveCamera(true);

    //    rotateX.setAngle(180);
    rotateX.setAngle(220);
    translate.setZ(-30); //-30

    add(camera);
  }

}

