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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.springrts.platform.LogLayer;
import com.springrts.tandroid.R;
import com.springrts.tandroid.TAndroid;
import com.springrts.tandroid.service.LobbyService;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class Options extends PreferenceActivity implements OnPreferenceClickListener, LogLayer {
	public static final String ABOUT = "about";
	
	protected LobbyService lobby = null;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			lobby = ((LobbyService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			lobby = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		bindService(new Intent(this, LobbyService.class), serviceConnection, 0);
		
		getPreferenceManager().setSharedPreferencesName(TAndroid.PREFS);
		addPreferencesFromResource(R.layout.options);
		
		Preference pref = findPreference(ABOUT);
		pref.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (ABOUT.equals(preference.getKey())) {
			showDialog(0);
			return true;
		}

		return false;
	}

	protected Dialog onCreateDialog(int id) {
		
		if (id == 0) {
			Builder builder = new AlertDialog.Builder(this).setTitle(R.string.options_about);
			View view = getLayoutInflater().inflate(R.layout.about, null);
			TextView info = (TextView) view.findViewById(R.id.about_info);
			info.setText(Html.fromHtml(getString(R.string.options_about_info, getVersionNumber(this))));
			builder.setView(view);

			builder.setNegativeButton(R.string.ok, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

			return builder.create();
		}
		
		return null;
	}
	
	public static String getVersionNumber(Context context) {
		String version;
		try {
			PackageInfo packagInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = packagInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) { 
			version = "?";
		}
		return version;
	}

	@Override
	protected void onPause() {
		super.onPause();
		lobby.notifyConfigurationChanged();
	}

	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
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
	
	private TAndroid getTAndroid() {
		return (TAndroid) getApplication();
	}
	
}
