package com.mrbbot.civilisation.logic.techs;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class TechTree {
  public static TechTree ROOT;
  public static int MAX_X = 0;

  static {
    Tech civilisation = new TechCustom("Civilisation", 0, 0, Color.GOLDENROD, "Settler", "Scout", "Warrior", "Worker");
    Tech agriculture = new TechCustom("Agriculture", 1, 0, Color.GREEN, "Farm");
    Tech forestry = new TechCustom("Forestry", 2, 0, Color.DARKGREEN, "Chop Forest");
    Tech mining = new TechCustom("Mining", 3, 2, Color.GREY, "Mine", "Wall");
    Tech pottery = new TechCustom("Pottery", 3, -2, Color.FIREBRICK, "Monument");
    Tech animalHusbandry = new TechCustom("Animal Husbandry", 3, -1, Color.PINK, "Pasture", "Stable", "Caravan");
    Tech theWheel = new TechCustom("The Wheel", 3, 0, Color.GOLDENROD, "Road");
    Tech archery = new TechCustom("Archery", 3, 1, Color.RED, "Archer");
    Tech currency = new TechCustom("Currency", 4, -2, Color.GOLD, "Bank");
    Tech dramaAndPoetry = new TechCustom("Drama and Poetry", 4, -1, Color.PURPLE, "Amphitheatre");
    Tech sailing = new TechCustom("Sailing", 4, 1, Color.DODGERBLUE, "Fishing Boat", "Trireme");
    Tech ironWorking = new TechCustom("Iron Working", 4, 2, Color.GREY, "Swordsman");
    Tech education = new TechCustom("Education", 5, 0, Color.LIGHTBLUE, "School", "University");
    Tech industrialisation = new TechCustom("Industrialisation", 6, -1, Color.BLACK, "Factory", "Railway");
    Tech steel = new TechCustom("Steel", 6, 1, Color.GREY.darker(), "Knight");
    Tech electricity = new TechCustom("Electricity", 7, -1, Color.YELLOW, "Power Station");
    Tech plastics = new TechCustom("Plastics", 7, 1, Color.PINK, "Supermarket");
    Tech rocketry = new TechCustom("Rocketry", 8, 0, Color.DARKBLUE, "Rocket");
    Tech future = new TechCustom("Future", 9, 0, Color.PURPLE);

    ROOT = new TechTree(
      civilisation,
      new TechTree(
        agriculture,
        new TechTree(
          pottery,
          new TechTree(currency),
          new TechTree(dramaAndPoetry)
        ),
        new TechTree(
          forestry,
          new TechTree(
            animalHusbandry,
            new TechTree(currency),
            new TechTree(
              dramaAndPoetry,
              new TechTree(
                education,
                new TechTree(
                  industrialisation,
                  new TechTree(
                    electricity,
                    new TechTree(
                      rocketry,
                      new TechTree(future)
                    )
                  ),
                  new TechTree(
                    plastics,
                    new TechTree(rocketry)
                  )
                ),
                new TechTree(
                  steel,
                  new TechTree(electricity),
                  new TechTree(plastics)
                )
              )
            )
          ),
          new TechTree(
            theWheel,
            new TechTree(education),
            new TechTree(sailing)
          ),
          new TechTree(archery)
        ),
        new TechTree(
          mining,
          new TechTree(
            ironWorking,
            new TechTree(steel)
          )
        )
      )
    );

    traverse(ROOT);
  }

  private static void traverse(TechTree tree) {
    int x = tree.tech.getX();
    if (x > MAX_X) MAX_X = x;
    for (TechTree child : tree.children) {
      traverse(child);
    }
  }

  public ArrayList<TechTree> parents;
  public Tech tech;
  public TechTree[] children;

  private TechTree(Tech tech, TechTree... children) {
    this.parents = new ArrayList<>();
    this.tech = tech;
    this.children = children;
    for (TechTree child : children) child.parents.add(this);
  }
}
