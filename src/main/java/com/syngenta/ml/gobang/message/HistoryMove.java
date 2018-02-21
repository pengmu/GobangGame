package com.syngenta.ml.gobang.message;

public class HistoryMove {
	String player;
	short gridX;
	short gridY;
	public String getPlayer() {
		return player;
	}
	public void setPlayer(String player) {
		this.player = player;
	}
	public short getGridX() {
		return gridX;
	}
	public void setGridX(short gridX) {
		this.gridX = gridX;
	}
	public short getGridY() {
		return gridY;
	}
	public void setGridY(short gridY) {
		this.gridY = gridY;
	}
	
}
