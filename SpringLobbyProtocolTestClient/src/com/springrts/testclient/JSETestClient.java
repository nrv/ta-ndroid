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

package com.springrts.testclient;

import com.springrts.client.MonitoringApplication;
import com.springrts.client.MonitoringClient;
import com.springrts.data.SpringAccount;
import com.springrts.platform.jse.JSENetworkLayerImpl;
import com.springrts.platform.jse.JSEPlatformLayerImpl;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.tools.PasswordEncoder;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class JSETestClient implements MonitoringApplication {
	private MonitoringClient client;

	public JSETestClient() {
		super();
	}

	public void launch(String login, String password) {
		client = new MonitoringClient(this);
		
		client.addFriend("[FLM]Nrv");
		client.addFriend("[FLM]Stef");
		client.addFriend("[FLM]daftalx");
		client.addFriend("[FLM]Marciolino");
		client.addFriend("[FLM]ZZZzzz");
		client.addFriend("[FLM]Faboz");
		client.addFriend("[FLM]Scorplex");
		client.addFriend("[FLM]got_thx");
		client.addFriend("[FLM]NoFX");
		client.addFriend("[FLM]7777");
		client.addFriend("[FLM]Krogoth");
		client.addFriend("[FLM]mOonst4r");

		JSEPlatformLayerImpl hdw = new JSEPlatformLayerImpl();
		hdw.setDebug(false);

		JSENetworkLayerImpl nwk = new JSENetworkLayerImpl(hdw);
		client.setHardware(hdw);
		client.setRemote(nwk);

		client.setRegisterIfFirstLoginFailed(false);
		client.setStartPinger(true);

		try {
			ConnectionContext context = ConnectionContext.defaultContext();
			context.setServerIP(ConnectionContext.OFFICIAL_SERVER_ADDRESS);
			context.setLogin(login);
			context.setPassword(password);
			PasswordEncoder pwdenc = new PasswordEncoder();
			context.setEncodedPassword(pwdenc.encodePassword(password, context.getCharset()));

			client.connect(context);

			while (client.isConnectedAndRunning()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

		} catch (Exception e) {
			System.err.println("JSETestClient catched an exception : " + e.getMessage());
		} finally {
			client.disconnect();
			client = null;
		}
	}

	public void notifyFriendsOnlineChanged() {
		if (client.isLoginFinished()) {
			System.out.println("You have now " + client.getNbFriendsOnline() + " friends online :");
			for (SpringAccount act : client.getActiveFriendsSince(15)) {
				System.out.println("  - " + act.shortDisplay());
			}
		}
	}

	public static void main(String[] args) {
		JSETestClient test = new JSETestClient();
		test.launch(args[0], args[1]);
	}

	@Override
	public void notifyConnected() {
		System.out.println("notifyConnected()");
	}

	@Override
	public void notifyLogin() {
		System.out.println("notifyLogin()");
	}

	@Override
	public void notifyDisconnected() {
		System.out.println("notifyDisconnected()");
	}

	@Override
	public void notifyLoginEnd() {
		System.out.println("notifyLoginEnd()");
	}

	@Override
	public void notifyAccessDenied(String why) {
		System.out.println("notifyAccessDenied(" + why + ")");
	}

	@Override
	public void notifyFriendDisconnected(SpringAccount act) {
		if (client.isLoginFinished()) {
			System.out.println("notifyFriendDisconnected(" + act.getUsername() + ")");
		}
	}

	@Override
	public void notifyFriendConnected(SpringAccount act) {
		if (client.isLoginFinished()) {
			System.out.println("notifyFriendConnected(" + act.getUsername() + ")");
		}
	}

}
