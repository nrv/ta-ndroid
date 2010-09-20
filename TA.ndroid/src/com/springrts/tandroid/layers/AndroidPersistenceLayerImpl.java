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

package com.springrts.tandroid.layers;

import java.util.Map;
import java.util.regex.Pattern;

import android.content.SharedPreferences;

import com.springrts.data.SpringAccountList;
import com.springrts.platform.CommonPersistenceLayer;
import com.springrts.platform.PlatformLayer;
import com.springrts.protocol.ConnectionContext;
import com.springrts.protocol.ProtocolException;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class AndroidPersistenceLayerImpl extends CommonPersistenceLayer {
	private SharedPreferences settings;
	public static final String K_FRIENDS = "TA.ndroid-friends";
	public static final String K_CONTEXT = "TA.ndroid-context";
	public static final String K_PATTERNS = "TA.ndroid-patterns";

	public AndroidPersistenceLayerImpl(PlatformLayer hardware, SharedPreferences settings) {
		super(hardware);
		this.settings = settings;
	}

	@Override
	public SpringAccountList loadFriends() throws ProtocolException {
		try {
			return friendListFromString(settings.getString(K_FRIENDS, ""));
		} catch (NumberFormatException e) {
			throw new ProtocolException(e);
		}
	}

	@Override
	public void saveFriends(SpringAccountList lst) throws ProtocolException {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(K_FRIENDS, friendListAsString(lst));
		editor.commit();
	}

	@Override
	public ConnectionContext loadConnectionContext() throws ProtocolException {
		return contextFromString(settings.getString(K_CONTEXT, ""));
	}

	@Override
	public void saveConnectionContext(ConnectionContext ctx) throws ProtocolException {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(K_CONTEXT, contextAsString(ctx));
		editor.commit();
	}

	@Override
	public Map<String, Pattern> loadUsernamePatterns() throws ProtocolException {
		return usernamePatternsFromString(settings.getString(K_PATTERNS, ""));
	}

	@Override
	public void saveUsernamePatterns(Map<String, Pattern> p) throws ProtocolException {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(K_PATTERNS, usernamePatternsAsString(p));
		editor.commit();
	}
}
