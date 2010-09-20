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
import com.springrts.platform.PersistenceLayer;
import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.LobbyCommandListener;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public abstract class PingClient implements LobbyCommandListener {
	private class Pinger extends Thread {
		public synchronized void run() {
			int pingInterval = getContext().getAvoidTimeoutPingInterval();
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
		}

		public synchronized void stopPinger() {
			notify();
		}
	}

	protected NetworkLayer remote;
	protected PlatformLayer hardware;
	protected PersistenceLayer persistence;
//	protected ConnectionContext context;
	protected boolean tryingToConnect;
	private boolean connected;
	private boolean loginFinished;
	private boolean startPinger;
	private Pinger pinger;
//	private boolean redirect;

	public PingClient() {
		super();
		tryingToConnect = false;
		connected = false;
		startPinger = true;
		loginFinished = false;
//		redirect = false;
	}

	public boolean isConnectedAndRunning() {
		return connected && remote.isRunning();
	}

	public void notifyDisconnected() {
		if (pinger != null) {
			pinger.stopPinger();
			pinger = null;
		}
	}

	public void connect() throws ProtocolException {
		if (!connected) {
			connected = false;
			tryingToConnect = true;
			loginFinished = false;

			remote.setCommandListener(this);
			remote.connectNetwork();

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
	}

	public void disconnect() {
		connected = false;

		if (pinger != null) {
			pinger.stopPinger();
			try {
				pinger.join();
			} catch (InterruptedException e) {
			}
		}

		if (remote != null) {
			remote.disconnectNetwork();
		}

//		if (redirect) {
//			redirect = false;
//
//			try {
//				connect(context);
//			} catch (ProtocolException e) {
//				disconnect();
//			}
//		}

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

	public void pcRedirect(String ip) {
		hardware.log("Redirecting to " + ip);
//		getContext().setServerIP(ip);
		tryingToConnect = false;
//		redirect = true;
	}

	public void setPersistence(PersistenceLayer persistence) {
		this.persistence = persistence;
	}

	public ConnectionContext getContext() {
		return remote.getContext();
	}
}
