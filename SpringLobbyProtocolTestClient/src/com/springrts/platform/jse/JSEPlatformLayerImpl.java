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

import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.LobbyCommandListener;
import com.springrts.protocol.ProtocolException;
import com.springrts.protocol.tools.CommandParser;
import com.springrts.protocol.tools.PasswordEncoder;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class JSEPlatformLayerImpl implements PlatformLayer {
	private static final DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS", Locale.FRANCE);
	private boolean doDebug;
	private CommandParser parser;

	public JSEPlatformLayerImpl() {
		super();
		setDebug(false);
		parser = new CommandParser(this);
	}

	public void log(String msg) {
		System.out.println(df.format(new Date()) + " : " + msg);
	}

	public void dbg(String msg) {
		if (doDebug) {
			log(" [DBG] " + msg);
		}
	}

	public void err(String msg) {
		System.err.println(df.format(new Date()) + " : " + msg);
	}

	public void err(Throwable e) {
		if(doDebug) {
			e.printStackTrace();
		}
		err(e.getClass().getName() + " : " + e.getMessage());
	}
	
	public void dbg(Throwable e) {
		if(doDebug) {
			e.printStackTrace();
			dbg(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	public void setDebug(boolean dbg) {
		doDebug = dbg;
	}

	public String encodePassword(String plainPassword, String preferredEncoding) throws ProtocolException {
		try {
			PasswordEncoder enc = new PasswordEncoder();
			return enc.encodePassword(plainPassword, preferredEncoding);
		} catch (NoSuchAlgorithmException e) {
			throw new ProtocolException(e);
		} catch (DigestException e) {
			throw new ProtocolException(e);
		}
	}

	public void parse(String command, LobbyCommandListener client) throws ProtocolException {
		parser.parse(command, client);
	}

	public int split(String line, String[] commands) {
		String[] r = line.split(" ", commands.length);

		if (r != null) {
			for (int i = 0; i < r.length; i++) {
				commands[i] = r[i];
			}
			return r.length;
		} else {
			return 0;
		}
	}

	@Override
	public void threadCreationSpecificStuff() {
	}

}
