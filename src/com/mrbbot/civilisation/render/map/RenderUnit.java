package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.Render;
import com.mrbbot.generic.render.RenderData;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Render object for a unit. Containing within a {@link RenderTile}. Always
 * added to the tile, but only visible if a unit exists on the tile.
 */
@ClientOnly
class RenderUnit extends RenderData<Unit> {
  /**
   * Constant for the height of a rocket body
   */
  private static final double ROCKET_HEIGHT = 1.2;
  /**
   * Constant for the height of a rocket engine
   */
  private static final double ROCKET_ENGINE_HEIGHT = 0.2;

  /**
   * Array containing the torsos of all the people. Stored so that the colours
   * can be changed when the unit changes. The torso colour is based on the
   * unit type.
   */
  private Cylinder[] torsos;
  /**
   * Array containing the belts of all the people. Stored so that the colours
   * can be changed when the unit changes. The belt colour is based on the
   * colour of the owning player.
   */
  private Cylinder[] belts;
  /**
   * Array containing the render objects that wrap all the components of a
   * person. There are 7 people representing each unit. The amount that are
   * shown depends on the health of the unit.
   */
  private Render[] people;
  /**
   * Render object representing a rocket. Only shown when the unit's type is
   * {@link UnitType#ROCKET}.
   */
  private Render rocket;
  /**
   * Render object for showing a unit's health.
   */
  private RenderHealthBar healthBar;

  RenderUnit(Unit data) {
    super(data);

    // Create arrays for render components
    torsos = new Cylinder[7];
    belts = new Cylinder[7];
    people = new Render[7];

    // Create the render objects for the people and rocket
    add(people[0] = buildPerson(0));
    add(rocket = buildRocket());
    for (int i = 0; i < 6; i++) {
      Render rotor = new Render();
      rotor.add(people[i + 1] = buildPerson(i + 1));
      // Pivot the person around the center
      rotor.translate.setX(0.5);
      rotor.rotateZ.setAngle(60 * i);
      add(rotor);
    }

    // Create and add the health bar
    add(healthBar = new RenderHealthBar(data, false));
  }

  /**
   * Creates a render object representing a person facing forward
   *
   * @param i index of this person (0 for center, 1 - 6 anticlockwise from
   *          right)
   * @return render object containing components representing a person
   */
  @SuppressWarnings("Duplicates")
  private Render buildPerson(int i) {
    Render person = new Render();

    // Build legs
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

    // Build torso
    Cylinder torso = new Cylinder(0.2, 0.4);
    torso.setMaterial(new PhongMaterial(Color.WHITE));
    torso.setTranslateZ(0.2 + 0.2);
    torso.setRotationAxis(Rotate.X_AXIS);
    torso.setRotate(90);
    person.add(torso);
    // Store torso so the colour can be changed later
    torsos[i] = torso;

    // Build belt
    Cylinder belt = new Cylinder(0.25, 0.05);
    belt.setMaterial(new PhongMaterial(Color.WHITE));
    belt.setTranslateZ(0.2 + 0.15);
    belt.setRotationAxis(Rotate.X_AXIS);
    belt.setRotate(90);
    person.add(belt);
    // Store belt so the colour can be changed later
    belts[i] = belt;

    // Build head
    Sphere head = new Sphere(0.3);
    head.setMaterial(new PhongMaterial(Color.LIGHTGOLDENRODYELLOW));
    head.setTranslateZ(0.2 + 0.4 + 0.27);
    person.add(head);

    // Build eyes
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

    // Make the person a bit smaller than it otherwise would be
    person.scaleTo(0.5);

    // Rotate the person that when it is pivoted, it will still be facing
    // forward
    double angle = 180;
    if (i > 0) angle -= (i - 1) * 60;
    person.rotateZ.setAngle(angle);

    return person;
  }

  /**
   * Creates a render object representing a rocket
   *
   * @return render object containing components representing a rocket
   */
  private Render buildRocket() {
    Render rocket = new Render();

    // Build rocket engine (bottom bit underneath body)
    Cylinder engine = new Cylinder(0.11, ROCKET_ENGINE_HEIGHT);
    engine.setTranslateZ(ROCKET_ENGINE_HEIGHT / 2);
    engine.setRotationAxis(Rotate.X_AXIS);
    engine.setRotate(90);
    engine.setMaterial(
      new PhongMaterial(UnitType.ROCKET.getColor().brighter())
    );
    rocket.add(engine);

    // Build rocket body
    Cylinder body = new Cylinder(0.22, ROCKET_HEIGHT);
    body.setTranslateZ(ROCKET_HEIGHT / 2 + ROCKET_ENGINE_HEIGHT);
    body.setRotationAxis(Rotate.X_AXIS);
    body.setRotate(90);
    body.setMaterial(new PhongMaterial(UnitType.ROCKET.getColor()));
    rocket.add(body);

    // Build nose cone
    Sphere cone = new Sphere(0.22);
    cone.setTranslateZ(ROCKET_HEIGHT + ROCKET_ENGINE_HEIGHT);
    cone.setMaterial(new PhongMaterial(UnitType.ROCKET.getColor()));
    rocket.add(cone);

    // Hide the rocket by default
    rocket.setVisible(false);

    return rocket;
  }

  /**
   * Update the render for the unit now placed on the tile. Sets the colour and
   * visibility of various components
   *
   * @param unit unit to take data for the update from
   */
  void updateRender(Unit unit) {
    if (unit != null) {
      // Show the rocket if this is the rocket type
      rocket.setVisible(unit.unitType == UnitType.ROCKET);

      // Build materials for the unit colours
      PhongMaterial torsoMaterial =
        new PhongMaterial(unit.unitType.getColor());
      PhongMaterial beltMaterial =
        new PhongMaterial(unit.player.getColour());

      // Show a proportionate amount of people for the health
      double healthPercent = unit.getHealthPercent();
      double onePersonProportion = 1.0 / (double) people.length;
      for (int i = 0; i < people.length; i++) {
        // Update the colours
        torsos[i].setMaterial(torsoMaterial);
        belts[i].setMaterial(beltMaterial);

        // Update the visibility for the health
        people[i].setVisible(healthPercent >= i * onePersonProportion);
      }
    }

    // Update the health bar render with new information
    healthBar.updateRender(unit);
  }
}
