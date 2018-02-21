package com.syngenta.ml.gobang.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.syngenta.ml.gobang.message.*;
import com.syngenta.ml.gobang.board.*;


@Controller
public class GobangController implements ApplicationListener<ApplicationEvent> {
	private static Logger logger = LoggerFactory.getLogger(GobangController.class);
	private static ArrayList<String> redWin = new ArrayList<String>();
	private static ArrayList<String> blackWin = new ArrayList<String>();
	static {
		//initialize redWin set
		File dataFile1 = new File("C:/Syngenta/training/ML/gobang_data/tmp/BlackWin.sgf");
		File dataFile2 = new File("C:/Syngenta/training/ML/gobang_data/tmp/WhiteWin.sgf");
		try {
			Files.lines(dataFile1.toPath(), Charset.forName("GB18030")).map(s -> s.trim())
			.forEach(
					s->redWin.add(s)
					);
			Files.lines(dataFile2.toPath(), Charset.forName("GB18030")).map(s -> s.trim())
			.forEach(
					s->blackWin.add(s)
					);
		} catch (IOException e) {
			logger.error("Error :"+e);
		}
		
	}
    private HashMap<String, GobangBoard> gameBoards = new HashMap<String, GobangBoard>();
	
	@RequestMapping("/gobang")
    public String gobang(Model model) {
		
    	Random r = new Random();
    	int high = 99999;
    	int low =10000;
    	int uidx = r.nextInt(high-low) + low;
        model.addAttribute("uidX", uidx);
        int uidy = r.nextInt(high-low) + low;
        model.addAttribute("uidY", uidy);
        String gameId = uidx+"_"+uidy;
		logger.info("game id : "+ gameId);
        model.addAttribute("gameId",gameId);
        
        GobangBoard board = new GobangBoard();
		board.addUser(uidx);
		board.addUser(uidy);
		gameBoards.put(gameId, board);
        
        return "gobang";
    }

    @MessageMapping("/playerMove")
    @SendToUser
    public Result movePiece(@Payload PieceMove msg) throws Exception{
    	//String sessionId = headerAccessor.getSessionId();
    	//logger.info("sessionId[ "+sessionId+"] player["+msg.getUid()+"] moves : X ("+msg.getGridX()+") Y ("+msg.getGridY()+")");
    	String gameId = msg.getGameId();
    	GobangBoard board = null;
    	if(gameBoards.get(gameId)==null){
    		 board = new GobangBoard();
    		board.addUser(msg.getUid());
    		gameBoards.put(gameId, board);
    		
    	}else{
    		 board = gameBoards.get(gameId);
    		if(!board.containsUser(msg.getUid())){
    			board.addUser(msg.getUid());
    		}
    	}
    	Piece piece = new Piece(msg.getGridX(),msg.getGridY());
    	Result result = new Result();
    	result.setUid(msg.getUid());
    	if(board.movePiece(msg.getUid(), piece)){
    		logger.info("player ["+msg.getUid()+"] wins!");
    		result.setWin(true);
    		board.clearBoard();
    		
    	}
    	return result;
    }
    /*
    @MessageMapping("/historyMove")
    @SendToUser
    public HistoryMove historyMove(SimpMessageHeaderAccessor headerAccessor) throws Exception{
    	String sessionId = headerAccessor.getSessionId();
    	GobangBoard board = gameBoards.get(sessionId);
    	HistoryMove move = new HistoryMove();
    	if(board.getHistoryMoveIndex()%2 == 0){
    		move.setPlayer("RED");
    	}else{
    		move.setPlayer("BLACK");
    	}
    	if(board!=null){
    		short[] nextMove = board.nextHistoryMove();
    		move.setGridX(nextMove[0]);
    		move.setGridY(nextMove[1]);
    		logger.info("next move: "+move.getGridX()+"  "+move.getGridY());
    	}
    	
    	return move;
    }
    */
    
    @MessageMapping("/loadSet")
    @SendToUser
    public HistorySet loadHistorySet(@Payload LoadSet set){
    	int setNum = set.getSetNum();
    	HistorySet historySet = new HistorySet();
    	logger.info("load history set : "+setNum + " for game id: "+set.getGameId());
    	String gameId = set.getGameId();
    	GobangBoard board = gameBoards.get(gameId);  	
    	if(board!=null){
    		board.clearBoard();
    		historySet.setMoves(blackWin.get(setNum));
    		logger.info("load history moves :"+historySet.getMoves());
    		board.setHistorySet(blackWin.get(setNum));
    	}
    	return historySet; 
    }
    
    
    @MessageMapping("/resetGame")
    public void restGame(@Payload GameID gameId) throws Exception{    	
    	String id = gameId.getGameId();
    	 GobangBoard board = gameBoards.get(id);
 		if(board!=null){
 			logger.info("clear game : "+id);
 			board.clearBoard(); 			
 		}    	
    }
    /*
    @RequestMapping("/loadData")
    public void loadData() throws Exception{
    	File dataFolder = new File("C:/Syngenta/training/ML/gobang_data");
    	File[] listOfFiles = dataFolder.listFiles();
    	int blackWin =1;
    	int whiteWin =1;
    	int draw =1;
    	for (int i = 0; i < listOfFiles.length; i++) {
    		File temp;
    	      if (listOfFiles[i].isFile()) {
    	    	  String fileName = listOfFiles[i].getName();
    	    	  if(fileName.contains("黑胜")){
    	    		  temp = new File( dataFolder.getAbsolutePath()+"/black"+(blackWin++)+".sgf");
    	    		  listOfFiles[i].renameTo(temp);
    	    	  }else if(fileName.contains("白胜")){
    	    		  temp = new File(dataFolder.getAbsolutePath()+"/white"+(whiteWin++)+".sgf");
    	    		  listOfFiles[i].renameTo(temp);
    	    	  }else if(fileName.contains("和棋")){
    	    		  temp = new File(dataFolder.getAbsolutePath()+"/draw"+(draw++)+".sgf");
    	    		  listOfFiles[i].renameTo(temp);
    	    	  }
    	    	 
    	      } else if (listOfFiles[i].isDirectory()) {
    	    	  logger.info("Directory " + listOfFiles[i].getName());
    	      }
    	    }
    }
    
    
    @RequestMapping("/formatData")
    public void formatData() throws Exception{
    	File dataFolder = new File("C:/Syngenta/training/ML/gobang_data");
    	File blackWinFile = new File("C:/Syngenta/training/ML/gobang_data/tmp/BlackWin.sgf");
    	File whiteWinFile = new File("C:/Syngenta/training/ML/gobang_data/tmp/WhiteWin.sgf");
    	FileWriter blackWriter = new FileWriter(blackWinFile);
    	FileWriter whiteWriter = new FileWriter(whiteWinFile);
    	File[] listOfFiles = dataFolder.listFiles();
    	for (int i = 0; i < listOfFiles.length; i++) {
    		File dataFile = listOfFiles[i];    		
    		if(dataFile.getName().startsWith("black")){
    			Files.lines(dataFile.toPath(), Charset.forName("GB18030")).map(s -> s.trim())
    				.forEach(
    						s->writeString(new BufferedWriter(blackWriter),replaceString(s)+"\n")
    						);
    		}else if(dataFile.getName().startsWith("white")){
    			Files.lines(dataFile.toPath(), Charset.forName("GB18030")).map(s -> s.trim())
				.forEach(        	
        			s->writeString(new BufferedWriter(whiteWriter),replaceString(s)+"\n")
        		);
    		}
    	}
    }
    
    
    private  void writeString(BufferedWriter writer,String s){
    	try{
    		writer.append(s);
    		writer.flush();
    	}catch (Exception e){
    		logger.error("Exception: ",e );
    	}
    }
    private String replaceString(String s){
    	String result = s;
    	int index = s.indexOf(";B[");
    	result = s.substring(index+1, s.length()-1);
    	return result;
    }
    */

	@Override
	public void onApplicationEvent(ApplicationEvent evt) {
		if(evt instanceof SessionDisconnectEvent){
			SessionDisconnectEvent event = (SessionDisconnectEvent)evt;
			StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
			logger.info("Disconnect event [sessionId: " + sha.getSessionId()+"], remove game..." );
			gameBoards.put(sha.getSessionId(), null);
			gameBoards.remove(sha.getSessionId());
		}else if (evt instanceof SessionConnectEvent){
			SessionConnectEvent event = (SessionConnectEvent) evt;
			StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
			String sid = sha.getSessionId();
			logger.info("Connect event [sessionId: " + sid+"]" );
			if(!gameBoards.containsKey(sid)){
				gameBoards.put(sid, null);
			}
		}
		
		
	}
    
    
    
}
