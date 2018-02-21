class WebClient  {
	constructor(){
		this.socket = new SockJS('/game');
		this.stompClient = Stomp.over(this.socket);
	}
	


 sendMessage(msg) {
	this.stompClient.send("/app/gobang", {}, JSON.stringify({
		'gridX' : msg.gridX,
		'text' : msg.gridY,
		'uid': msg.uid
	}));
 }

}