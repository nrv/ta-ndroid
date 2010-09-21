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

package com.springrts.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.springrts.client.MonitoringApplication;
import com.springrts.client.MonitoringClient;
import com.springrts.data.SpringAccount;
import com.springrts.platform.jse.JSENetworkLayerImpl;
import com.springrts.platform.jse.JSEPersistenceLayerImpl;
import com.springrts.platform.jse.JSEPlatformLayerImpl;
import com.springrts.protocol.ConnectionContext;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class SwingTestUI extends JFrame implements MonitoringApplication, ActionListener {
	private static final long serialVersionUID = -3975429257823026504L;

	private JButton btLogin;
	private JButton btLogout;
	private JTextArea taLogWindow;

	private String login;
	private String password;
	private String server;
	private String port;
	
	private DateFormat df;

	private MonitoringClient client;

	public SwingTestUI(String[] args) {
		super("Spring Lobby Test Client");

		login = args[0];
		password = args[1];
		server = args[2];
		port = args[3];
		
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		taLogWindow = new JTextArea();
		mainPanel.add(taLogWindow);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setOpaque(false);
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));

		btLogin = new JButton("Login");
		btLogin.addActionListener(this);
		buttonsPanel.add(btLogin);

		btLogout = new JButton("Logout");
		btLogout.addActionListener(this);
		buttonsPanel.add(btLogout);

		mainPanel.add(buttonsPanel);

		setContentPane(mainPanel);
		setPreferredSize(new java.awt.Dimension(800, 600));

		pack();
		setVisible(true);
		
		df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
	}

	private void logToUI(String msg) {
		taLogWindow.append(df.format(new Date()) + " - " + msg + "\n");
	}

	@Override
	public void notifyFriendsOnlineChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyConnected() {
		logToUI("You are now connected");
	}

	@Override
	public void notifyLogin() {
		logToUI("Login accepted");
	}

	@Override
	public void notifyAccessDenied(String why) {
		logToUI("Access denied : " + why);
	}

	@Override
	public void notifyLoginEnd() {
	}

	@Override
	public void notifyDisconnected() {
		logToUI("You are now disconnected");
	}

	@Override
	public void notifyFriendDisconnected(SpringAccount act) {
		logToUI(act.getUsername() + " has disconnected");
	}

	@Override
	public void notifyFriendConnected(SpringAccount act) {
		logToUI(act.getUsername() + " is connected");
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.err.println("Please launch with the following parameters :");
			System.err.println("SwingTestUI [login] [password] [server] [port]");
			return;
		}

		new SwingTestUI(args);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton b = (JButton) e.getSource();

		if (b == null) {
			return;
		}

		if (b == btLogin) {
			performLogin();
		}

		if (b == btLogout) {
			performLogout();
		}
	}

	private void performLogout() {
		client.disconnect();
	}

	private void performLogin() {
		client = new MonitoringClient(this);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JSEPlatformLayerImpl hdw = new JSEPlatformLayerImpl();
		hdw.setDebug(true);

		JSENetworkLayerImpl nwk = new JSENetworkLayerImpl(hdw);
		client.setHardware(hdw);
		client.setRemote(nwk);

		JSEPersistenceLayerImpl pers = new JSEPersistenceLayerImpl(hdw);
		client.setPersistence(pers);

		client.setStartPinger(true);

		try {
			ConnectionContext context = ConnectionContext.defaultContext();
			context.setServerIP(server);
			context.setServerPort(port);
			context.setLogin(login);
			context.setEncodedPassword(hdw.encodePassword(password, context.getCharset()));
			pers.saveConnectionContext(context);

			client.addClan("FLM");
			pers.saveUsernamePatterns(client.getUsernamePatterns());

			try {
				client.loadParameters();
			} catch (Exception e1) {
				System.err.println("SwingTestUI catched an exception : " + e1.getMessage());
			}

			client.connect();

		} catch (Exception e) {
			System.err.println("SwingTestUI catched an exception : " + e.getMessage());
			e.printStackTrace();
		}
	}

}
