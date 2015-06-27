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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
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

@Log4j
public class LinkHandler implements Handler {
	
	private static final File LINKS = new File("links");
	
	private static final String LINK_MSG = "Added link '%s' to '%s'";
	private static final String LINK_REM = "Link '%s' removed";
	private static final String LINK_LIST = BotUtil.addColors("%cLinks:%n");
	private static final Pattern LINK_REGEX = Pattern.compile("\\B([~?]\\S+)");
	
	private Properties links;
	
	@Override
	public void configure(BotConfig config) throws HandlerException {
		try {
			links = readLinks(LINKS);
		} catch (final IOException e) {
			throw new HandlerException(e);
		}
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
			throws HandlerException {
		final Matcher linkMatch = LINK_REGEX.matcher(message);
		if (linkMatch.find())
			return link(linkMatch.group(1));
		if (!message.toLowerCase().startsWith("@link "))
			return "";
		try {
			if (BotUtil.isLikc(user))
				return getLinksLikc(message);
			if (BotUtil.isOp(chan, user))
				return getLinksOp(message);
			return getLinks(message);
		} catch (final IOException e) {
			throw new HandlerException(e);
		}
	}
	
	private String link(String link) {
		final String out = Strings.nullToEmpty(links.getProperty(link));
		if (!out.isEmpty())
			log.info(link + " requested");
		return out;
	}
	
	private String getLinksLikc(String message) throws IOException {
		final List<String> args = getArgs(message);
		if (args.size() > 2 && args.get(0).equalsIgnoreCase("put"))
			return setLink("?" + args.get(1), args.get(2));
		else if (args.size() > 1 && args.get(0).equalsIgnoreCase("del"))
			return delLink("?" + args.get(1));
		return getLinksOp(args);
	}
	
	private String getLinksOp(String message) throws IOException {
		return getLinksOp(getArgs(message));
	}
	
	private String getLinksOp(List<String> args) throws IOException {
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
	
	private Properties readLinks(File file) throws IOException {
		final Properties links = new Properties();
		file.createNewFile();
		links.load(new FileReader(file));
		return links;
	}
	
	private void writeLinks(Properties properties, File file)
			throws IOException {
		properties.store(new FileWriter(file), "");
	}
	
	private List<String> getArgs(String message) {
		return Splitter.on(" ").trimResults().omitEmptyStrings().limit(3)
				.splitToList(message.substring(6));
	}
	
	private String setLink(String link, String target) throws IOException {
		links.setProperty(link, target);
		writeLinks(links, LINKS);
		log.info("Link " + link + " added");
		return String.format(LINK_MSG, link, target);
	}
	
	private String delLink(String link) throws IOException {
		if (!links.containsKey(link))
			return "";
		links.remove(link);
		writeLinks(links, LINKS);
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
		for (final String link : links.stringPropertyNames())
			out.append(" " + link);
		return out.toString();
	}
	
}
