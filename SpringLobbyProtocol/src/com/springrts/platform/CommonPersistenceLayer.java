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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.springrts.data.SpringAccount;
import com.springrts.data.SpringAccountList;
import com.springrts.protocol.ConnectionContext;

/**
 * Ugly implementation, will be improved later
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
// TODO Better stuff
public abstract class CommonPersistenceLayer implements PersistenceLayer {
	public CommonPersistenceLayer(PlatformLayer hardware) {
		super();
		this.hardware = hardware;
	}

	private final static String SEP1 = " ; ";
	private final static String SEP2 = " : ";

	protected PlatformLayer hardware;

	public String friendListAsString(SpringAccountList lst) {
		String s = "";

		for (SpringAccount a : lst) {
			if (s.length() > 0) {
				s += SEP1;
			}
			s += friendAsString(a);
		}

		return s;
	}

	public String friendAsString(SpringAccount f) {
		String s = "";
		s += f.getInternalId();
		s += SEP2 + f.getUsername();
		s += SEP2 + f.getLastTimeSeen();
		return s;
	}

	public SpringAccountList friendListFromString(String s) throws NumberFormatException {
		SpringAccountList lst = new SpringAccountList();
		if (s.length() > 0) {
			String[] d = s.split(SEP1);
			for (String a : d) {
				lst.put(friendFromString(a));
			}
		}
		return lst;
	}

	public SpringAccount friendFromString(String s) throws NumberFormatException {
		String[] d = s.split(SEP2);
		SpringAccount a = new SpringAccount();
		a.setInternalId(Integer.parseInt(d[0]));
		a.setUsername(d[1]);
		a.setLastTimeSeen(Long.parseLong(d[2]));
		return a;
	}

	public String contextAsString(ConnectionContext c) {
		String s = "";
		s += c.getServerIP();
		s += SEP2 + c.getServerPort();
		s += SEP2 + c.getLogin();
		s += SEP2 + c.getEncodedPassword();
		s += SEP2 + c.getAvoidTimeoutPingInterval();
		return s;
	}

	public ConnectionContext contextFromString(String s) throws NumberFormatException {
		String[] d = s.split(SEP2);
		ConnectionContext c = new ConnectionContext();
		if (d.length > 0) {
			c.setServerIP(d[0]);
			if (d.length > 1) {
				c.setServerPort(Integer.parseInt(d[1]));
				if (d.length > 2) {
					c.setLogin(d[2]);
					if (d.length > 3) {
						c.setEncodedPassword(d[3]);
						if (d.length > 4) {
							c.setAvoidTimeoutPingInterval(Integer.parseInt(d[4]));
						}
					}
				}
			}
		}
		return c;
	}

	public String usernamePatternsAsString(Map<String, Pattern> p) {
		String s = "";

		for (String pt : p.keySet()) {
			if (s.length() > 0) {
				s += SEP1;
			}
			s += pt;
		}

		return s;
	}

	public Map<String, Pattern> usernamePatternsFromString(String s) {
		String[] d = s.split(SEP2);
		HashMap<String, Pattern> p = new HashMap<String, Pattern>();
		for (String a : d) {
			p.put(a, Pattern.compile(a));
		}
		return p;
	}
}
