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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.User;

public class SpamHandler {
	
	private static final File IGNORE = new File("ignore");
	
	private static final Logger LOG = Logger.getLogger(SpamHandler.class);
	
	private final Map<String, Long> lastOut;
	private final Map<String, Long> lastSpam;
	private final List<String> ignored;
	
	public SpamHandler() throws IOException {
		lastOut = new ConcurrentHashMap<String, Long>();
		lastSpam = new ConcurrentHashMap<String, Long>();
		ignored = Collections.synchronizedList(readIgnore());
	}
	
	public void add(String user) throws IOException {
		ignored.add(user);
		writeIgnore();
		LOG.info("User " + user + " ignored");
	}
	
	public void rem(String user) throws IOException {
		ignored.remove(user);
		writeIgnore();
		LOG.info("User " + user + " unignored");
	}
	
	public String getList() {
		return ignored.toString();
	}
	
	public boolean isIgnored(User user) {
		return ignored.contains(user.getHostmask());
	}
	
	public void out(Channel chan, User user, String message) throws IOException {
		if (message.isEmpty())
			return;
		final String hostmask = user.getHostmask();
		final long now = System.currentTimeMillis();
		if (BotUtil.isLikc(user))
			chan.send().message(message);
		else if (lastOut.containsKey(hostmask)
			&& now - lastOut.get(hostmask) < 5000) {
			lastSpam.put(hostmask, now);
			chan.send().message(user, "Please don't spam.");
		} else if (lastSpam.containsKey(hostmask)
			&& now - lastSpam.get(hostmask) < 5000) {
			lastSpam.remove(hostmask);
			ignored.add(hostmask);
			writeIgnore();
			chan.send().message(
				"User " + user.getNick() + " ignored due to spam.");
			LOG.info("User " + user + " ignored due to spam");
		} else {
			lastSpam.remove(hostmask);
			lastOut.put(hostmask, now);
			chan.send().message(message);
		}
	}
	
	private List<String> readIgnore() throws IOException {
		final List<String> ignored = new ArrayList<String>();
		IGNORE.createNewFile();
		final BufferedReader read = new BufferedReader(new FileReader(IGNORE));
		String line;
		while ((line = read.readLine()) != null)
			ignored.add(line);
		read.close();
		return ignored;
	}
	
	private void writeIgnore() throws IOException {
		final BufferedWriter write = new BufferedWriter(new FileWriter(IGNORE));
		synchronized (ignored) {
			for (final String ignore : ignored)
				write.write(ignore + "\n");
		}
		write.flush();
		write.close();
	}
	
}
