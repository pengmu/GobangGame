class PieceMovMsg {
	constructor(piece,uid,gameId){
		this.gridX = piece.gridX;
		this.gridY = piece.gridY;
		this.uid = uid;
		this.gameId = gameId;
	}
}

class ResultMsg{
	constructor(uid,winn){
		this.uid = uid;
		this.winn = winn;
	}
}