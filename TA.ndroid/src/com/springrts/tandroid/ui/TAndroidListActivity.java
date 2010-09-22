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

package com.springrts.tandroid.ui;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.springrts.platform.LogLayer;
import com.springrts.tandroid.TAndroid;
import com.springrts.tandroid.service.LobbyService;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public abstract class TAndroidListActivity extends ListActivity implements LogLayer {
	protected LobbyService lobby = null;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			lobby = ((LobbyService.LocalBinder) service).getService();
			notifyLobbyServiceConnected();
		}

		public void onServiceDisconnected(ComponentName className) {
			lobby = null;
		}
	};
	
	protected abstract void notifyLobbyServiceConnected();
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, LobbyService.class), serviceConnection, 0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		lobby = null;
		unbindService(serviceConnection);
	}
	
	protected TAndroid getTAndroid() {
		return (TAndroid) getApplication();
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

	@Override
	public void log(String msg) {
		getTAndroid().log(msg);
	}
}
