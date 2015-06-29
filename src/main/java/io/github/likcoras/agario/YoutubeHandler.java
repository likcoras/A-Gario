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
import lombok.extern.log4j.Log4j;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.gson.Gson;

@Log4j
public class YoutubeHandler implements Handler {
	
	private static final String API =
			"https://www.googleapis.com/youtube/v3/videos/?part=contentDetails,snippet,statistics&maxResults=1&id=%s&key=%s";
	private static final Pattern LINK_PATTERN =
			Pattern.compile("(?i)\\byoutu(\\.be|be\\.com)\\/(.*v(/|=)|(.*/)?)([\\w-]+)");
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
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
			throws HandlerException {
		final Matcher match = LINK_PATTERN.matcher(message);
		if (!match.find())
			return "";
		final String id = match.group(5);
		log.info("Youtube data for " + id + " requested");
		YoutubeInfo info;
		try {
			info = getYoutubeJson(String.format(API, id, apiKey));
		} catch (final IOException e) {
			throw new HandlerException(e);
		}
		if (!info.wasFound())
			return "";
		return String.format(YOUTUBE_FORMAT, info.getTitle(),
				info.getChannel(), info.getDuration(), info.getViews(),
				info.getLikes(), info.getDislikes());
	}
	
	private YoutubeInfo getYoutubeJson(String url)
			throws MalformedURLException, IOException {
		return GSON
				.fromJson(
						new InputStreamReader(new URL(String.format(url))
								.openStream()), YoutubeInfo.class);
	}
	
}
