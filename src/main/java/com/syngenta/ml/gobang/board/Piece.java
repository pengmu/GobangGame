package com.syngenta.ml.gobang.board;

public class Piece {
	short gridX;
	short gridY;
	
	public Piece(short x, short y){
		this.gridX=x;
		this.gridY=y;
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
