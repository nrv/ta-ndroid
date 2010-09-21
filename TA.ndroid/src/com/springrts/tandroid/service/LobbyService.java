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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;

import com.springrts.client.MonitoringApplication;
import com.springrts.client.MonitoringClient;
import com.springrts.data.SpringAccount;
import com.springrts.platform.LogLayer;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.ProtocolException;
import com.springrts.tandroid.TAndroid;
import com.springrts.tandroid.layers.AndroidNetworkLayerImpl;
import com.springrts.tandroid.layers.AndroidPersistenceLayerImpl;

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
		return (TAndroid)getApplication();
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
		dbg("LobbyService.notifyConfigurationChanged()");
		ConnectionContext context = client.getContext();
		if (context == null) {
			context = ConnectionContext.defaultContext();
		}
		
		SharedPreferences settings = getSharedPreferences(TAndroid.PREFS, MODE_PRIVATE);
		
		dbg("context = " + context + " - settings = " + settings);
		
		for (String k : settings.getAll().keySet()) {
			dbg("key : " + k + " = " + settings.getString(k, ""));
		}
		
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
			
			//TODO reconnect if something has changed
		} catch (ProtocolException e) {
			err(e);
		}
		
	}



	@Override
	public void notifyConnected() {
		getTAndroid().notifyConnected();
	}

	@Override
	public void notifyDisconnected() {
		getTAndroid().notifyDisconnected();
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
		dbg("LobbyService.onBind()");
		return mBinder;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		dbg("LobbyService.onConfigurationChanged()");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		dbg("LobbyService.onCreate()");

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
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dbg("LobbyService.onDestroy()");
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		dbg("LobbyService.onLowMemory()");
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		dbg("LobbyService.onRebind()");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		dbg("LobbyService.onStart()");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		dbg("LobbyService.onUnbind()");
		return super.onUnbind(intent);
	}

}
