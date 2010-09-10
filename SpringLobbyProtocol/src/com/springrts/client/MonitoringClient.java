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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springrts.data.SpringAccount;
import com.springrts.data.SpringAccountList;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class MonitoringClient extends PingClient {
	private SpringAccountList connectedPlayers;
	private SpringAccountList friends;
	private Map<String, Pattern> usernamePatterns;
	private int nbFriendsOnline;
	private MonitoringApplication application;

	public MonitoringClient(MonitoringApplication application) {
		super();

		connectedPlayers = new SpringAccountList();
		friends = new SpringAccountList();
		usernamePatterns = new HashMap<String, Pattern>();
		nbFriendsOnline = 0;

		this.application = application;
	}
	
	public void loadParameters() throws ProtocolException {
		friends = persistence.loadFriends();
		context = persistence.loadConnectionContext();
		usernamePatterns = persistence.loadUsernamePatterns();
	}

	public void addFriend(String username) {
		username = username.trim().toUpperCase();
		SpringAccount friend = new SpringAccount(username);
		friends.put(friend);
		if (connectedPlayers.contains(username)) {
			friendOnline(username);
		}
		
		try {
			persistence.saveFriends(friends);
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}
	
	public void addClan(String c) {
		addUsernamePattern("\\["+ c +"\\].*");
	}
	
	public void addUsernamePattern(String p) {
		Pattern pt = Pattern.compile(p, Pattern.CASE_INSENSITIVE);
		usernamePatterns.put(p, pt);
	}
	
	public void removeUsernamePattern(String p) {
		usernamePatterns.remove(p);
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
	
	public void notifyDisconnected() {
		super.notifyDisconnected();
		
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
		synchronized (connectedPlayers) {
			connectedPlayers.put(new SpringAccount(username));
			if (!friends.contains(username)) {
				for (Pattern p : usernamePatterns.values()) {
					Matcher m = p.matcher(username);
					if (m.matches()) {
						addFriend(username);
						break;
					}
				}
			} else {
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
		
		try {
			persistence.saveFriends(friends);
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}

	public void pcBroadcast(String msg) {
		hardware.log(msg);
	}

	public void pcServerMessage(String msg) {
		hardware.log(msg);
	}

	public void pcServerMessageBox(String msg) {
		hardware.log(msg);
	}

	public void pcRegistrationAccepted() {
		// Ignore
	}

	public void pcRegistrationDenied(String reason) {
		// Ignore
	}

	public void pcAgreementEnd() {
		// Ignore
	}

	public Map<String, Pattern> getUsernamePatterns() {
		return usernamePatterns;
	}

	public void setUsernamePatterns(Map<String, Pattern> usernamePatterns) {
		this.usernamePatterns = usernamePatterns;
	}

}
