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

package com.springrts.client;

import java.util.ArrayList;
import java.util.List;

import com.springrts.data.SpringAccount;
import com.springrts.data.SpringAccountList;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class MonitoringClient extends RegisteringClient {
	private SpringAccountList connectedPlayers;
	private SpringAccountList friends;
	private int nbFriendsOnline;
	private MonitoringApplication application;

	public MonitoringClient(MonitoringApplication application) {
		super();

		connectedPlayers = new SpringAccountList();
		friends = new SpringAccountList();
		nbFriendsOnline = 0;

		this.application = application;
	}

	public void addFriend(String username) {
		username = username.trim().toUpperCase();
		SpringAccount friend = new SpringAccount(username);
		friends.put(friend);
		if (connectedPlayers.contains(username)) {
			friendOnline(username);
		}
	}

	private void friendOffline(String username) {
		synchronized (friends) {
			friends.get(username).setCurrentlyOnline(false);
			nbFriendsOnline--;
			application.notifyFriendsOnlineChanged();
			application.notifyFriendDisconnected(friends.get(username));
		}
	}

	private void friendOnline(String username) {
		synchronized (friends) {
			friends.get(username).setCurrentlyOnline(true);
			nbFriendsOnline++;
			application.notifyFriendsOnlineChanged();
			application.notifyFriendConnected(friends.get(username));
		}
	}

	public List<SpringAccount> getActiveFriendsSince(long nbMinutes) {
		long aFewTimesAgo = System.currentTimeMillis() - nbMinutes * 60 * 1000;
		List<SpringAccount> l = new ArrayList<SpringAccount>();
		for (SpringAccount act : friends) {
			if (act.isCurrentlyOnline() || (act.getLastTimeSeen() > aFewTimesAgo)) {
				l.add(act);
			}
		}
		return l;
	}

	public SpringAccountList getFriends() {
		return friends;
	}

	public List<SpringAccount> getFriendsOnline() {
		List<SpringAccount> l = new ArrayList<SpringAccount>();
		for (SpringAccount act : friends) {
			if (act.isCurrentlyOnline()) {
				l.add(act);
			}
		}
		return l;
	}

	public int getNbFriendsOnline() {
		return nbFriendsOnline;
	}

	public void notifyConnected() {
		application.notifyConnected();
	}
	
	public synchronized void notifyDisconnected() {
		application.notifyDisconnected();
		
		for (SpringAccount act : friends) {
			if (act.isCurrentlyOnline()) {
				act.setCurrentlyOnline(false);
			}
		}
		nbFriendsOnline = 0;
		application.notifyFriendsOnlineChanged();
	}

	public void pcAccepted(String username) {
		super.pcAccepted(username);
		application.notifyLogin();
	}

	public void pcAddUser(String username, String country, String cpu, String accountId) {
		username = username.trim().toUpperCase();
		hardware.dbg("pcAddUser " + username);
		synchronized (connectedPlayers) {
			connectedPlayers.put(new SpringAccount(username));
			if (friends.contains(username)) {
				friendOnline(username);
			}
		}
	}

	public void pcAgreement(String agreement) {
		// Ignore
	}

	public void pcBattleOpened(String battleId, String type, String natType, String founder, String ip, String port, String maxPlayers, String passworded, String rank, String mapHash, String mapName, String title, String modName) {
		// Ignore
	}

	public void pcClientStatus(String username, String status) {
		// Ignore
	}

	public void pcDenied(String reason) {
		super.pcDenied(reason);
		disconnect();
		application.notifyAccessDenied(reason);
	}

	public void pcJoinedBattle(String battleId, String username, String scriptPassword) {
		// Ignore
	}

	public void pcLoginInfoEnd() {
		super.pcLoginInfoEnd();
		application.notifyLoginEnd();
		application.notifyFriendsOnlineChanged();
	}

	public void pcMotd(String message) {
		// Ignore
	}

	public void pcNotImplemented(String command) {
		// Ignore
	}

	public void pcRemoveUser(String username) {
		username = username.trim().toUpperCase();
		hardware.dbg("pcRemoveUser " + username);
		synchronized (connectedPlayers) {
			connectedPlayers.remove(username);
			if (friends.contains(username)) {
				friendOffline(username);
			}
		}
	}

	public void pcUpdateBattleInfo(String battleId, String spectatorCount, String locked, String mapHash, String mapName) {
		// Ignore
	}

	public void removeFriend(String username) {
		username = username.trim().toUpperCase();
		if (connectedPlayers.contains(username)) {
			friendOffline(username);
		}
		friends.remove(username);
	}

}
