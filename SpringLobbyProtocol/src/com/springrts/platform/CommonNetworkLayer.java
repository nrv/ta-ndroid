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

package com.springrts.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.LobbyCommandListener;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public abstract class CommonNetworkLayer implements NetworkLayer {
	private final static char NL = '\n';
	private final static char CR = '\r';
	private final static byte[] EOL = "\r\n".getBytes();

	protected PlatformLayer hardware;
	protected ConnectionContext context;
	protected LobbyCommandListener client;
	protected boolean running;
	protected InputStream is;
	protected OutputStream os;

	protected Sender sender;
	protected Receiver receiver;

	public CommonNetworkLayer(PlatformLayer hardware) {
		super();
		this.hardware = hardware;
		this.sender = null;
		this.receiver = null;
	}

	private class Sender extends Thread {
		private String message;

		public Sender() {
			super();
			message = null;
		}

		public synchronized void run() {
			hardware.threadCreationSpecificStuff();
			while (running) {
				if (message == null) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
					if (message != null) {
						try {
							os.write(message.getBytes());
							os.write(EOL);
						} catch (IOException e) {
							hardware.err(e);
							client.disconnect();
						}
					}

					message = null;
				}
			}
		}

		public synchronized void send(String msg) {
			message = msg;
			notify();
		}
	}

	private class Receiver extends Thread {
		public synchronized void run() {
			hardware.threadCreationSpecificStuff();
			while (running) {
				try {
					StringBuffer sb = new StringBuffer();
					int c;

					while (((c = is.read()) != NL) && (c != CR) && (c != -1)) {
						sb.append((char) c);
					}

					if (c == -1) {
						client.disconnect();
					} else {
						String s = sb.toString();
						hardware.dbg("Receiving : " + s);
						hardware.parse(s, client);
					}
				} catch (IOException e) {
					hardware.err(e);
					client.disconnect();
				} catch (ProtocolException e) {
					hardware.err(e);
					client.disconnect();
				} catch (NullPointerException e) {
					hardware.err(e);
					client.disconnect();
				}
			}
		}

	}

	protected void startSenderAndReceiver() {
		running = true;

		sender = new Sender();
		sender.start();
		receiver = new Receiver();
		receiver.start();
	}

	protected void stopSenderAndReceiver() {
		running = false;

		if (sender != null) {
			sender.send(null);
		}

		if (sender != null) {
			try {
				sender.join();
			} catch (InterruptedException e) {
			}
		}
		
		if (receiver != null) {
			try {
				receiver.interrupt();
				receiver.join();
			} catch (InterruptedException e) {
			}
		}
		
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}

		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
			}
		}

		sender = null;
		receiver = null;
		is = null;
		os = null;
	}

	protected void send(String msg) throws ProtocolException {
		hardware.dbg("Sending : " + msg);
		sender.send(msg);
	}

	public void login() throws ProtocolException {
		send("LOGIN " + context.getLogin() + " " + context.getEncodedPassword() + " 0 * " + context.getLobbyNameAndVersion());
	}

	public void ping() throws ProtocolException {
		send("PING");
	}

	public void confirmAgreement() throws ProtocolException {
		send("CONFIRMAGREEMENT");
	}

	public void register() throws ProtocolException {
		send("REGISTER " + context.getLogin() + " " + context.getEncodedPassword());
	}

	public boolean isRunning() {
		return running;
	}

	public ConnectionContext getContext() {
		return context;
	}

	public void setContext(ConnectionContext context) {
		this.context = context;
	}

	public LobbyCommandListener getCommandListener() {
		return client;
	}

	public void setCommandListener(LobbyCommandListener client) {
		this.client = client;
	}
}
