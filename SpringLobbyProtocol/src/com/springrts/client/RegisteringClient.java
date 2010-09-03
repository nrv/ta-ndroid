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

import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public abstract class RegisteringClient extends PingClient {
	private boolean registerIfFirstLoginFailed;
	private int nbLoginFailed;
	

	public RegisteringClient() {
		super();
		this.registerIfFirstLoginFailed = false;
		this.nbLoginFailed = 0;
	}

	public void pcRegistrationAccepted() {
		hardware.log("Registration accepted");
		try {
			remote.login();
		} catch (ProtocolException e) {
			hardware.err(e);
			disconnect();
		}
		
	}

	public void pcDenied(String reason) {
		hardware.log("Access denied (" + reason + ")");
		nbLoginFailed++;
		
		if (registerIfFirstLoginFailed && (nbLoginFailed == 1)) {
			try {
				remote.register();
			} catch (ProtocolException e) {
				hardware.err(e);
				disconnect();
			}
		} else {
			tryingToConnect = false;
		}
	}
	
	public void pcRegistrationDenied(String reason) {
		hardware.log("Registration denied (" + reason + ")");
		tryingToConnect = false;
	}

	public void pcAgreementEnd() {
		hardware.dbg("pcAgreementEnd");
		try {
			remote.confirmAgreement();
			try {
				// 5 sec between 2 connection attempts
				Thread.sleep(7000);
			} catch (InterruptedException e) {
			}
			remote.login();
		} catch (ProtocolException e) {
			hardware.err(e);
			disconnect();
		}
	}

	public boolean isRegisterIfFirstLoginFailed() {
		return registerIfFirstLoginFailed;
	}

	public void setRegisterIfFirstLoginFailed(boolean registerIfFirstLoginFailed) {
		this.registerIfFirstLoginFailed = registerIfFirstLoginFailed;
	}

}
