package com.syngenta.ml.gobang.message;

public class PieceMove {
	short gridX;
	short gridY;
	int uid;
	String gameId;
	
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
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	
}
