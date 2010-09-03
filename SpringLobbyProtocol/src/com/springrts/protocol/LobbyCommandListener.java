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

package com.springrts.protocol;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public interface LobbyCommandListener {
	void notifyConnected();
	void notifyDisconnected();
	void pcTasServer(String serverVersion, String springVersion, String udpPort, String serverMode);
	void pcPong();
	void pcDenied(String reason);
	void pcAccepted(String username);
	void pcMotd(String message);
	void pcAddUser(String username, String country, String cpu, String accountId);
	void pcRemoveUser(String username);
	void pcLoginInfoEnd();
	void pcRegistrationAccepted();
	void pcRegistrationDenied(String reason);
	void pcClientStatus(String username, String status);
	void pcAgreement(String agreement);
	void pcAgreementEnd();
	void pcNotImplemented(String command);
	void pcBattleOpened(String battleId, String type, String natType, String founder, String ip, String port, String maxPlayers, String passworded, String rank, String mapHash, String mapName, String title, String modName);
	void pcUpdateBattleInfo(String battleId, String spectatorCount, String locked, String mapHash, String mapName);
	void pcJoinedBattle(String battleId, String username, String scriptPassword);
}
