package io.github.likcoras.agario;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

public class WebsiteHandler implements Handler {
	
	private static final Pattern LINK_PATTERN = Pattern.compile("\\b(http(s)?://([a-zA-Z0-9-]+\\.)+[a-zA-Z]+(/\\S*)?)\\b");
	
	@Override
	public void configure(BotConfig config) throws HandlerException {}
	
	@Override
	public boolean handlesEvent(Event<PircBotX> event) {
		return false;
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) throws HandlerException {}
	
	@Override
	public boolean isHandlerOf(Channel chan, User user, String message) {
		return LINK_PATTERN.matcher(message).find();
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException {
		Matcher match = LINK_PATTERN.matcher(message);
		match.find();
		String url = match.group(1);
		try {
			return user.getNick() + ": " + Jsoup.connect(url).userAgent("").get().title();
		} catch (IOException e) {
			return "";
		}
	}
	
}
