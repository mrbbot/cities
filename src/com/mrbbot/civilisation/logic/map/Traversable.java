package com.mrbbot.civilisation.logic.map;

public interface Traversable {
    int getX();
    int getY();
    int getCost();
    boolean canTraverse();
}
