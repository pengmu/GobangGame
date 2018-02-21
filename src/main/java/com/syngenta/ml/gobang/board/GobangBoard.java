package com.syngenta.ml.gobang.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syngenta.ml.gobang.message.PieceMove;

public class GobangBoard {
	private static Logger logger = LoggerFactory.getLogger(GobangBoard.class);
	HashSet<Short> availablePieces;
	ArrayList<Short> horAlignment;
	HashMap<Integer, ArrayList<Short>> playerPieces;
	String[] historySet;
	int historySetIndex=0;

	public GobangBoard() {
		playerPieces = new HashMap<Integer, ArrayList<Short>>();
		initAvailablePieces();
		// initialize horizontal alignment : "111110000..." "01111100..."
		// ..."0000...11111"
		short align = (short) (Math.pow(2, 5) - 1);
		horAlignment = new ArrayList<Short>();
		horAlignment.add(align);
		for (byte i = 1; i <= 10; i++) {
			horAlignment.add((short) (align << i));
		}
	}
	
	private void initAvailablePieces(){
		availablePieces = new HashSet<Short>();
		// convert piece position to number 1 - 225 (15*15)
		for (short i = 1; i <= 225; i++) {
			availablePieces.add(i);
		}
	}

	public void addUser(int uid) {
		ArrayList<Short> placedPositions = new ArrayList<Short>();
		for (byte i = 0; i < 15; i++) {
			placedPositions.add((short) 0);
		}
		playerPieces.put(uid, placedPositions);
	}

	public boolean containsUser(int uid) {
		return playerPieces.containsKey(uid);
	}

	public boolean movePiece(int uid, Piece piece) {
		short posY = convertPiece(piece);
		short prePos = playerPieces.get(uid).get(piece.gridY - 1);
		// perform bit or operation to get new position
		playerPieces.get(uid).set(piece.gridY - 1, (short) (prePos | posY));
		availablePieces.remove((piece.gridX - 1) * 15 + piece.gridY);
		return checkWinningCondition(playerPieces.get(uid));
	}
	
	public void clearBoard(){
		logger.info("clearing board..");
		initAvailablePieces();
		playerPieces.clear();
		this.historySetIndex=0;
		
	}

	/**
	 * Scan board and check positions
	 * 
	 * @param playerPieces
	 * @return
	 */
	private boolean checkWinningCondition(ArrayList<Short> playerPieces) {
		// check horizontal positions
		for (byte i = 0; i < 15; i++) {
			short pos = playerPieces.get(i);
			// check horizontal alignment
			for (byte j = 0; j < horAlignment.size(); j++) {
				if ((pos & horAlignment.get(j)) == horAlignment.get(j)) {
					logger.info("horizontal alignment win! alignement at "+i+1+" row, aligment "+pos);
					return true;
				}
			}

		}

		// check vertical and diagonal positions
		for (byte i = 0; i + 4 < 15; i++) {
			// check vertical
			short verti_pos = (short) (playerPieces.get(i)
					& playerPieces.get(i + 1) & playerPieces.get(i + 2)
					& playerPieces.get(i + 3) & playerPieces.get(i + 4));
			if (verti_pos > 0) {
				logger.info("vertial alignment win!");
				return true;
			}

			// check diagonal
			short dia_pos_right = (short) (playerPieces.get(i)
					| (playerPieces.get(i) >> 1 & playerPieces.get(i + 1))
					| (playerPieces.get(i) >> 2 & playerPieces.get(i + 2))
					| (playerPieces.get(i) >> 3 & playerPieces.get(i + 3)) | (playerPieces
					.get(i) >> 4 & playerPieces.get(i + 4)));

			for (byte j = 0; j < horAlignment.size(); j++) {
				if ((dia_pos_right & horAlignment.get(j)) == horAlignment
						.get(j)) {
					// make sure every row contributes at least 1 piece
					dia_pos_right = horAlignment.get(j);
					short bitMask = (short) (((dia_pos_right>>>1) ^ dia_pos_right) & dia_pos_right);
						if (((bitMask >> 1 & playerPieces.get(i + 1)) > 0)
							&& ((bitMask >> 2 & playerPieces.get(i + 2)) > 0)
							&& ((bitMask >> 3 & playerPieces.get(i + 3)) > 0)
							&& ((bitMask >> 4 & playerPieces.get(i + 4)) > 0)) {
						logger.info("diagonal right alignment win ! starting from "+(i+1)  +" row , "+Integer.toBinaryString(dia_pos_right)+" bitMask: "+Integer.toBinaryString(bitMask));
						return true;
					}
				}
			}

			short dia_pos_left = (short) (playerPieces.get(i)
					| (playerPieces.get(i) << 1 & playerPieces.get(i + 1))
					| (playerPieces.get(i) << 2 & playerPieces.get(i + 2))
					| (playerPieces.get(i) << 3 & playerPieces.get(i + 3)) | (playerPieces
					.get(i) << 4 & playerPieces.get(i + 4)));

			for (byte j = 0; j < horAlignment.size(); j++) {
				if ((dia_pos_left & horAlignment.get(j)) == horAlignment.get(j)) {
					// make sure every row contributes at least 1 piece
					dia_pos_left = horAlignment.get(j);
					short bitMask = (short) (((dia_pos_left<<1) ^ dia_pos_left) & dia_pos_left);
					if (((bitMask << 1 & playerPieces.get(i + 1)) > 0)
							&& ((bitMask << 2 & playerPieces.get(i + 2)) > 0)
							&& ((bitMask << 3 & playerPieces.get(i + 3)) > 0)
							&& ((bitMask << 4 & playerPieces.get(i + 4)) > 0)) {
						logger.info("diagonal left alignment win ! starting from "+(i+1)  +" row , "+Integer.toBinaryString(dia_pos_left)+" bitMask: "+Integer.toBinaryString(bitMask));
						return true;
					}
				}
			}

		}

		return false;
	}

	/**
	 * Convert piece position to short number that represents the grid in bit
	 * order (e.g. (1,1) as 1 0 0 0 0 0 ...0 )
	 * 
	 * @param piece
	 * @return
	 */
	private short convertPiece(Piece piece) {

		return (short) Math.pow(2, 15 - piece.gridX);

	}

	public String[] getHistorySet() {
		return historySet;
	}

	public void setHistorySet(String setMoveString) {
		this.historySet = setMoveString.split(";");
	}
	
	public short[] nextHistoryMove(){
		String nextMove = historySet[historySetIndex].substring(2,4);
		historySetIndex++;
		return convertToGrid(nextMove);		
	}
	
	public int getHistoryMoveIndex(){
		return this.historySetIndex;
	}
	
	private short[] convertToGrid(String move){
		short gridx;
		short gridy;
		gridx = charToInt( move.charAt(0));
		gridy = charToInt(move.charAt(1));
		return new short[]{gridx,gridy};
	}
	
	private short charToInt(char ch){
		short num=0;
		switch (ch){
			case 'a': num=1;break;
			case 'b': num=2;break;
			case 'c': num=3;break;
			case 'd': num=4;break;
			case 'e': num=5;break;
			case 'f': num=6;break;
			case 'g': num=7;break;
			case 'h': num=8;break;
			case 'i': num=9;break;
			case 'j': num=10;break;
			case 'k': num=11;break;
			case 'l': num=12;break;
			case 'm': num=13;break;
			case 'n': num=14;break;
			case 'o': num=15;break;			
		}
		return num;
	}
	
	

}
