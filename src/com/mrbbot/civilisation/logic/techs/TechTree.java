package com.mrbbot.civilisation.logic.techs;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class TechTree {
  public static TechTree ROOT;

  static {
    Tech farming = new TechCustom("Farming", 0, 0, Color.GREEN, "Farm", "Pasture");
    Tech pottery = new TechCustom("Pottery", 1, -1, Color.FIREBRICK, "Monument");
    Tech writing = new TechCustom("Writing", 2, -1, Color.DODGERBLUE, "Library", "Great Library", "Something else");
    Tech mining = new TechCustom("Mining", 1, 0, Color.GREY, "Mine", "Wall", "House");
    Tech archery = new TechCustom("Archery", 1, 1, Color.RED, "Archer");
    Tech futureTech = new TechCustom("Future", 6, -1, Color.PURPLE);

    ROOT = new TechTree(
      farming,
      new TechTree(
        pottery,
        new TechTree(
          writing,
          new TechTree(futureTech)
        )
      ),
      new TechTree(
        mining,
        new TechTree(writing),
        new TechTree(futureTech)
      ),
      new TechTree(archery)
    );
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
