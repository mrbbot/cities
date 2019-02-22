package com.mrbbot.civilisation.net.packet;

public class PacketPurchaseTileRequest extends PacketUpdate {
  public int cityX, cityY, purchaseX, purchaseY;

  public PacketPurchaseTileRequest(int cityX, int cityY, int purchaseX, int purchaseY) {
    this.cityX = cityX;
    this.cityY = cityY;
    this.purchaseX = purchaseX;
    this.purchaseY = purchaseY;
  }
}
