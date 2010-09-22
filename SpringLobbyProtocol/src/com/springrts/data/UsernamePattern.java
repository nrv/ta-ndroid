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

import java.util.regex.Pattern;

/**
 * @author NRV - nherve75@gmail.com
 * @version 1.0.0
 */
public class UsernamePattern {
	private String asString;
	private Pattern asPattern;
	private String displayString;
	
	public UsernamePattern(String asString) {
		this(asString, asString);
	}
	
	public UsernamePattern(String patternString, String displayString) {
		super();
		this.asString = patternString;
		this.displayString = displayString;
		this.asPattern = Pattern.compile(asString, Pattern.CASE_INSENSITIVE);
	}

	public String getDisplay() {
		return displayString;
	}
	
	public String getPattern() {
		return asString;
	}
	
	public boolean matches(String s) {
		return asPattern.matcher(s).matches();
	}

}
