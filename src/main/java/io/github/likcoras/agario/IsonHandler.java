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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

public class IsonHandler implements Handler {
	
	private static final Logger LOG = Logger.getLogger(IsonHandler.class);
	
	private static final Pattern HOST_PATTERN = Pattern
		.compile("(?i)(([a-z0-9-]+\\.)+[a-z0-9-]+)(:(\\d+))?");
	
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
		return message.toLowerCase().startsWith("@isup ");
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException {
		final String url = message.split(" ")[1].trim();
		final Matcher match = HOST_PATTERN.matcher(url);
		if (!match.find())
			return "";
		if (isUp(match.group(1),
			match.group(4) != null ? Integer.parseInt(match.group(4)) : 80))
			return Colors.DARK_GREEN + url + " is up for me";
		return Colors.RED + url + " is down for me";
	}
	
	private boolean isUp(String host, int port) {
		LOG.info("Ison requested for " + host + ":" + port);
		final Socket connection = new Socket();
		try {
			connection.connect(new InetSocketAddress(host, port), 3000);
			connection.close();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}
	
}
