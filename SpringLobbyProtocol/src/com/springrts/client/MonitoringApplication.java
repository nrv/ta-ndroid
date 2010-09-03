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

package com.springrts.client;

import com.springrts.data.SpringAccount;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public interface MonitoringApplication {
	void notifyFriendsOnlineChanged();
	void notifyConnected();
	void notifyLogin();
	void notifyAccessDenied(String why);
	void notifyLoginEnd();
	void notifyDisconnected();
	void notifyFriendDisconnected(SpringAccount act);
	void notifyFriendConnected(SpringAccount act);
}
