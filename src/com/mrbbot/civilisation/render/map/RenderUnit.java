package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

@ClientOnly
class RenderUnit extends RenderData<Unit> {
  private Cylinder[] torsos, belts;
  private Render[] people;
  private RenderHealthBar healthBar;

  RenderUnit(Unit data) {
    super(data);

    torsos = new Cylinder[7];
    belts = new Cylinder[7];
    people = new Render[7];

    add(people[0] = buildPerson(0));
    for (int i = 0; i < 6; i++) {
      Render rotor = new Render();
      rotor.add(people[i + 1] = buildPerson(i + 1));
      rotor.translate.setX(0.5);
      rotor.rotateZ.setAngle(60 * i);
      add(rotor);
    }

    add(healthBar = new RenderHealthBar(data, false));
  }

  @SuppressWarnings("Duplicates")
  private Render buildPerson(int i) {
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
    torso.setMaterial(new PhongMaterial(Color.WHITE));
    torso.setTranslateZ(0.2 + 0.2);
    torso.setRotationAxis(Rotate.X_AXIS);
    torso.setRotate(90);
    person.add(torso);
    torsos[i] = torso;

    Cylinder belt = new Cylinder(0.25, 0.05);
    belt.setMaterial(new PhongMaterial(Color.WHITE));
    belt.setTranslateZ(0.2 + 0.15);
    belt.setRotationAxis(Rotate.X_AXIS);
    belt.setRotate(90);
    person.add(belt);
    belts[i] = belt;

    Sphere head = new Sphere(0.3);
    head.setMaterial(new PhongMaterial(Color.LIGHTGOLDENRODYELLOW));
    head.setTranslateZ(0.2 + 0.4 + 0.27);
    person.add(head);

    Sphere eye = new Sphere(0.05);
    eye.setMaterial(new PhongMaterial(Color.BLACK));
    eye.setTranslateZ(0.2 + 0.4 + 0.27);
    eye.setTranslateX(0.1);
    eye.setTranslateY(0.3);
    person.add(eye);

    Sphere eye2 = new Sphere(0.05);
    eye2.setMaterial(new PhongMaterial(Color.BLACK));
    eye2.setTranslateZ(0.2 + 0.4 + 0.27);
    eye2.setTranslateX(-0.1);
    eye2.setTranslateY(0.3);
    person.add(eye2);

    person.scaleTo(0.5);
    double angle = 180;
    if(i > 0) angle -= (i - 1) * 60;
    person.rotateZ.setAngle(angle);

    return person;
  }

  void updateRender(Unit unit) {
    if(unit != null) {
      PhongMaterial torsoMaterial = new PhongMaterial(unit.unitType.getColor());
      PhongMaterial beltMaterial = new PhongMaterial(unit.player.getColour());

      double healthPercent = unit.getHealthPercent();
      double onePersonProportion = 1.0 / (double)people.length;
      for (int i = 0; i < people.length; i++) {
        torsos[i].setMaterial(torsoMaterial);
        belts[i].setMaterial(beltMaterial);
        people[i].setVisible(healthPercent >= i * onePersonProportion);
      }
    }
    healthBar.updateRender(unit);
  }
}
