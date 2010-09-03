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
public class ConnectionContext {
	public final static String LOBBYNAME = "NRV-SpringLobbyProtocol";
	public final static String LOBBYVERSION = "1.0.0";
	public final static String LOCAL_SERVER_ADDRESS = "localhost";
	public final static String OFFICIAL_SERVER_ADDRESS = "taspringmaster.clan-sy.com";
	public final static String TEST_SERVER_ADDRESS = "springlobby.info";
	public final static String DEFAULT_SERVER_ADDRESS = LOCAL_SERVER_ADDRESS;
	public final static int DEFAULT_SERVER_PORT = 8200;
	public final static int DEFAULT_PING_INTERVAL = 15000;
	public final static String DEFAULT_CHARSET = "ISO-8859-1";
	
	private String serverIP;
	private int serverPort;
	private int avoidTimeoutPingInterval;
	private String login;
	private String password;
	private String encodedPassword;
	private String charset;
	private String lobbyNameAndVersion;

	public static ConnectionContext defaultContext() {
		ConnectionContext dc = new ConnectionContext();
		dc.setServerIP(DEFAULT_SERVER_ADDRESS);
		dc.setServerPort(DEFAULT_SERVER_PORT);
		dc.setAvoidTimeoutPingInterval(DEFAULT_PING_INTERVAL);
		dc.setCharset(DEFAULT_CHARSET);
		dc.setLobbyNameAndVersion(LOBBYNAME + " " + LOBBYVERSION);
		return dc;
	}

	public ConnectionContext() {
		super();
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public void setServerPort(String serverPort) throws ProtocolException {
		try {
			this.serverPort = Integer.parseInt(serverPort);
		} catch (NumberFormatException e) {
			throw new ProtocolException(e);
		}
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getAvoidTimeoutPingInterval() {
		return avoidTimeoutPingInterval;
	}

	public void setAvoidTimeoutPingInterval(int avoidTimeoutPingInterval) {
		this.avoidTimeoutPingInterval = avoidTimeoutPingInterval;
	}

	public void setAvoidTimeoutPingInterval(String avoidTimeoutPingInterval) throws ProtocolException {
		try {
			this.avoidTimeoutPingInterval = Integer.parseInt(avoidTimeoutPingInterval);
		} catch (NumberFormatException e) {
			throw new ProtocolException(e);
		}
	}

	public String getLobbyNameAndVersion() {
		return lobbyNameAndVersion;
	}

	public void setLobbyNameAndVersion(String lobbyNameAndVersion) {
		this.lobbyNameAndVersion = lobbyNameAndVersion;
	}
}
