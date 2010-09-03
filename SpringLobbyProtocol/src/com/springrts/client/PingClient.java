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

import com.springrts.platform.NetworkLayer;
import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.LobbyCommandListener;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public abstract class PingClient implements LobbyCommandListener {
	private class Pinger extends Thread {
		public synchronized void run() {
			int pingInterval = remote.getContext().getAvoidTimeoutPingInterval();
			hardware.dbg("Starting pinger (" + (pingInterval / 1000) + "s)");
			while ((remote != null) && connected && remote.isRunning()) {
				try {
					wait(pingInterval);
				} catch (InterruptedException e) {
				}
				try {
					if ((remote != null) && connected && remote.isRunning()) {
						remote.ping();
					}
				} catch (ProtocolException e) {
					hardware.err(e);
					disconnect();
				}
			}
			hardware.dbg("Stoping pinger");
		}

		public synchronized void stopPinger() {
			notify();
		}
	}

	protected NetworkLayer remote;
	protected PlatformLayer hardware;
	protected boolean tryingToConnect;
	private boolean connected;
	private boolean loginFinished;
	private boolean startPinger;
	private Pinger pinger;

	public PingClient() {
		super();
		tryingToConnect = false;
		connected = false;
		startPinger = true;
		loginFinished = false;
	}

	public boolean isConnectedAndRunning() {
		return connected && remote.isRunning();
	}

	public synchronized void connect(ConnectionContext context) throws ProtocolException {
		connected = false;
		tryingToConnect = true;
		loginFinished = false;

		remote.setCommandListener(this);
		remote.setContext(context);
		remote.connect();

		while (tryingToConnect) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

		if (startPinger && isConnectedAndRunning()) {
			pinger = new Pinger();
			pinger.start();
		}
	}

	public synchronized void disconnect() {
		connected = false;

		if (pinger != null) {
			pinger.stopPinger();
		}

		if (remote != null) {
			remote.disconnect();
		}
	}

	public boolean isStartPinger() {
		return startPinger;
	}

	public void pcAccepted(String username) {
		hardware.log("Login accepted");
		connected = true;
		tryingToConnect = false;
	}

	public void pcDenied(String reason) {
		hardware.log("Access denied (" + reason + ")");
		connected = false;
		tryingToConnect = false;
	}

	public void pcPong() {
		hardware.dbg("Pong");
	}

	public void pcLoginInfoEnd() {
		loginFinished = true;
	}

	public void pcTasServer(String serverVersion, String springVersion, String udpPort, String serverMode) {
		hardware.log("Connected to a " + serverVersion + " instance accepting Spring " + springVersion);
		try {
			remote.login();
		} catch (ProtocolException e) {
			hardware.err(e);
			disconnect();
		}
	}

	public void setHardware(PlatformLayer hardware) {
		this.hardware = hardware;
	}

	public void setRemote(NetworkLayer remote) {
		this.remote = remote;
	}

	public void setStartPinger(boolean startPinger) {
		this.startPinger = startPinger;
	}

	public boolean isLoginFinished() {
		return loginFinished;
	}
}
