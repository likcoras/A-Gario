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
import org.apache.log4j.Logger;
import com.google.gson.Gson;

public class ServersHandler {
	
	private static final String SERVERS_FORMAT = BotUtil
		.addColors("%t%s: %g%d %n| ");
	private static final String SERVERS_TEAM = BotUtil
		.addColors("%t%s: %g%d %tTeams: %g%d %n| ");
	private static final String SERVERS_TOTAL = BotUtil
		.addColors("%tTotal: %g%d players");
	
	private static final Logger LOG = Logger.getLogger(ServersHandler.class);
	
	private final Gson gson;
	
	public ServersHandler() {
		gson = ServerInfo.getBuilder().create();
	}
	
	public String getServers() throws IOException {
		LOG.info("Servers requested");
		final ServerInfo info = getServersJson();
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
		final StringBuffer out = new StringBuffer();
		for (final Entry<String, Integer> server : servers.entrySet()) {
			final String name = server.getKey();
			if (name.endsWith(":teams"))
				continue;
			final String shortName = name.replaceAll(".+-", "");
			final String teamName = name + ":teams";
			final int num = server.getValue();
			if (servers.containsKey(teamName))
				out.append(String.format(SERVERS_TEAM, shortName, num,
					servers.get(teamName)));
			else
				out.append(String.format(SERVERS_FORMAT, shortName, num));
		}
		return out.toString();
	}
	
	private String getTotalText(ServerInfo info) {
		return String.format(SERVERS_TOTAL, info.getTotals());
	}
	
}
