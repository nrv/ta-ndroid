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

package com.springrts.protocol.tools;

import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.LobbyCommandListener;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class CommandParser {
	public final static char SPACE = ' ';
	public final static int MAX_PARAM = 15;
	private PlatformLayer hdw;
	
	public CommandParser(PlatformLayer hdw) {
		super();
		this.hdw = hdw;
	}

	public static int defaultSplit(String command, String[] commands) {
		command.trim();
		
		if (command.length() == 0) {
			return 0;
		}
		
		char[] c = command.toCharArray();
		int b = 0;
		int e = 0;
		int s = c.length;
		int n = 0;
		
		while ((e < s) && (n < MAX_PARAM)) {
			if (c[e] == SPACE) {
				if (b < e) {
					commands[n] = new String(c, b, e - b);
					n++;
				}
				while((e < s) && (c[e] == SPACE)) {
					e++;
				}
				b = e;
			} else {
				e++;
			}
		}
		if (b < e) {
			commands[n] = new String(c, b, e - b);
			n++;
		}
		
		return n;
	}

	public void parse(String command, LobbyCommandListener client) throws ProtocolException {
		command = command.trim();
		if (command.equals("")) {
			return;
		}

		String[] c = new String[MAX_PARAM]; 
		hdw.split(command, c);

		String c0 = c[0].toUpperCase();
		String cxx = command.substring(c0.length()).trim();

		if (c0.equals("PONG")) {
			client.pcPong();
		} else if (c0.equals("TASSERVER")) {
			client.pcTasServer(c[1], c[2], c[3], c[4]);
		} else if (c0.equals("DENIED")) {
			client.pcDenied(cxx);
		} else if (c0.equals("ACCEPTED")) {
			client.pcAccepted(c[1]);
		} else if (c0.equals("REGISTRATIONACCEPTED")) {
			client.pcRegistrationAccepted();
		} else if (c0.equals("REGISTRATIONDENIED")) {
			client.pcRegistrationDenied(cxx);
		} else if (c0.equals("MOTD")) {
			client.pcMotd(cxx);
		} else if (c0.equals("ADDUSER")) {
			client.pcAddUser(c[1], c[2], c[3], c[4]);
		} else if (c0.equals("REMOVEUSER")) {
			client.pcRemoveUser(c[1]);
		} else if (c0.equals("AGREEMENT")) {
			client.pcAgreement(cxx);
		} else if (c0.equals("AGREEMENTEND")) {
			client.pcAgreementEnd();
		} else if (c0.equals("LOGININFOEND")) {
			client.pcLoginInfoEnd();
		} else if (c0.equals("CLIENTSTATUS")) {
			client.pcClientStatus(c[1], c[2]);
		} else if (c0.equals("BATTLEOPENED")) {
			client.pcBattleOpened(c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8], c[9], c[10], c[11], c[12], c[13]);
		} else if (c0.equals("UPDATEBATTLEINFO")) {
			client.pcUpdateBattleInfo(c[1], c[2], c[3], c[4], c[5]);
		} else if (c0.equals("JOINEDBATTLE")) {
			client.pcJoinedBattle(c[1], c[2], c[3]);
		} else if (c0.equals("BROADCAST")) {
			client.pcBroadcast(cxx);
		} else if (c0.equals("SERVERMSGBOX")) {
			client.pcServerMessageBox(cxx);
		} else if (c0.equals("SERVERMSG")) {
			client.pcServerMessage(cxx);
		} else if (c0.equals("REDIRECT")) {
			client.pcRedirect(c[1]);
		} else if (c0.equals("JOIN")) {
			client.pcJoin(c[1]);
		} else if (c0.equals("JOINFAILED")) {
			client.pcJoinFailed(cxx);
		} else {
			client.pcNotImplemented(command);
		}
	}
}
