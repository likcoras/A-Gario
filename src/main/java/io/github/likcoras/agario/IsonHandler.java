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
import java.net.InetAddress;
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
	
	private static final Pattern HOST_PATTERN =
		Pattern
			.compile("(?i)(([a-z0-9-]+\\.)+[a-z0-9-]+|\\[?(([0-9a-f]{1,4}:{1,2}){1,7}[0-9a-f]{1,4})\\]?)(:(\\d+))?");
	
	private static final String UP_MSG = Colors.DARK_GREEN + "%s (%s port %d) is up for me";
	private static final String DOWN_MSG = Colors.RED + "%s (%s port %d) is down for me";
	private static final String NO_MSG = Colors.RED + "%s is down for me";
	
	@Override
	public void configure(BotConfig config) {}
	
	@Override
	public boolean handlesEvent(Event<PircBotX> event) {
		return false;
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public boolean isHandlerOf(Channel chan, User user, String message) {
		return message.toLowerCase().startsWith("@isup ");
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message) {
		final String url = message.split(" ")[1].trim();
		final Matcher match = HOST_PATTERN.matcher(url);
		if (!match.find())
			return "";
		final InetSocketAddress addr = new InetSocketAddress(match.group(1), match.group(6) != null ? Integer.parseInt(match.group(6)) : 80);
		if (addr.isUnresolved())
			return String.format(NO_MSG, url);
		if (isUp(addr))
			return String.format(UP_MSG, url, addr.getAddress().getHostAddress(), addr.getPort());
		return String.format(DOWN_MSG, url, addr.getAddress().getHostAddress(), addr.getPort());
	}
	
	private boolean isUp(InetSocketAddress addr) {
		final InetAddress inet = addr.getAddress();
		LOG.info("Ison requested for " + inet.getHostAddress() + " port " + addr.getPort());
		if (inet.isAnyLocalAddress() || inet.isLoopbackAddress()
			|| inet.isLinkLocalAddress() || inet.isSiteLocalAddress())
			return false;
		try (Socket connection = new Socket()) {
			connection.connect(addr, 2000);
		} catch (final IOException e) {
			return false;
		}
		return true;
	}
	
}
