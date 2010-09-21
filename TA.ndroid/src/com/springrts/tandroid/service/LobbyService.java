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

	private MonitoringClient client;
	private Notification notification;

	private final IBinder mBinder = new LocalBinder();

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

	@Override
	public void log(String msg) {
		getTAndroid().log(msg);
	}

	private TAndroid getTAndroid() {
		return (TAndroid) getApplication();
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
		getTAndroid().notifyConnected();

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.notification;
		CharSequence tickerText = getResources().getText(R.string.st_online);
		long when = System.currentTimeMillis();
		notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.DEFAULT_ALL | Notification.FLAG_NO_CLEAR;
		Context context = getApplicationContext();
		CharSequence contentTitle = getResources().getText(R.string.app_name);
		CharSequence contentText = getResources().getText(R.string.st_online);
		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.number = 1;
		
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(1, notification);
	}

	@Override
	public void notifyDisconnected() {
		dbg("LobbyService.notifyDisconnected()");
		getTAndroid().notifyDisconnected();
		if (notification != null) {
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notification.flags = Notification.DEFAULT_ALL;
			mNotificationManager.notify(1, notification);
			mNotificationManager.cancel(1);
			// TODO en cours
		}
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
		}
	}

	@Override
	public void notifyLogin() {
		getTAndroid().notifyLogin();
	}

	@Override
	public void notifyLoginEnd() {
		getTAndroid().notifyLoginEnd();
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

		// TODO temp stuff
		client.addClan("FLM");
		// ---

		client.setStartPinger(true);

		notification = null;
	}

	public boolean isConnectedAndRunning() {
		return client.isConnectedAndRunning();
	}

	public List<SpringAccount> getActiveFriendsSince(long nbMinutes) {
		return client.getActiveFriendsSince(nbMinutes);
	}

	public int getNbFriendsOnline() {
		return client.getNbFriendsOnline();
	}

}
