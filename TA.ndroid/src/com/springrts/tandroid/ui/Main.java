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

import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.springrts.data.SpringAccount;
import com.springrts.platform.LogLayer;
import com.springrts.tandroid.R;
import com.springrts.tandroid.TAndroid;
import com.springrts.tandroid.service.LobbyService;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class Main extends ListActivity implements LogLayer {
	public static final String HANDLER_ACTION = "a";
	public static final String HANDLER_INFO = "i";

	private Handler handler;
	private UIRefresher refresher;

	private LobbyService lobby = null;
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			lobby = ((LobbyService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			lobby = null;
		}
	};

	private class UIRefresher extends Thread {
		public UIRefresher() {
			super();
			doRefresh = true;
		}

		private boolean doRefresh;

		public synchronized void run() {
			int refreshInterval = 15 * 1000;
			while (doRefresh) {
				try {
					wait(refreshInterval);
					refresh();
				} catch (InterruptedException e) {
				}
			}
		}

		public synchronized void stopRefresher() {
			doRefresh = false;
			notify();
		}
	}

	private void logout() {
		lobby.logout();
	}

	private void exit() {
		logout();
		if (refresher != null) {
			refresher.stopRefresher();
			refresher = null;
		}
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bindService(new Intent(this, LobbyService.class), serviceConnection, Context.BIND_AUTO_CREATE);

		dbg("FriendsList.onCreate()");

		refresher = null;

		class MyArrayAdapter extends ArrayAdapter<SpringAccount> {
			public MyArrayAdapter(Context context, int textViewResourceId, List<SpringAccount> objects) {
				super(context, textViewResourceId, objects);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView view = (TextView) convertView;

				if (view == null) {
					LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					view = (TextView) li.inflate(R.layout.friend_row, null);
				}

				SpringAccount act = getItem(position);

				if (act != null) {
					view.setText(act.shortDisplay(getResources().getText(R.string.st_online).toString(), getResources().getText(R.string.min).toString(), getResources().getText(R.string.hour).toString(), getResources().getText(R.string.day).toString()));
				}

				return view;
			}
		}

		class MyHandler extends Handler {
			private Main fl;

			public MyHandler(Main fl) {
				super();
				this.fl = fl;
			}

			public void handleMessage(Message msg) {
				Bundle data = msg.getData();
				int action = data.getInt(HANDLER_ACTION);
				String info = null;
				if (data.containsKey(HANDLER_INFO)) {
					info = data.getString(HANDLER_INFO);
				}

				switch (action) {
				case TAndroid.HANDLER_REFRESH_UI:
//					log("You have now " + client.getNbFriendsOnline() + " friends online :");
//					List<SpringAccount> aa = new ArrayList<SpringAccount>();
//					for (SpringAccount act : client.getActiveFriendsSince(15)) {
//						aa.add(act);
//						log("  - " + act.shortDisplay());
//					}
//					MyArrayAdapter adt = new MyArrayAdapter(fl, R.layout.friend_row, aa);
//					setListAdapter(adt);
					break;
				case TAndroid.HANDLER_NOTIFY_LOGIN:
					setStatus(R.string.st_login, info);
					break;
				case TAndroid.HANDLER_NOTIFY_OFFLINE:
					setStatus(R.string.st_offline, info);
					break;
				case TAndroid.HANDLER_NOTIFY_ONLINE:
					setStatus(R.string.st_online, info);
					if (refresher != null) {
						refresher.stopRefresher();
					}
					refresher = new UIRefresher();
					refresher.start();
					break;
				case TAndroid.HANDLER_NOTIFY_CONNECTED:
					setStatus(R.string.st_connected, info);
					break;
				case TAndroid.HANDLER_NOTIFY_DENIED:
					setStatus(R.string.st_denied, info);
					Toast.makeText(fl, info, Toast.LENGTH_LONG).show();
					break;
				case TAndroid.HANDLER_NOTIFY_FRIEND_CONNECTED:
					String st = info + " " + getResources().getText(R.string.fr_connected);
					Toast.makeText(fl, st, Toast.LENGTH_LONG).show();
					// String ns = Context.NOTIFICATION_SERVICE;
					// NotificationManager mNotificationManager =
					// (NotificationManager) getSystemService(ns);
					// int icon = R.drawable.notification;
					// CharSequence tickerText = st;
					// long when = System.currentTimeMillis();
					//
					// Notification notification = new Notification(icon,
					// tickerText, when);
					// Context context = getApplicationContext();
					// CharSequence contentTitle =
					// getResources().getText(R.string.app_name);
					// CharSequence contentText = st;
					// Intent notificationIntent = new Intent(fl,
					// FriendsList.class);
					// PendingIntent contentIntent =
					// PendingIntent.getActivity(fl, 0, notificationIntent, 0);
					//
					// notification.setLatestEventInfo(context, contentTitle,
					// contentText, contentIntent);
					//					
					//
					// mNotificationManager.notify(1, notification);
					break;
				case TAndroid.HANDLER_NOTIFY_FRIEND_DISCONNECTED:
					String st2 = info + " " + getResources().getText(R.string.fr_disconnected);
					Toast.makeText(fl, st2, Toast.LENGTH_LONG).show();
					break;
				}

			}
		}

		handler = new MyHandler(this);

		setContentView(R.layout.friends_list);

		// notifyDisconnected();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		dbg("FriendsList.onCreateOptionsMenu()");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.login:
			lobby.login();
			return true;
		case R.id.logout:
			logout();
			return true;
		case R.id.exit:
			exit();
			return true;
		case R.id.refresh:
			refresh();
			return true;
		case R.id.options:
			startActivity(new Intent(this, Options.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refresh() {
		// TODO
		// refreshUI();
	}

	public void setStatus(int msg, String info) {
		TextView status = (TextView) findViewById(R.id.status);
		String st = getResources().getText(R.string.status) + " " + getResources().getText(msg);
		if (info != null) {
			st += " (" + info + ")";
		}
		status.setText(st);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (lobby != null) {
			unbindService(serviceConnection);
		}
		dbg("FriendsList.onDestroy()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		dbg("FriendsList.onPause()");
		getTAndroid().unsetFriendsListDisplayed();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		dbg("FriendsList.onRestart()");
	}

	@Override
	protected void onResume() {
		super.onResume();
		dbg("FriendsList.onResume()");
		getTAndroid().setFriendsListDisplayed(this);
	}

	private TAndroid getTAndroid() {
		return (TAndroid) getApplication();
	}

	@Override
	protected void onStart() {
		super.onStart();
		dbg("FriendsList.onStart()");
	}

	@Override
	protected void onStop() {
		super.onStop();
		dbg("FriendsList.onStop()");
	}

	public void sendMessageToMainThread(int action, String info) {
		log("sendMessageToMainThread(" + action + ") " + info);
		Message msg = handler.obtainMessage();
		Bundle data = new Bundle();
		data.putInt(HANDLER_ACTION, action);
		if (info != null) {
			data.putString(HANDLER_INFO, info);
		}
		msg.setData(data);
		handler.sendMessage(msg);
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
