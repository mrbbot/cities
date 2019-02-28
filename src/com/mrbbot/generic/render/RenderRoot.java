package com.mrbbot.generic.render;

import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;

/**
 * Root render object that handles creating a sub scene so that other objects
 * added to this can be scene. Also creates the camera for the scene.
 *
 * @param <T> render type that should be the root of the scene where all other
 *            children are added
 */
public class RenderRoot<T extends Render>
  extends Render {
  /**
   * Root render object of the scene where other children that make up the
   * scene should be added.
   */
  public final T root;
  /**
   * The camera used for rendering the scene. Can be transformed to see
   * different perspectives.
   */
  public final RenderCamera camera;
  /**
   * The sub scene that allows the root render object to be visible within a
   * JavaFX applications's scene.
   */
  public final SubScene subScene;

  /**
   * Creates a new root render object with camera and sub scene
   *
   * @param root   root render object to be rendered by the camera
   * @param width  width of the sub scene
   * @param height height of the sub scene
   */
  public RenderRoot(T root, int width, int height) {
    super();

    this.root = root;

    // Creates and adds the camera
    camera = new RenderCamera();
    add(root, camera);

    // Creates the new sub scene pointing to this as the root
    subScene = new SubScene(
      this,
      width,
      height,
      // Enable the depth buffer for 3D support
      true,
      // Enable antialiasing for smoother edges on 3D objects
      SceneAntialiasing.BALANCED
    );
    // Set the default background of the scene
    subScene.setFill(Color.WHITE);
    // Set the camera responsible for rendering the scene
    subScene.setCamera(camera.camera);
  }

}

