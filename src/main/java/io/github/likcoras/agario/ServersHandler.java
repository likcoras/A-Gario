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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.log4j.Log4j;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.gson.Gson;

@Log4j
public class ServersHandler implements Handler {
	
	private static final Gson gson = ServerInfo.getGson();
	
	private static final String SERVERS_FORMAT = BotUtil
			.addColors("%c%s: %n%d | ");
	private static final String SERVERS_TOTAL = BotUtil
			.addColors("%cTotal: %n%d players");
	
	@Override
	public void configure(BotConfig config) {}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
			throws HandlerException {
		if (!message.equalsIgnoreCase("@servers"))
			return "";
		log.info("Servers requested");
		ServerInfo info;
		try {
			info = getServersJson();
		} catch (final IOException e) {
			throw new HandlerException(e);
		}
		final String serverText = getServerText(info);
		final String totalText = getTotalText(info);
		return serverText + totalText;
	}
	
	private ServerInfo getServersJson() throws IOException {
		return gson.fromJson(new InputStreamReader(
				new URL("http://m.agar.io/info").openStream()), ServerInfo.class);
	}
	
	private String getServerText(ServerInfo info) {
		final Map<String, Integer> regions = info.getRegions();
		final StringBuffer out = new StringBuffer();
		for (final Entry<String, Integer> server : regions.entrySet())
			out.append(String.format(SERVERS_FORMAT, server.getKey(),
					server.getValue()));
		return out.toString();
	}
	
	private String getTotalText(ServerInfo info) {
		return String.format(SERVERS_TOTAL, info.getTotals());
	}
	
}
