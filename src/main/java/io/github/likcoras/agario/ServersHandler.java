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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.gson.Gson;

public class ServersHandler implements Handler {
	
	private static final Logger LOG = Logger.getLogger(ServersHandler.class);
	
	private static final Gson gson = ServerInfo.getBuilder().create();
	
	private static final String SERVERS_FORMAT = BotUtil
		.addColors("%c%s: %n%d | ");
	private static final String SERVERS_TOTAL = BotUtil
		.addColors("%cTotal: %n%d players");
	
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
		return message.equalsIgnoreCase("@servers");
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException {
		LOG.info("Servers requested");
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
		final InputStreamReader read =
			new InputStreamReader(new URL("http://m.agar.io/info").openStream());
		final ServerInfo info = gson.fromJson(read, ServerInfo.class);
		read.close();
		return info;
	}
	
	private String getServerText(ServerInfo info) {
		final Map<String, Integer> servers = info.getRegions();
		final Map<String, Integer> numPlayers = new HashMap<String, Integer>();
		for (final Entry<String, Integer> server : servers.entrySet()) {
			final String name =
				server.getKey().replaceAll(".+-", "").replaceAll(":.+", "");
			final int num =
				(numPlayers.get(name) == null ? 0 : numPlayers.get(name))
					+ server.getValue();
			numPlayers.put(name, num);
		}
		final StringBuffer out = new StringBuffer();
		for (final Entry<String, Integer> server : numPlayers.entrySet())
			out.append(String.format(SERVERS_FORMAT, server.getKey(),
				server.getValue()));
		return out.toString();
	}
	
	private String getTotalText(ServerInfo info) {
		return String.format(SERVERS_TOTAL, info.getTotals());
	}
	
}
