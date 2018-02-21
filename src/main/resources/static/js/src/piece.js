class Piece {
	constructor(gridX,gridY){
		this.gridX = gridX;
		this.gridY = gridY;
	}
	
	isSame(piece){
		return (this.gridX==piece.gridX && this.gridY==piece.gridY);
	}
}