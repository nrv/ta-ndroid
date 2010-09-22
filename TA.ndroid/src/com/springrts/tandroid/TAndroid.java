/*
 * Copyright (C) 2010 NRV - nherve75@gmail.com
 * 
 * This file is part of TA.ndroid.
 * 
 * TA.ndroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 * 
 * TA.ndroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with TA.ndroid. If not, see http://www.gnu.org/licenses/
 * 
 */
package com.springrts.tandroid;

import java.security.DigestException;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import com.springrts.client.MonitoringApplication;
import com.springrts.data.SpringAccount;
import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.LobbyCommandListener;
import com.springrts.protocol.ProtocolException;
import com.springrts.protocol.tools.CommandParser;
import com.springrts.protocol.tools.PasswordEncoder;
import com.springrts.tandroid.ui.Main;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class TAndroid extends Application implements MonitoringApplication, PlatformLayer {
	private static final String LOG_TAG = TAndroid.class.getSimpleName();

	public static final int HANDLER_REFRESH_UI = 1;
	public static final int HANDLER_NOTIFY_LOGIN = 2;
	public static final int HANDLER_NOTIFY_OFFLINE = 3;
	public static final int HANDLER_NOTIFY_ONLINE = 4;
	public static final int HANDLER_NOTIFY_CONNECTED = 5;
	public static final int HANDLER_NOTIFY_DENIED = 6;
	public static final int HANDLER_NOTIFY_FRIEND_CONNECTED = 7;
	public static final int HANDLER_NOTIFY_FRIEND_DISCONNECTED = 8;
	
	private static final boolean DEBUG_LEVEL_ENABLED = true;
	private static final boolean LOG_LEVEL_ENABLED = true;

	private Main friendsListDisplayed = null;
	private CommandParser parser;

	public static final String PREFS = "com.springrts.tandroid.TA.ndroid";

	@Override
	public void dbg(String msg) {
		if (DEBUG_LEVEL_ENABLED) {
			Log.d(LOG_TAG, msg);
		}
	}
	
	@Override
	public void dbg(Throwable e) {
		if(DEBUG_LEVEL_ENABLED) {
			e.printStackTrace();
			dbg(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	@Override
	public String encodePassword(String plainPassword, String preferredEncoding) throws ProtocolException {
		PasswordEncoder enc = new PasswordEncoder();
		try {
			return enc.encodePassword(plainPassword, preferredEncoding);
		} catch (NoSuchAlgorithmException e) {
			throw new ProtocolException(e);
		} catch (DigestException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public void err(String msg) {
		Log.e(LOG_TAG, msg);
	}

	@Override
	public void err(Throwable e) {
		Log.e(LOG_TAG, e.getMessage(), e);
		if (DEBUG_LEVEL_ENABLED) {
			e.printStackTrace();
		}
	}

	public Main getFriendsListDisplayed() {
		return friendsListDisplayed;
	}

	@Override
	public void log(String msg) {
		if (LOG_LEVEL_ENABLED) {
			Log.i(LOG_TAG, msg);
		}
	}

	@Override
	public void notifyAccessDenied(String why) {
		sendMessageToMainUI(HANDLER_NOTIFY_DENIED, why);
	}

	@Override
	public void notifyConnected() {
		sendMessageToMainUI(HANDLER_NOTIFY_CONNECTED);
	}

	@Override
	public void notifyDisconnected() {
		sendMessageToMainUI(HANDLER_NOTIFY_OFFLINE);
	}

	@Override
	public void notifyFriendConnected(SpringAccount act) {
		sendMessageToMainUI(HANDLER_NOTIFY_FRIEND_CONNECTED, act.getUsername());
	}

	@Override
	public void notifyFriendDisconnected(SpringAccount act) {
		sendMessageToMainUI(HANDLER_NOTIFY_FRIEND_DISCONNECTED, act.getUsername());
	}

	@Override
	public void notifyFriendsOnlineChanged() {
		refreshUI();
	}

	@Override
	public void notifyLogin() {
		sendMessageToMainUI(HANDLER_NOTIFY_LOGIN);
	}

	@Override
	public void notifyLoginEnd() {
		sendMessageToMainUI(HANDLER_NOTIFY_ONLINE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		parser = new CommandParser(this);
	}

	@Override
	public void parse(String command, LobbyCommandListener client) throws ProtocolException {
		parser.parse(command, client);
	}

	private synchronized void refreshUI() {
		sendMessageToMainUI(HANDLER_REFRESH_UI);
	}

	private void sendMessageToMainUI(int action) {
		sendMessageToMainUI(action, null);
	}

	private void sendMessageToMainUI(int action, String info) {
		if (friendsListDisplayed != null) {
			friendsListDisplayed.sendMessageToMainThread(action, info);
		}
	}

	public void setFriendsListDisplayed(Main fld) {
		friendsListDisplayed = fld;
	}

	public void unsetFriendsListDisplayed() {
		friendsListDisplayed = null;
	}

	@Override
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
		Looper.prepare();
	}

}
