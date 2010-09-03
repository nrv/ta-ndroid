/*
 * Copyright (C) 2010 NRV - nherve75@gmail.com
 * 
 * This file is part of SpringLobbyProtocol.
 * 
 * SpringLobbyProtocol is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 * 
 * SpringLobbyProtocol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SpringLobbyProtocol. If not, see http://www.gnu.org/licenses/
 * 
 */

package com.springrts.data;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class SpringAccount {
	private int internalId;
	private String username;
	private long lastTimeSeen;
	private boolean currentlyOnline;
	
	public SpringAccount() {
		super();
		currentlyOnline = false;
	}
	
	public SpringAccount(String username) {
		this();
		this.username = username;
		this.lastTimeSeen = -1;
		this.internalId = -1;
	}
	
	public int getInternalId() {
		return internalId;
	}
	public void setInternalId(int internalId) {
		this.internalId = internalId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public long getLastTimeSeen() {
		return lastTimeSeen;
	}
	public void setLastTimeSeen(long lastTimeSeen) {
		this.lastTimeSeen = lastTimeSeen;
	}

	public boolean isCurrentlyOnline() {
		return currentlyOnline;
	}
	
	public void seenNow() {
		setLastTimeSeen(System.currentTimeMillis());
	}

	public void setCurrentlyOnline(boolean currentlyOnline) {
		this.currentlyOnline = currentlyOnline;
		seenNow();
	}
	
	public String shortDisplay() {
		return shortDisplay("online", " m", " h", " d");
	}
	
	public String seen(String min, String hour, String day) {
		String r = "";
		long diffmin = System.currentTimeMillis() - getLastTimeSeen();
		diffmin /= (60 * 1000);
		if (diffmin < 60) {
			if (diffmin < 10) {
				r += " ";
			}
			r += diffmin + " " + min;
		} else if (diffmin < 1440) {
			if (diffmin < 600) {
				r += " ";
			}
			r += (diffmin / 60) + " " + hour;
		} else {
			if (diffmin < 14400) {
				r += " ";
			}
			r += (diffmin / (60 * 24)) + " " + day;
		}
		return r;
	}
	
	private String spaces(int n) {
		String r = "";
		for (int i = 0; i < n; i++) {
			r += " ";
		}
		return r;
	}
	
	public String shortDisplay(String online, String min, String hour, String day) {
		String r = "[ ";
		
		if (isCurrentlyOnline()) {
			r += online;
		} else {
			if (getLastTimeSeen() > 0) {
				r += spaces(online.length() - (min.length() + 1));
				r += seen(min, hour, day);
			} else {
				r += spaces(online.length());
			}
		}
		
		r+= " ] " + getUsername();
		
		return r;
	}

	public String toString() {
		return "SpringAccount [username=" + username + "]";
	}
}
