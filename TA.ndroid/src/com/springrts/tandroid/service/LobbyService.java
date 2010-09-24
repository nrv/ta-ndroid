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

package com.springrts.tandroid.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import com.springrts.client.MonitoringApplication;
import com.springrts.client.MonitoringClient;
import com.springrts.data.SpringAccount;
import com.springrts.data.UsernamePattern;
import com.springrts.platform.LogLayer;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.ProtocolException;
import com.springrts.tandroid.R;
import com.springrts.tandroid.TAndroid;
import com.springrts.tandroid.layers.AndroidNetworkLayerImpl;
import com.springrts.tandroid.layers.AndroidPersistenceLayerImpl;
import com.springrts.tandroid.ui.Main;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class LobbyService extends Service implements MonitoringApplication, LogLayer {
	public class LocalBinder extends Binder {
		public LobbyService getService() {
			return LobbyService.this;
		}
	}

	private static final int NOTIFICATION_ID = 1;

	private MonitoringClient client;
	// private NotificationManager notificationManager;
	// private Notification notification;

	private final IBinder mBinder = new LocalBinder();

	private int currentStatus;
	private String additionnalInformation;

	public void addClanToMonitor(String c) {
		client.addClanToMonitor(c);
	}

	public void addFriendToMonitor(String username) {
		client.addFriendToMonitor(username);
	}

	private void cancelNotification() {
		// if (notification != null) {
		dbg("LobbyService.notifyDisconnected() will try to cancel notification");
		//
		// CharSequence contentTitle =
		// getResources().getText(R.string.app_name);
		// CharSequence contentText =
		// getResources().getText(R.string.st_offline);
		// Intent notificationIntent = new Intent(this, Main.class);
		// PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		// notificationIntent, 0);
		//
		// notification.setLatestEventInfo(this, contentTitle, contentText,
		// contentIntent);
		// notification.flags = Notification.DEFAULT_ALL;
		//
		// updateNotification(0, true);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		// TODO en cours
		// }
	}

	private void createNotification() {
		// Need to do this way to have numbers activated on this notification
		updateNotification(1, null, null, true);
		updateNotification(0, getResources().getText(R.string.st_online), getResources().getText(R.string.st_online), true);
	}

	@Override
	public void dbg(String msg) {
		getTAndroid().dbg(msg);
	}

	@Override
	public void dbg(Throwable e) {
		getTAndroid().dbg(e);
	}

	@Override
	public void err(String msg) {
		getTAndroid().err(msg);
	}

	@Override
	public void err(Throwable e) {
		getTAndroid().err(e);
	}

	public List<SpringAccount> getActiveFriendsSince(long nbMinutes) {
		return client.getActiveFriendsSince(nbMinutes);
	}

	public String getAdditionnalInformation() {
		return additionnalInformation;
	}

	public int getCurrentStatus() {
		return currentStatus;
	}

	public int getNbFriendsOnline() {
		return client.getNbFriendsOnline();
	}

	private TAndroid getTAndroid() {
		return (TAndroid) getApplication();
	}

	public List<String> getUsernamePatterns() {
		ArrayList<String> lst = new ArrayList<String>();
		for (UsernamePattern p : client.getUsernamePatterns()) {
			lst.add(p.getDisplay());
		}
		Collections.sort(lst);
		return lst;
	}

	public boolean isConnectedAndRunning() {
		return client.isConnectedAndRunning();
	}

	@Override
	public void log(String msg) {
		getTAndroid().log(msg);
	}

	public void login() {
		try {
			client.loadConnectionContext();
			client.connect();
		} catch (ProtocolException e) {
			err(e);
		}
	}

	public void logout() {
		client.disconnect();
	}

	@Override
	public void notifyAccessDenied(String why) {
		currentStatus = R.string.st_denied;
		additionnalInformation = why;
		getTAndroid().notifyAccessDenied(why);
	}

	public void notifyConfigurationChanged() {
		ConnectionContext context = client.getContext();
		if (context == null) {
			context = ConnectionContext.defaultContext();
		}

		SharedPreferences settings = getSharedPreferences(TAndroid.PREFS, MODE_PRIVATE);

		context.setLogin(settings.getString("cnx_login", ""));
		try {
			context.setEncodedPassword(getTAndroid().encodePassword(settings.getString("cnx_password", ""), context.getCharset()));
		} catch (ProtocolException e1) {
			err(e1);
		}
		context.setServerIP(settings.getString("cnx_server", ""));
		try {
			context.setServerPort(settings.getString("cnx_server_port", ""));
		} catch (ProtocolException e) {
			err(e);
		}

		try {
			client.saveConnectionContext(context);

			// TODO reconnect if something has changed
		} catch (ProtocolException e) {
			err(e);
		}
	}

	@Override
	public void notifyConnected() {
		currentStatus = R.string.st_connected;
		additionnalInformation = null;
		getTAndroid().notifyConnected();
		createNotification();
	}

	@Override
	public void notifyDisconnected() {
		currentStatus = R.string.st_offline;
		additionnalInformation = null;
		getTAndroid().notifyDisconnected();
		cancelNotification();
	}

	@Override
	public void notifyFriendConnected(SpringAccount act) {
		if (client.isLoginFinished()) {
			getTAndroid().notifyFriendConnected(act);
		}
	}

	@Override
	public void notifyFriendDisconnected(SpringAccount act) {
		if (client.isLoginFinished()) {
			getTAndroid().notifyFriendDisconnected(act);
		}
	}

	@Override
	public void notifyFriendsOnlineChanged() {
		if (client.isLoginFinished()) {
			getTAndroid().notifyFriendsOnlineChanged();
			
			updateNotification(getNbFriendsOnline(), null, getNbFriendsOnline() + " " + getResources().getText(R.string.connected_friends), false);
		}
	}

	@Override
	public void notifyLogin() {
		currentStatus = R.string.st_login;
		additionnalInformation = null;
		getTAndroid().notifyLogin();
	}

	@Override
	public void notifyLoginEnd() {
		currentStatus = R.string.st_online;
		additionnalInformation = null;
		getTAndroid().notifyLoginEnd();
		try {
			client.join("tandroid");
		} catch (ProtocolException e) {
			err(e);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		client = new MonitoringClient(this);

		AndroidNetworkLayerImpl nwk = new AndroidNetworkLayerImpl(getTAndroid());
		client.setHardware(getTAndroid());
		client.setRemote(nwk);

		SharedPreferences settings = getSharedPreferences(TAndroid.PREFS, MODE_PRIVATE);
		AndroidPersistenceLayerImpl pers = new AndroidPersistenceLayerImpl(getTAndroid(), settings);
		client.setPersistence(pers);

		try {
			client.loadParameters();
		} catch (ProtocolException e) {
			err(e);
		}

		client.setStartPinger(true);

		

		currentStatus = R.string.st_offline;
		additionnalInformation = null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		// return START_STICKY;
		return 1;
	}

	private void updateNotification(int nb, CharSequence tickerText, CharSequence contentText, boolean force) {
		if (isConnectedAndRunning() || force) {
			int icon = R.drawable.notification;
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags = Notification.FLAG_NO_CLEAR;

			CharSequence contentTitle = getResources().getText(R.string.app_name);
			Intent notificationIntent = new Intent(this, Main.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

			notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
			
			notification.number = nb;
			notification.vibrate = new long[] { 100, 250 };
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	public void removeUsernamePatternFromMonitoring(String p) {
		client.removeUsernamePatternFromMonitoring(p);
	}

	public void clearFriends() throws ProtocolException {
		client.clearFriends();
	}

}
