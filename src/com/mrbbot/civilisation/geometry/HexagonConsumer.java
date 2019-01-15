package com.mrbbot.civilisation.geometry;

@FunctionalInterface
public interface HexagonConsumer<T> {
   void accept(T t, Hexagon hex, int x, int y);
}
