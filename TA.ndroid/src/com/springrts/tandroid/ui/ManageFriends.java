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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.springrts.tandroid.R;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class ManageFriends extends TAndroidListActivity {
	private final static int DIALOG_CLAN = 1;
	private final static int DIALOG_FRIEND = 2;

	private class MyArrayAdapter extends ArrayAdapter<String> {
		private int textViewResourceId;

		public MyArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
			this.textViewResourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView view = (TextView) convertView;

			if (view == null) {
				LayoutInflater li = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = (TextView) li.inflate(textViewResourceId, null);
			}

			String act = getItem(position);

			if (act != null) {
				view.setText(act);
			}

			return view;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.manage_list);
	}

	@Override
	protected void notifyLobbyServiceConnected() {
		refresh();
	}

	private void refresh() {
		MyArrayAdapter adt = new MyArrayAdapter(this, R.layout.manage_row, lobby.getUsernamePatterns());
		setListAdapter(adt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.addclan:
			showDialog(DIALOG_CLAN);
			return true;
		case R.id.addfriend:
			showDialog(DIALOG_FRIEND);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.add_pattern, null);
		AlertDialog.Builder b = new AlertDialog.Builder(this).setView(textEntryView);
		final AlertDialog dialog = b.create();
		dialog.setButton2(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface di, int whichButton) {
				dialog.dismiss();
				refresh();
			}
		});
		switch (id) {
		case DIALOG_CLAN:
			dialog.setTitle(R.string.mng_add_clan);
			dialog.setButton(getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int whichButton) {
					EditText et = (EditText) dialog.findViewById(R.id.add_pattern_edit);
					String s = et.getText().toString();
					lobby.addClanToMonitor(s);
					dialog.dismiss();
					refresh();
				}
			});
			break;
		case DIALOG_FRIEND:
			dialog.setTitle(R.string.mng_add_friend);
			dialog.setButton(getResources().getText(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int whichButton) {
					EditText et = (EditText) dialog.findViewById(R.id.add_pattern_edit);
					String s = et.getText().toString();
					lobby.addFriendToMonitor(s);
					dialog.dismiss();
					refresh();
				}
			});
		}
		return dialog;

	}
}
