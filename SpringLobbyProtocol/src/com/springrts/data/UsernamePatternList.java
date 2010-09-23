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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class UsernamePatternList implements Iterable<UsernamePattern> {
	private Map<String, UsernamePattern> patterns;
	
	public UsernamePatternList() {
		super();
		patterns = new HashMap<String, UsernamePattern>();
	}
	
	public Iterator<UsernamePattern> patterns() {
		return patterns.values().iterator();
	}

	public void clear() {
		synchronized (patterns) {
			patterns.clear();
		}
	}

	public boolean contains(String u) {
		return patterns.containsKey(u.toUpperCase());
	}

	public UsernamePattern get(String u) {
		return patterns.get(u.toUpperCase());
	}

	public boolean isEmpty() {
		return patterns.isEmpty();
	}

	public void put(UsernamePattern a) {
		synchronized (patterns) {
			patterns.put(a.getDisplay().toUpperCase(), a);
		}
	}

	public void remove(String u) {
		synchronized (patterns) {
			patterns.remove(u.toUpperCase());
		}
	}

	public int size() {
		return patterns.size();
	}

	public Iterator<UsernamePattern> iterator() {
		return patterns();
	}

}
