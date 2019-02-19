package com.mrbbot.civilisation.render;

import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderRoot;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

@ClientOnly
public class RenderCivilisation extends RenderRoot<RenderGame> {
  private final static double MAX_ZOOM = 5;
  private final static double MIN_ZOOM = 80;

  private double oldMouseX = -1, oldMouseY = -1;

  public RenderCivilisation(RenderGame root, int width, int height) {
    super(root, width, height);

    /*PointLight light = new PointLight(Color.WHITE);
    light.setTranslateZ(4);
    add(light);*/

    //AmbientLight ambientLight = new AmbientLight(Color.WHITE.darker().darker());
    //add(ambientLight);

    //
    // PANNING & ZOOMING
    //
    subScene.setOnScroll((e) -> {
      double newValue = camera.translate.getZ() + (e.getDeltaY() / 40);
      if (newValue > -MAX_ZOOM) newValue = -MAX_ZOOM;
      if (newValue < -MIN_ZOOM) newValue = -MIN_ZOOM;
      camera.translate.setZ(newValue);
    });

    subScene.setOnMouseDragged((e) -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        double x = e.getX(), y = e.getY();
        if (oldMouseX == -1 || oldMouseY == -1) {
          oldMouseX = x;
          oldMouseY = y;
        } else {
          double dX = x - oldMouseX;
          double dY = y - oldMouseY;
          oldMouseX = x;
          oldMouseY = y;

          double multiplier = (Math.abs(camera.translate.getZ()) / 30) * 2;

          dX *= multiplier;
          dY *= multiplier;

          camera.translateBy(-dX / 250, -dY / 250, 0);
        }
      }
    });

    subScene.setOnMouseReleased((e) -> {
      switch(e.getButton()) {
        case PRIMARY:
          oldMouseX = -1;
          oldMouseY = -1;
          break;
        case SECONDARY:
          this.root.resetPathfinding();
          break;
        default:
          break;
      }
    });
  }

  public void setScene(Scene scene, EventHandler<? super KeyEvent> eventHandler) {
    scene.setOnKeyPressed((e) -> {
      switch(e.getCode()) {
        case W:
          camera.rotateBy(-1, 0, 0);
          break;
        case S:
          camera.rotateBy(1, 0, 0);
          break;
      }
      eventHandler.handle(e);
    });
  }

  /*public void handlePacket(Packet packet) {
    root.handlePacket(packet);

    if(packet instanceof PacketUnitMove) {
      root.handleUnitMovePacket((PacketUnitMove) packet);
    } else if(packet instanceof PacketPlayerChange) {
      boolean playerAlreadyAdded = false;
      String newId = ((PacketPlayerChange) packet).id;
      for (Player player : root.data.players) {
        if (player.id.equals(newId)) {
          playerAlreadyAdded = true;
          break;
        }
      }
      if(!playerAlreadyAdded) {
        root.data.players.add(new Player(newId));
      }
    } else if(packet instanceof PacketCityCreate) {
      root.handleCityCreate((PacketCityCreate) packet);
    } else if(packet instanceof PacketCityGrow) {
      root.handleCityGrow((PacketCityGrow) packet);
    } else if(packet instanceof PacketUnitCreate) {
      root.handleUnitCreatePacket((PacketUnitCreate) packet);
    } else if(packet instanceof PacketUnitDelete) {
      root.handleUnitDeletePacket((PacketUnitDelete) packet);
    }
  }*/
}
