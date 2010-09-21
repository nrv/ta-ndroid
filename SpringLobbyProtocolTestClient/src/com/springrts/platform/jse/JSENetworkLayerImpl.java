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

package com.springrts.platform.jse;

import java.io.IOException;
import java.net.Socket;

import com.springrts.platform.CommonNetworkLayer;
import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class JSENetworkLayerImpl extends CommonNetworkLayer {
	private Socket socket;

	public JSENetworkLayerImpl(PlatformLayer hardware) {
		super(hardware);
		socket = null;
	}

	public void connectNetwork() throws ProtocolException {
		try {
			socket = new Socket(context.getServerIP(), context.getServerPort());
			is = socket.getInputStream();
			os = socket.getOutputStream();

			client.notifyConnected();

			startSenderAndReceiver();
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	public void disconnectNetwork() {
		stopSenderAndReceiver();

		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
			socket = null;
			is = null;
			os = null;
			client.notifyDisconnected();
		}
	}
}
