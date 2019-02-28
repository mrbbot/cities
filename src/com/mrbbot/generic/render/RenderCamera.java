package com.mrbbot.generic.render;

import javafx.scene.PerspectiveCamera;

/**
 * Render object that just contains a camera that handles displaying the
 * rest of the scene.
 */
public class RenderCamera
  extends Render {

  /**
   * Camera that displays the scene. Can be positioned as if it were on a
   * tripod so that different perspectives of the same scene can be seen.
   */
  PerspectiveCamera camera;

  RenderCamera() {
    super();

    // Create the camera
    camera = new PerspectiveCamera(true);

    // Give it an initial transformation looking down slightly leant back
    rotateX.setAngle(220);
    translate.setZ(-30);

    add(camera);
  }

}

