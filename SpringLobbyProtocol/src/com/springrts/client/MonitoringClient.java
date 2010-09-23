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

	public void addClanToMonitor(String clan) {
		clan = clan.trim().toUpperCase();
		addUsernamePattern(".*[" + clan + "].*", "*[" + clan + "]*");
	}

	public void addFriendToMonitor(String username) {
		username = username.trim().toUpperCase();
		addUsernamePattern("^" + username + "$", username);
	}

	private void addPersistentFriend(String username) {
		SpringAccount friend = new SpringAccount(username);
		persistentFriends.put(friend);
		if (connectedPlayers.contains(username)) {
			friend.setCurrentlyOnline(true);
		}
	}

	private void checkConnectedPlayersAgainstUsernamePatterns() {
		boolean atLeastOneModification = false;

		// Add new friends
		for (SpringAccount a : connectedPlayers) {
			String username = a.getUsername();
			if (!persistentFriends.contains(username) && !username.equalsIgnoreCase(remote.getContext().getLogin())) {
				List<String> toAdd = new ArrayList<String>();
				for (UsernamePattern p : usernamePatterns) {
					if (p.matches(username)) {
						addPersistentFriend(username);
						toAdd.add(username);
						atLeastOneModification = true;
						break;
					}
				}
				for (String u : toAdd) {
					addFriendToMonitor(u);
				}
			}
		}

		// Remove players without any corresponding pattern
		List<String> toRemove = new ArrayList<String>();
		for (SpringAccount a : persistentFriends) {
			String username = a.getUsername();
			boolean found = false;
			for (UsernamePattern p : usernamePatterns) {
				if (p.matches(username)) {
					found = true;
					break;
				}
			}
			if (!found) {
				toRemove.add(username);
				atLeastOneModification = true;
			}
		}
		for (String username : toRemove) {
			persistentFriends.remove(username);
		}

		try {
			if (atLeastOneModification) {
				saveFriends();
				nbFriendsOnline = 0;
				for (SpringAccount a : persistentFriends) {
					if (a.isCurrentlyOnline()) {
						nbFriendsOnline++;
					}
				}
				application.notifyFriendsOnlineChanged();
			}
		} catch (ProtocolException e) {
			hardware.err(e);
		}
	}

	private void addUsernamePattern(String p, String d) {
		p = p.replace("[", "\\[");
		p = p.replace("]", "\\]");
		UsernamePattern pt = new UsernamePattern(p, d);
		if (!usernamePatterns.contains(pt.getDisplay())) {
			usernamePatterns.put(pt);

			try {
				saveUsernamePatterns();
			} catch (ProtocolException e) {
				hardware.err(e);
			}

			checkConnectedPlayersAgainstUsernamePatterns();
		}
	}

	public void removeUsernamePatternFromMonitoring(String p) {
		usernamePatterns.remove(p);

		try {
			saveUsernamePatterns();
		} catch (ProtocolException e) {
			hardware.err(e);
		}

		checkConnectedPlayersAgainstUsernamePatterns();
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

	public void clearFriends() throws ProtocolException {
		usernamePatterns.clear();
		persistentFriends.clear();

		try {
			saveUsernamePatterns();
		} catch (ProtocolException e) {
			hardware.err(e);
		}

		try {
			saveFriends();
		} catch (ProtocolException e) {
			hardware.err(e);
		}

		nbFriendsOnline = 0;
		application.notifyFriendsOnlineChanged();
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
		if (username.equalsIgnoreCase("[PiRO]Carpenter")) {
			System.out.println("--------------------------");
		}
		if (!username.equalsIgnoreCase(remote.getContext().getLogin())) {
			synchronized (connectedPlayers) {
				connectedPlayers.put(new SpringAccount(username));
				if (!persistentFriends.contains(username)) {
					for (UsernamePattern p : usernamePatterns) {
						if (p.matches(username)) {
							hardware.dbg("pcAddUser(" + username + ")");
							addPersistentFriend(username);
							addFriendToMonitor(username);
							break;
						}
					}
				}

				if (persistentFriends.contains(username)) {
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

	public void pcJoin(String chan) {
		// Ignore
	}

	public void pcJoinFailed(String chanAndReason) {
		// Ignore
	}

}
