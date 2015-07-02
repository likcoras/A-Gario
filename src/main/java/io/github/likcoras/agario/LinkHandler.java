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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

@Log4j
public class LinkHandler implements Handler {
	
	private static final Path LINKS = Paths.get("links");
	
	private static final String LINK_MSG = "Added link '%s' to '%s'";
	private static final String LINK_REM = "Link '%s' removed";
	private static final String LINK_LIST = BotUtil.addColors("%cLinks:%n");
	private static final Pattern LINK_REGEX = Pattern.compile("\\B(~\\S+)");
	
	private Properties links;
	
	@Override
	public void configure(BotConfig config) throws HandlerException {
		try {
			links = readLinks();
		} catch (final IOException e) {
			throw new HandlerException(e);
		}
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
			throws HandlerException {
		if (!message.toLowerCase().startsWith("@link ")) {
			final Matcher linkMatch = LINK_REGEX.matcher(message);
			if (linkMatch.find())
				return link(linkMatch.group(1));
			return "";
		}
		try {
			if (BotUtil.isOp(chan, user))
				user.send().notice(getLinksOp(message));
			else
				user.send().notice(getLinks(message));
		} catch (final IOException e) {
			throw new HandlerException(e);
		}
		return "";
	}
	
	private String link(String link) {
		link = link.toLowerCase();
		final String out = Strings.nullToEmpty(links.getProperty(link));
		if (!out.isEmpty())
			log.info(link + " requested");
		return out;
	}
	
	private String getLinksOp(String message) throws IOException {
		final List<String> args = getArgs(message);
		if (args.size() > 2 && args.get(0).equalsIgnoreCase("add"))
			return setLink("~" + Colors.removeFormattingAndColors(args.get(1)),
					args.get(2));
		else if (args.size() > 1 && args.get(0).equalsIgnoreCase("rem"))
			return delLink("~" + args.get(1));
		return getLinks(args);
	}
	
	private String getLinks(String message) throws IOException {
		return getLinks(getArgs(message));
	}
	
	private String getLinks(List<String> args) throws IOException {
		if (args.size() > 2 && args.get(0).equalsIgnoreCase("add"))
			return addLink("~" + Colors.removeFormattingAndColors(args.get(1)),
					args.get(2));
		else if (args.size() > 0 && args.get(0).equalsIgnoreCase("list"))
			return getLinkList();
		return "";
	}
	
	private Properties readLinks() throws IOException {
		final Properties links = new Properties();
		if (!Files.exists(LINKS))
			Files.createFile(LINKS);
		links.load(Files.newBufferedReader(LINKS, StandardCharsets.UTF_8));
		return links;
	}
	
	private void writeLinks(Properties properties) throws IOException {
		properties.store(
				Files.newBufferedWriter(LINKS, StandardCharsets.UTF_8), "");
	}
	
	private List<String> getArgs(String message) {
		final Iterator<String> it =
				Splitter.on(" ").trimResults().omitEmptyStrings().limit(3)
						.split(message.substring(6)).iterator();
		final ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (int i = 0; it.hasNext(); i++)
			if (i == 1)
				builder.add(it.next().toLowerCase());
			else
				builder.add(it.next());
		return builder.build();
	}
	
	private String setLink(String link, String target) throws IOException {
		links.setProperty(link, target);
		writeLinks(links);
		log.info("Link " + link + " added");
		return String.format(LINK_MSG, link, target);
	}
	
	private String delLink(String link) throws IOException {
		if (!links.containsKey(link))
			return "";
		links.remove(link);
		writeLinks(links);
		log.info("Link " + link + " removed");
		return String.format(LINK_REM, link);
	}
	
	private String addLink(String link, String target) throws IOException {
		if (links.containsKey(link))
			return "";
		return setLink(link, target);
	}
	
	private String getLinkList() {
		final StringBuffer out = new StringBuffer(LINK_LIST);
		for (final String link : new TreeSet<String>(links.stringPropertyNames()))
			out.append(" " + link);
		return out.toString();
	}
	
}
