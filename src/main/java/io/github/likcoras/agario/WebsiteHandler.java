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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.gson.Gson;

public class WebsiteHandler implements Handler {
	
	private static final Logger LOG = Logger.getLogger(WebsiteHandler.class);
	
	private static final String API =
		"https://www.googleapis.com/youtube/v3/videos/?part=contentDetails,snippet,statistics&maxResults=1&id=%s&key=%s";
	private static final Pattern LINK_PATTERN = Pattern
		.compile("(?i)\\byoutu(\\.be|be\\.com)\\/(.*v(/|=)|(.*/)?)([\\w-]+)");
	private static final Gson GSON = YoutubeInfo.getGson();
	
	private static final String YOUTUBE_FORMAT = BotUtil.addColors("["
		+ Colors.RED + "Youtube%n" + "] %s - by %s [%s] [%d views] %c[%d] "
		+ Colors.RED + "[%d]");
	
	private String apiKey;
	
	@Override
	public void configure(BotConfig config) {
		apiKey = config.getOthers().getApiKey();
	}
	
	@Override
	public boolean handlesEvent(Event<PircBotX> event) {
		return false;
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public boolean isHandlerOf(Channel chan, User user, String message) {
		return LINK_PATTERN.matcher(message).find();
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException {
		final Matcher match = LINK_PATTERN.matcher(message);
		match.find();
		final String id = match.group(5);
		LOG.info("Youtube data for " + id + " requested");
		YoutubeInfo info;
		try {
			info = getYoutubeJson(String.format(API, id, apiKey));
		} catch (final IOException io) {
			throw new HandlerException(io);
		}
		if (!info.wasFound())
			return "";
		return String.format(YOUTUBE_FORMAT, info.getTitle(),
			info.getChannel(), info.getDuration(), info.getViews(),
			info.getLikes(), info.getDislikes());
	}
	
	private YoutubeInfo getYoutubeJson(String url)
		throws MalformedURLException, IOException {
		final InputStreamReader read =
			new InputStreamReader(new URL(String.format(url)).openStream());
		final YoutubeInfo info = GSON.fromJson(read, YoutubeInfo.class);
		read.close();
		return info;
	}
	
}
