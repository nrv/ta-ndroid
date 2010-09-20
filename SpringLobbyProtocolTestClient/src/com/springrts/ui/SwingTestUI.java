package com.springrts.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class SwingTestUI extends JFrame implements MonitoringApplication, ActionListener {
	private static final long serialVersionUID = -3975429257823026504L;

	private JButton btLogin;
	private JButton btLogout;
	private JTextArea taLogWindow;

	private String login;
	private String password;
	private String server;
	private String port;

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
	}

	private void logToUI(String msg) {
	}

	@Override
	public void notifyFriendsOnlineChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyLogin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyAccessDenied(String why) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyLoginEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyFriendDisconnected(SpringAccount act) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyFriendConnected(SpringAccount act) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		SwingTestUI test = new SwingTestUI(args);
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
