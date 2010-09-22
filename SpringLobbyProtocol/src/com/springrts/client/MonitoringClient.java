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
import com.springrts.data.UsernamePattern;
import com.springrts.data.UsernamePatternList;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class MonitoringClient extends PingClient {
	private SpringAccountList connectedPlayers;
	private SpringAccountList persistentFriends;
	private UsernamePatternList usernamePatterns;
	private int nbFriendsOnline;
	private MonitoringApplication application;

	public MonitoringClient(MonitoringApplication application) {
		super();

		connectedPlayers = new SpringAccountList();
		persistentFriends = new SpringAccountList();
		usernamePatterns = new UsernamePatternList();
		nbFriendsOnline = 0;

		this.application = application;
	}

	public void addClanToMonitor(String c) {
		c = c.trim().toUpperCase();
		String pt = ".*\\[" + c + "\\].*";
		String dp = "*[" + c + "]*";
		if (!usernamePatterns.contains(dp)) {
			addUsernamePattern(pt, dp);
		}

	}

	public void addFriendToMonitor(String username) {
		username = username.trim().toUpperCase();
		if (!persistentFriends.contains(username) && !username.equalsIgnoreCase(remote.getContext().getLogin())) {
			SpringAccount friend = new SpringAccount(username);
			persistentFriends.put(friend);
			addUsernamePattern("^" + username + "$", username);
			if (connectedPlayers.contains(username)) {
				friendOnline(username);
			}

			try {
				saveFriends();
			} catch (ProtocolException e) {
				hardware.err(e);
			}
		}
	}

	private void addUsernamePattern(String p, String d) {
		UsernamePattern pt = new UsernamePattern(p, d);
		usernamePatterns.put(pt);
		try {
			saveUsernamePatterns();
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}

	public void clearParameters() throws ProtocolException {
		List<ProtocolException> e = new ArrayList<ProtocolException>();
		try {
			persistence.clearConnectionContext();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		try {
			persistence.clearUsernamePatterns();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		try {
			persistence.clearFriends();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		if (e.size() > 0) {
			String msg = "";
			for (ProtocolException e1 : e) {
				msg += e1.getMessage() + " | ";
			}
			throw new ProtocolException(msg);
		}
	}

	private void friendOffline(String username) {
		synchronized (persistentFriends) {
			persistentFriends.get(username).setCurrentlyOnline(false);
			nbFriendsOnline--;
			application.notifyFriendsOnlineChanged();
			application.notifyFriendDisconnected(persistentFriends.get(username));
		}
	}

	private void friendOnline(String username) {
		synchronized (persistentFriends) {
			persistentFriends.get(username).setCurrentlyOnline(true);
			nbFriendsOnline++;
			application.notifyFriendsOnlineChanged();
			application.notifyFriendConnected(persistentFriends.get(username));
		}
	}

	public List<SpringAccount> getActiveFriendsSince(long nbMinutes) {
		long aFewTimesAgo = System.currentTimeMillis() - nbMinutes * 60 * 1000;
		List<SpringAccount> l = new ArrayList<SpringAccount>();
		for (SpringAccount act : persistentFriends) {
			if (act.isCurrentlyOnline() || (act.getLastTimeSeen() > aFewTimesAgo)) {
				l.add(act);
			}
		}
		return l;
	}

	public SpringAccountList getFriends() {
		return persistentFriends;
	}

	public List<SpringAccount> getFriendsOnline() {
		List<SpringAccount> l = new ArrayList<SpringAccount>();
		for (SpringAccount act : persistentFriends) {
			if (act.isCurrentlyOnline()) {
				l.add(act);
			}
		}
		return l;
	}

	public int getNbFriendsOnline() {
		return nbFriendsOnline;
	}

	public UsernamePatternList getUsernamePatterns() {
		return usernamePatterns;
	}

	public void loadConnectionContext() throws ProtocolException {
		remote.setContext(persistence.loadConnectionContext());

	}

	public void loadFriends() throws ProtocolException {
		persistentFriends = persistence.loadFriends();
	}

	public void loadParameters() throws ProtocolException {
		List<ProtocolException> e = new ArrayList<ProtocolException>();
		try {
			loadFriends();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		try {
			loadConnectionContext();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		try {
			loadUsernamePatterns();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		if (e.size() > 0) {
			String msg = "";
			for (ProtocolException e1 : e) {
				msg += e1.getMessage() + " | ";
			}
			throw new ProtocolException(msg);
		}
	}

	public void loadUsernamePatterns() throws ProtocolException {
		usernamePatterns = persistence.loadUsernamePatterns();
	}

	public void notifyConnected() {
		application.notifyConnected();
	}

	public void notifyDisconnected() {
		super.notifyDisconnected();

		application.notifyDisconnected();

		for (SpringAccount act : persistentFriends) {
			if (act.isCurrentlyOnline()) {
				act.setCurrentlyOnline(false);
			}
		}
		nbFriendsOnline = 0;
		application.notifyFriendsOnlineChanged();

		try {
			saveFriends();
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}

	public void pcAccepted(String username) {
		super.pcAccepted(username);
		application.notifyLogin();
	}

	public void pcAddUser(String username, String country, String cpu, String accountId) {
		username = username.trim().toUpperCase();
		if (!username.equalsIgnoreCase(remote.getContext().getLogin())) {
			synchronized (connectedPlayers) {
				connectedPlayers.put(new SpringAccount(username));
				if (!persistentFriends.contains(username)) {
					for (UsernamePattern p : usernamePatterns) {
						if (p.matches(username)) {
							addFriendToMonitor(username);
							break;
						}
					}
				} else {
					friendOnline(username);
				}
			}
		}
	}

	public void pcAgreement(String agreement) {
		// Ignore
	}

	public void pcAgreementEnd() {
		// Ignore
	}

	public void pcBattleOpened(String battleId, String type, String natType, String founder, String ip, String port, String maxPlayers, String passworded, String rank, String mapHash, String mapName, String title, String modName) {
		// Ignore
	}

	public void pcBroadcast(String msg) {
		hardware.log(msg);
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

	public void pcRegistrationAccepted() {
		// Ignore
	}

	public void pcRegistrationDenied(String reason) {
		// Ignore
	}

	public void pcRemoveUser(String username) {
		username = username.trim().toUpperCase();
		synchronized (connectedPlayers) {
			connectedPlayers.remove(username);
			if (persistentFriends.contains(username)) {
				friendOffline(username);
			}
		}
	}

	public void pcServerMessage(String msg) {
		hardware.log(msg);
	}

	public void pcServerMessageBox(String msg) {
		hardware.log(msg);
	}

	public void pcUpdateBattleInfo(String battleId, String spectatorCount, String locked, String mapHash, String mapName) {
		// Ignore
	}

	public void removeFriend(String username) {
		username = username.trim().toUpperCase();
		if (connectedPlayers.contains(username)) {
			friendOffline(username);
		}
		persistentFriends.remove(username);

		removeUsernamePatternFromMonitoring(username);

		try {
			saveFriends();
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}

	public void removeUsernamePatternFromMonitoring(String p) {
		usernamePatterns.remove(p);

		try {
			saveUsernamePatterns();
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}

	public void saveConnectionContext() throws ProtocolException {
		saveConnectionContext(remote.getContext());
	}

	public void saveConnectionContext(ConnectionContext c) throws ProtocolException {
		persistence.saveConnectionContext(c);
	}

	public void saveFriends() throws ProtocolException {
		persistence.saveFriends(persistentFriends);
	}

	public void saveParameters() throws ProtocolException {
		List<ProtocolException> e = new ArrayList<ProtocolException>();
		try {
			saveFriends();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		try {
			saveConnectionContext();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		try {
			saveUsernamePatterns();
		} catch (ProtocolException e1) {
			e.add(e1);
		}
		if (e.size() > 0) {
			String msg = "";
			for (ProtocolException e1 : e) {
				msg += e1.getMessage() + " | ";
			}
			throw new ProtocolException(msg);
		}
	}

	public void saveUsernamePatterns() throws ProtocolException {
		persistence.saveUsernamePatterns(usernamePatterns);
	}

	public void setUsernamePatterns(UsernamePatternList usernamePatterns) {
		this.usernamePatterns = usernamePatterns;
	}

}
