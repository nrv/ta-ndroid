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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import com.springrts.data.SpringAccountList;
import com.springrts.platform.CommonPersistenceLayer;
import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class JSEPersistenceLayerImpl extends CommonPersistenceLayer {
	private final static String FILE_FRIENDS = "friends.txt";
	private final static String FILE_CONTEXT = "context.txt";
	private final static String FILE_PATTERNS = "patterns.txt";

	private void save(String s, String f) throws IOException {
		FileWriter fw = new FileWriter(new File(f));
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(s);
		bw.close();
	}

	private String load(String f) throws IOException {
		FileReader fr = new FileReader(new File(f));
		BufferedReader br = new BufferedReader(fr);
		String s = br.readLine();
		br.close();
		return s;
	}

	public JSEPersistenceLayerImpl(PlatformLayer hardware) {
		super(hardware);
	}

	@Override
	public SpringAccountList loadFriends() throws ProtocolException {
		try {
			return friendListFromString(load(FILE_FRIENDS));
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public void saveFriends(SpringAccountList lst) throws ProtocolException {
		try {
			save(friendListAsString(lst), FILE_FRIENDS);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public ConnectionContext loadConnectionContext() throws ProtocolException {
		try {
			return contextFromString(load(FILE_CONTEXT));
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public void saveConnectionContext(ConnectionContext ctx) throws ProtocolException {
		try {
			save(contextAsString(ctx), FILE_CONTEXT);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public Map<String, Pattern> loadUsernamePatterns() throws ProtocolException {
		try {
			return usernamePatternsFromString(load(FILE_PATTERNS));
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public void saveUsernamePatterns(Map<String, Pattern> p) throws ProtocolException {
		try {
			save(usernamePatternsAsString(p), FILE_PATTERNS);
		} catch (IOException e) {
			throw new ProtocolException(e);
		}
	}

}
