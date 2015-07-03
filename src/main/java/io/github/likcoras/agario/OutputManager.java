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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Cleanup;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j;
import org.pircbotx.Channel;
import org.pircbotx.User;

@Log4j
public class OutputManager {
	
	private static final Path IGNORE = Paths.get("ignore");
	
	private final String ownerNick;
	private final String ownerHost;
	
	private final Map<String, Long> lastOut;
	private final Map<String, Long> lastSpam;
	private final List<String> ignored;
	
	public OutputManager(BotConfig config) throws IOException {
		ownerNick = config.getOthers().getOwnerNick();
		ownerHost = config.getOthers().getOwnerHost();
		lastOut = new HashMap<String, Long>();
		lastSpam = new HashMap<String, Long>();
		ignored = readIgnore();
	}
	
	@Synchronized
	public void add(String user) throws IOException {
		ignored.add(user);
		writeIgnore();
		log.info("User " + user + " ignored");
	}
	
	@Synchronized
	public void rem(String user) throws IOException {
		ignored.remove(user);
		writeIgnore();
		log.info("User " + user + " unignored");
	}
	
	public String getList() {
		return ignored.toString();
	}
	
	@Synchronized
	public boolean isIgnored(User user) {
		return ignored.contains(user.getHostmask());
	}
	
	@Synchronized
	public void out(Channel chan, User user, List<String> message)
			throws IOException {
		if (message.isEmpty())
			return;
		final String hostmask = user.getHostmask();
		final long now = System.currentTimeMillis();
		if (ownerNick.equalsIgnoreCase(user.getNick())
				&& ownerHost.equalsIgnoreCase(hostmask))
			sendLines(chan, message);
		else if (shouldFlag(lastOut, hostmask, now)) {
			lastSpam.put(hostmask, now);
			user.send().notice("Please don't spam.");
		} else if (shouldFlag(lastSpam, hostmask, now)) {
			lastSpam.remove(hostmask);
			ignored.add(hostmask);
			writeIgnore();
			user.send().notice("You have been ignored due to spam.");
			log.info("User " + user + " ignored due to spam");
		} else {
			lastOut.put(hostmask, now);
			sendLines(chan, message);
		}
		purgeOut(now);
	}
	
	private List<String> readIgnore() throws IOException {
		final List<String> ignored = new ArrayList<String>();
		if (!Files.exists(IGNORE))
			Files.createFile(IGNORE);
		@Cleanup
		final BufferedReader read =
				Files.newBufferedReader(IGNORE, StandardCharsets.UTF_8);
		String line;
		while ((line = read.readLine()) != null)
			ignored.add(line);
		return ignored;
	}
	
	private void writeIgnore() throws IOException {
		@Cleanup
		final BufferedWriter write =
				Files.newBufferedWriter(IGNORE, StandardCharsets.UTF_8);
		for (final String ignore : ignored)
			write.write(ignore + "\n");
		write.flush();
	}
	
	private boolean shouldFlag(Map<String, Long> map, String hostmask, long now) {
		return map.containsKey(hostmask) && now - map.get(hostmask) < 3000L;
	}
	
	private void sendLines(Channel chan, List<String> message) {
		for (final String line : message)
			if (!line.isEmpty())
				chan.send().message(line);
	}
	
	private void purgeOut(long now) {
		final Iterator<Entry<String, Long>> outIt = lastOut.entrySet().iterator();
		while (outIt.hasNext())
			if (now - outIt.next().getValue() < 3000L)
				outIt.remove();
		final Iterator<Entry<String, Long>> spamIt = lastSpam.entrySet().iterator();
		while (spamIt.hasNext())
			if (now - spamIt.next().getValue() < 3000L)
				spamIt.remove();
	}
	
}
