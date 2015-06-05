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
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickAlreadyInUseEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import com.google.common.base.Splitter;

public class AGario extends ListenerAdapter<PircBotX> {
	
	private static final String HELP_MSG =
		BotUtil
			.addColors("%yCommands: %g@help %n| %y@info %n| %g@servers %n| %y@link %n| %g~[link] %n| %y?[link]");
	
	private static final Logger LOG = Logger.getLogger(AGario.class);
	
	private final SpamHandler spam;
	private final InfoHandler info;
	private final ServersHandler servers;
	private final LinkHandler link;
	
	public static void main(String[] args) {
		try {
			new AGario();
		} catch (IOException | IrcException e) {
			LOG.error("Startup Error: ", e);
		}
	}
	
	private AGario() throws IOException, IrcException {
		LOG.info("Starting bot...");
		spam = new SpamHandler();
		info = new InfoHandler();
		servers = new ServersHandler();
		link = new LinkHandler();
		new PircBotX(configureBot(BotConfig.getConfig())).startBot();
	}
	
	@Override
	public void onNickAlreadyInUse(NickAlreadyInUseEvent<PircBotX> event) {
		LOG.warn("Nickname is already in use!");
	}
	
	@Override
	public void onConnect(ConnectEvent<PircBotX> event) {
		info.setStart();
		LOG.info("Connected");
	}
	
	@Override
	public void onAction(ActionEvent<PircBotX> event) throws IOException {
		final User user = event.getUser();
		if (event.getAction().matches("slaps (.+?) around a bit with .*"))
			spam.out(event.getChannel(), user, user.getNick()
				+ ", no slapping! >:(");
	}
	
	@Override
	public synchronized void onPrivateMessage(
		PrivateMessageEvent<PircBotX> event) {
		if (!BotUtil.isLikc(event.getUser()))
			return;
		final String message = event.getMessage();
		final String target = message.length() > 3 ? message.substring(4) : "";
		try {
			if (message.startsWith("add "))
				spam.add(target);
			else if (message.startsWith("rem "))
				spam.rem(target);
			else if (message.equals("lst"))
				event.respond(spam.getList());
			else if (message.startsWith("raw "))
				rawLine(event.getBot(), target);
			else if (message.equals("quit"))
				quit(event.getBot());
		} catch (final IOException e) {
			event.respond("Error!");
			LOG.error("Privmsg Error: User: " + event.getUser().getNick()
				+ " Message: " + message, e);
		}
	}
	
	@Override
	public void onMessage(MessageEvent<PircBotX> event) {
		final Channel chan = event.getChannel();
		final User user = event.getUser();
		final String message = event.getMessage();
		try {
			if (spam.isIgnored(user))
				return;
			else if (message.equalsIgnoreCase("@quit") && BotUtil.isLikc(user))
				quit(event.getBot());
			else if (message.equalsIgnoreCase("@help"))
				spam.out(chan, user, HELP_MSG);
			else if (message.equalsIgnoreCase("@info"))
				spam.out(chan, user, info.getInfo());
			else if (message.equalsIgnoreCase("@servers"))
				spam.out(chan, user, servers.getServers());
			else if (message.toLowerCase().startsWith("@link "))
				if (BotUtil.isLikc(user))
					spam.out(chan, user, link.getLinksLikc(message));
				else if (chan.isOp(user) || chan.isSuperOp(user)
					|| chan.isOwner(user))
					spam.out(chan, user, link.getLinksOp(message));
				else
					spam.out(chan, user, link.getLinks(message));
			else if (message.startsWith("~") || message.startsWith("?"))
				spam.out(chan, user, link.link(message));
		} catch (final IOException e) {
			event.respond("Error! Ping likcoras!");
			LOG.error("Message Error: Chan: " + chan.getName() + " User: "
				+ user.getNick() + " Message: " + message, e);
		}
	}
	
	private Configuration<PircBotX> configureBot(BotConfig config) {
		final BotConfig.Server server = config.getServer();
		final BotConfig.Bot bot = config.getBot();
		final Configuration.Builder<PircBotX> builder =
			new Configuration.Builder<PircBotX>()
				.addListener(this)
				.setAutoReconnect(true)
				.setAutoSplitMessage(true)
				.setMessageDelay(3000L)
				
				.setName(bot.getNick())
				.setLogin(bot.getLogin())
				.setRealName(bot.getRealname())
				.setVersion(bot.getVersion())
				.setNickservPassword(bot.getPassword())
				
				.setServerHostname(server.getHost())
				.setServerPort(server.getPort())
				.setSocketFactory(
					server.isSsl() ? server.isTrust()
						? new UtilSSLSocketFactory().trustAllCertificates()
						: SSLSocketFactory.getDefault() : SocketFactory
						.getDefault());
		
		for (final String chan : config.getChannels()) {
			final List<String> chanKey =
				Splitter.on(":").trimResults().omitEmptyStrings().limit(2)
					.splitToList(chan);
			if (chanKey.size() < 2)
				builder.addAutoJoinChannel(chan);
			else
				builder.addAutoJoinChannel(chanKey.get(0), chanKey.get(1));
		}
		return builder.buildConfiguration();
	}
	
	private void rawLine(PircBotX bot, String target) {
		LOG.info("Raw " + target);
		bot.sendRaw().rawLineNow(target, true);
	}
	
	private void quit(PircBotX bot) {
		bot.stopBotReconnect();
		bot.sendIRC().quitServer();
		LOG.info("Quitted");
	}
	
}
