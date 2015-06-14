package io.github.likcoras.agario;

import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

public interface Handler {
	
	public void configure(BotConfig config) throws HandlerException;
	
	public boolean handlesEvent(Event<PircBotX> event);
	
	public void handleEvent(Event<PircBotX> event) throws HandlerException;
	
	public boolean isHandlerOf(Channel chan, User user, String message);
	
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException;
	
}
