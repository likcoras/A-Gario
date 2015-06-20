/*
 * Copyright 2015 likcoras
 * 
 * This file is part of A-Gario
 * 
 * A-Gario is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * A-Gario is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with A-Gario.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.likcoras.agario;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

public class WebsiteHandler implements Handler {
	
	private static final Pattern LINK_PATTERN = Pattern.compile("(?i)\\b(http(s)?://([a-z0-9-]+\\.)+[a-z]+(/\\S*)?)\\b");
	
	@Override
	public void configure(BotConfig config) throws HandlerException {}
	
	@Override
	public boolean handlesEvent(Event<PircBotX> event) {
		return false;
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) throws HandlerException {}
	
	@Override
	public boolean isHandlerOf(Channel chan, User user, String message) {
		return LINK_PATTERN.matcher(message).find();
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException {
		Matcher match = LINK_PATTERN.matcher(message);
		match.find();
		String url = match.group(1);
		try {
			return user.getNick() + ": " + Jsoup.connect(url).userAgent("").get().title();
		} catch (IOException e) {
			return "";
		}
	}
	
}
