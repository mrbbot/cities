package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted when a player attempts to buy a tile. On receiving this
 * packet, the game should check if the player can purchase that tile and if
 * they can, bring the tile into the city's territory.
 */
public class PacketPurchaseTileRequest extends PacketUpdate {
  /**
   * X-coordinate of the city the player is trying to expand
   */
  public int cityX;
  /**
   * Y-coordinate of the city the player is trying to expand
   */
  public int cityY;
  /**
   * X-coordinate of the tile the player is trying to purchase
   */
  public int purchaseX;
  /**
   * Y-coordinate of the tile the player is trying to purchase
   */
  public int purchaseY;

  public PacketPurchaseTileRequest(
    int cityX,
    int cityY,
    int purchaseX,
    int purchaseY
  ) {
    this.cityX = cityX;
    this.cityY = cityY;
    this.purchaseX = purchaseX;
    this.purchaseY = purchaseY;
  }
}
