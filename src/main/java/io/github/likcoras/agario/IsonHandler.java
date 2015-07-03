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
import lombok.extern.log4j.Log4j;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.common.base.Optional;

@Log4j
public class IsonHandler implements Handler {
	
	private static final Pattern HOST_PATTERN =
			Pattern.compile("(?i)(([a-z0-9-]+\\.)+[a-z0-9-]+|\\[?(([0-9a-f]{1,4}:{1,2}){1,7}[0-9a-f]{1,4})\\]?)(:(\\d+))?");
	
	private static final String UP_MSG = Colors.DARK_GREEN
			+ "%s is up for me";
	private static final String DOWN_MSG = Colors.RED
			+ "%s is down for me";
	
	@Override
	public void configure(BotConfig config) {}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public String getResponse(Channel chan, User user, String message) {
		if (!message.toLowerCase().startsWith("@isup ") || message.length() == 6)
			return "";
		String target = message.substring(6).toLowerCase();
		log.info("Ison requested for " + target);
		Optional<InetSocketAddress> addr = getAddr(target);
		if (addr.isPresent() && checkUpDown(addr.get()))
			return String.format(UP_MSG, target);
		return String.format(DOWN_MSG, target);
	}
	
	private Optional<InetSocketAddress> getAddr(String target) {
		final Matcher match = HOST_PATTERN.matcher(target);
		if (!match.find())
			return Optional.absent();
		return Optional.of(new InetSocketAddress(match.group(1), match.group(6) != null
						? Integer.parseInt(match.group(6)) : 80));
	}
	
	private boolean checkUpDown(InetSocketAddress addr) {
		if (addr.isUnresolved() || isInvalid(addr.getAddress()))
			return false;
		try (Socket con = new Socket()) {
			con.connect(addr);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	private boolean isInvalid(InetAddress inet) {
		return inet.isAnyLocalAddress() || inet.isLoopbackAddress()
				|| inet.isLinkLocalAddress() || inet.isSiteLocalAddress();
	}
	
}
