package com.mrbbot.civilisation.logic;

import java.util.Map;

/**
 * Interface describing something that stores the state of itself in a map so
 * that it can be restored later. Used for sending the state over a network or
 * for storing it in a file.
 */
public interface Mappable {
  Map<String, Object> toMap();
}
