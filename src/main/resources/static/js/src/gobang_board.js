class Board {
	
	constructor(client){
		this.highlighted_x=-1;
		this.highlighted_y=-1;
		this.highlight_color_y = "#f2f2f2";		
		this.highlight_color_x = "#0d0d0d";
		this.webClient = client;
		this.black_piece = new Image();
		this.black_piece.src="images/black_piece.png";
		this.white_piece = new Image();
		this.white_piece.src="images/white_piece.png";
		this.historyMove=new Array();
		this.hisotryMoveInd = 0;
	}



 initBoard(boardSize,gridNum,uidx,uidy,gameId,playMode){
	
	this.uidx = uidx;
	this.uidy = uidy;
	this.gameId = gameId;
	this.cellSize = boardSize/gridNum;
	var client = this.webClient;
	var oldCanvas = document.getElementById("piece");
	
	//remove all listeners
	var c=oldCanvas.cloneNode(true);
	oldCanvas.parentNode.replaceChild(c,oldCanvas);
	
	
	var board = this;
	console.log("game id "+board.gameId+" uidx "+board.uidx);
	var boardCanvas = document.getElementById("board");
	var ctx=boardCanvas.getContext("2d");
	ctx.strokeStyle="black";
	//ctx.fillStyle=this.board_color;
	
	this.occupiedPieces = new Array();
	this.playerXPieces = new Array();
	this.playerYPieces = new Array();
	this.turnX = 1;
	
	this.drawBoard(ctx,gridNum,boardSize);
	
	
	if(playMode){
		c.addEventListener('mousedown',function(evt){
			var mousePos = getMousePos(c,evt);
			var gridX = Math.round(mousePos.x/board.cellSize);
			var gridY = Math.round(mousePos.y/board.cellSize);
			var piece = new Piece(gridX,gridY);
			var pieceMove=null;
			if(!board.isOccupied(piece,board.occupiedPieces)){
				board.occupiedPieces[board.occupiedPieces.length]=piece;
				if(board.turnX){
					board.drawPiece(c,piece);
					board.turnX=0;
					pieceMove = new PieceMovMsg(piece,board.uidx,gameId);
				}else{
					board.drawPiece(c,piece);
					board.turnX=1;
					pieceMove = new PieceMovMsg(piece,board.uidy,gameId);
				}
			
				playerMove(client,pieceMove);
		}
		});
	
		c.addEventListener('mousemove',function(evt){
			var mousePos = getMousePos(c,evt);
			var gridX = Math.round(mousePos.x/board.cellSize) ;
			var gridY = Math.round(mousePos.y/board.cellSize) ;
			if(gridX>=1&&gridX<=15&&gridY>=1&&gridY<=15){
			var piece = new Piece(gridX,gridY);
				if(!board.isOccupied(piece,board.occupiedPieces)){
					board.highlightGrid(c,gridX,gridY,board.cellSize);
					/*
					var messageCanvas =  document.getElementById("messageWindow");
					var message = "GridX: "+gridX+" , "+"GridY: "+gridY;
					logMessage(messageCanvas,message);
					 */
				}
			}		
		});
	}
}
 
 drawBoard(ctx,gridNum,boardSize){
	 	//clear canvas
		ctx.clearRect(0,0,boardSize,boardSize);
		ctx.fillStyle="#cc9900";
		ctx.fillRect(0,0,boardSize,boardSize);
		var cellSize = boardSize/gridNum;
			for(var i=1;i<gridNum;i++){
				for(var j=1;j<gridNum;j++){
					ctx.moveTo(cellSize,cellSize*j);
					ctx.lineTo(boardSize-cellSize,cellSize*j);//horizon line
					ctx.stroke();	 
					ctx.moveTo(cellSize*i,cellSize);
					ctx.lineTo(cellSize*i,boardSize-cellSize);//vertical line			
					ctx.stroke();
				}
			}
		
 }
 
 highlightGrid(canvas,gridX,gridY,cellSize){
		if(this.highlighted_x!=gridX||this.highlighted_y!=gridY){
			
			var ctx=canvas.getContext("2d");
			var posX=(gridX-0.5)*cellSize;
			var posY=(gridY-0.5)*cellSize;				
			
			//clear previous highlighted cell if it is not occupied
			var highlighted_piece = new Piece(this.highlighted_x,this.highlighted_y);
			if(!this.isOccupied(highlighted_piece,this.occupiedPieces)){
				this.clearGrid(canvas,this.highlighted_x,this.highlighted_y,cellSize);
			}
			
			//highlight new cell
			if(this.turnX==1){
				ctx.fillStyle=this.highlight_color_x;
			}else{
				ctx.fillStyle=this.highlight_color_y;
			}
			ctx.globalAlpha = 0.8;
			ctx.fillRect(posX+cellSize/4,posY+cellSize/4,cellSize/2,cellSize/2);
			ctx.fillStyle=this.board_color;
			ctx.globalAlpha = 1.0;
			this.highlighted_x = gridX;
			this.highlighted_y = gridY;
		}
	}
 
 clearGrid(canvas,gridX,gridY,cellSize){
		if(gridX>0&&gridY>0){
			var ctx=canvas.getContext("2d");
			var posX=(gridX-0.5)*cellSize;
			var posY=(gridY-0.5)*cellSize;
			/*
			if((gridX+gridY)%2==0)
				ctx.fillStyle="white";
			else
				ctx.fillStyle=this.board_color;
				*/
			ctx.clearRect(posX,posY,cellSize,cellSize);
			
		}
 }
 /*
 clearPiece(canvas,gridX,gridY,cellSize){
		if(gridX>0&&gridY>0){
			var ctx=canvas.getContext("2d");
			var posX=(gridX-1)*cellSize;
			var posY=(gridY-1)*cellSize;
			if((gridX+gridY)%2==0)
				ctx.fillStyle="white";
			else
				ctx.fillStyle=this.board_color;
			ctx.fillRect(posX,posY,cellSize,cellSize);
			
		}
}
*/
 
 loadHistorySet(setNum,gameId){
	var board = this;
	if(setNum>0){	
		//console.log("load set "+setNum+" for game "+gameId);
		board.webClient.stompClient.send("/gobang/loadSet", {}, 
	            JSON.stringify({'setNum':setNum,'gameId':gameId}));
		
		
	}	
	
 }
 
 resetGame(gameId){
	 this.webClient.stompClient.send("/gobang/resetGame", {}, 
	            JSON.stringify({'gameId':gameId}));
	
 }
 
 nextMove(){
	 //this.webClient.stompClient.send("/gobang/historyMove", {},null	);
	 var board = this;
	 //console.log("game id "+board.gameId+" uidx "+board.uidx);
	 if(this.historyMoveInd<this.historyMove.length){
		 var move = this.historyMove[this.historyMoveInd++];
		 var player = move.charAt(0);
		 var uid;
		 var c = document.getElementById("piece");
		
		 var gridX = board.convertGridNum(move.charAt(2));
		 var gridY = board.convertGridNum(move.charAt(3));
		 if(gridX>0&&gridY>0){
			 console.log("next move : "+move+" grid : "+gridX+" "+gridY);
		 
			 var piece = new Piece(gridX,gridY);
			 var pieceMove;
			 board.drawPiece(c,piece);
			 if(player=='B'){
				 uid = board.uidx;			 			 
				 board.turnX=0;
			 
			 }else{
				 uid = board.uidy;
				 board.turnX=1;
			
			 }
			 pieceMove = new PieceMovMsg(piece,uid,board.gameId);
			 playerMove(board.webClient,pieceMove);
		}
		 
	 }else{
		 console.log("already last move!");
	 }
	 
 }
 
 convertGridNum(gridChar){
	 return (gridChar.charCodeAt(0) - 'a'.charCodeAt(0)+1);
 }
 
 drawPiece(canvas,piece){
	 	var pieceImg;
	 	if( this.turnX){
	 		pieceImg = this.black_piece;
	 	}else{
	 		pieceImg = this.white_piece;
	 	}
	  	var cellSize = this.cellSize;
		var posX=(piece.gridX-0.5)*cellSize;
		var posY=(piece.gridY-0.5)*cellSize;	
		this.clearGrid(canvas,piece.gridX,piece.gridY,cellSize);
		canvas.getContext('2d').drawImage(pieceImg,posX+5,posY+8);
 }
 
 isOccupied(piece,occupiedPieces){
		for(var i=0;i<occupiedPieces.length;i++){
			if(piece.isSame(occupiedPieces[i])){
				return 1;
			}
		}
		return 0;
 }


}

function  getMousePos(canvas,evt){
	 var rect = canvas.getBoundingClientRect();
     return {
       x: evt.clientX - rect.left,
       y: evt.clientY - rect.top
     };
}  

function  logMessage(canvas, message) {
    var context = canvas.getContext('2d');
    context.clearRect(0, 0, canvas.width, canvas.height);
    context.font = '18pt Calibri';
    context.fillStyle = 'black';
    context.fillText(message, 10, 25);
}

function playerMove(client,pieceMov){
	client.stompClient.send("/gobang/playerMove", {}, 
            JSON.stringify({'gridX':pieceMov.gridX, 'gridY':pieceMov.gridY,"uid":pieceMov.uid,"gameId":pieceMov.gameId}));
      
}

	