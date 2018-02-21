package com.syngenta.ml.gobang.message;

public class Result {
	long uid;
	boolean win=false;

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}
	
	
}
