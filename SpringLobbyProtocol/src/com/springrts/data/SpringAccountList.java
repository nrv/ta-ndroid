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

package com.springrts.data;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SpringAccountList implements Iterable<SpringAccount> {
	private Map<String, SpringAccount> accounts;

	public SpringAccountList() {
		super();
		accounts = new HashMap<String, SpringAccount>();
	}

	public Iterator<String> accountNames() {
		return accounts.keySet().iterator();
	}

	public Iterator<SpringAccount> accounts() {
		return accounts.values().iterator();
	}

	public void clear() {
		synchronized (accounts) {
			accounts.clear();
		}
	}

	public boolean contains(String u) {
		return accounts.containsKey(u.toUpperCase());
	}

	public SpringAccount get(String u) {
		return accounts.get(u);
	}

	public boolean isEmpty() {
		return accounts.isEmpty();
	}

	public void put(SpringAccount a) {
		synchronized (accounts) {
			accounts.put(a.getUsername().toUpperCase(), a);
		}
	}

	public void remove(String u) {
		synchronized (accounts) {
			accounts.remove(u.toUpperCase());
		}
	}

	public int size() {
		return accounts.size();
	}

	public Iterator<SpringAccount> iterator() {
		return accounts();
	}
}
