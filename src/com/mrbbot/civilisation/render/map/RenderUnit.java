package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.generic.render.Render;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.util.Random;

class RenderUnit extends RenderData<Unit> {
  private static final Random random = new Random();

  RenderUnit(Unit data) {
    super(data);

    add(buildPerson());
    for (int i = 0; i < 6; i++) {
      Render rotor = new Render();
      rotor.add(buildPerson());
      rotor.translate.setX(0.6);
      rotor.rotateZ.setAngle(60 * i);
      add(rotor);
    }
  }

  private Render buildPerson() {
    Render person = new Render();

    Cylinder leg1 = new Cylinder(0.1, 0.2);
    leg1.setMaterial(new PhongMaterial(Color.LIGHTGOLDENRODYELLOW));
    leg1.setTranslateX(-0.1);
    leg1.setTranslateZ(0.1);
    leg1.setRotationAxis(Rotate.X_AXIS);
    leg1.setRotate(90);
    person.add(leg1);

    Cylinder leg2 = new Cylinder(0.1, 0.2);
    leg2.setMaterial(new PhongMaterial(Color.LIGHTGOLDENRODYELLOW));
    leg2.setTranslateX(0.1);
    leg2.setTranslateZ(0.1);
    leg2.setRotationAxis(Rotate.X_AXIS);
    leg2.setRotate(90);
    person.add(leg2);

    Cylinder torso = new Cylinder(0.2, 0.4);
    torso.setMaterial(new PhongMaterial(new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1)));
    torso.setTranslateZ(0.2 + 0.2);
    torso.setRotationAxis(Rotate.X_AXIS);
    torso.setRotate(90);
    person.add(torso);

    Sphere head = new Sphere(0.3);
    head.setMaterial(new PhongMaterial(Color.LIGHTGOLDENRODYELLOW));
    head.setTranslateZ(0.2 + 0.4 + 0.27);
    person.add(head);

    person.scaleTo(0.5);

    return person;
  }
}
