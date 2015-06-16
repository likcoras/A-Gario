package io.github.likcoras.agario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

public class TeamHandler implements Handler {
	
	private static final Logger LOG = Logger.getLogger(TeamHandler.class);
	
	private static final String TEAM_FORMAT = BotUtil
		.addColors("%cIP:port: ws://%n%s | %cLeaderboard:%s");
	private static final String LEAD_FORMAT = BotUtil.addColors(" %n%s %c|");
	
	private URL mAgar;
	
	@Override
	public void configure(BotConfig config) throws HandlerException {
		try {
			mAgar = new URL("http://m.agar.io");
		} catch (final MalformedURLException e) {
			throw new HandlerException(e);
		}
	}
	
	@Override
	public boolean handlesEvent(Event<PircBotX> event) {
		return false;
	}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public boolean isHandlerOf(Channel chan, User user, String message) {
		return message.toLowerCase().startsWith("@team");
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message)
		throws HandlerException {
		LOG.info("Team requested");
		final String input = message.substring(5).toLowerCase().trim();
		String region = getRegion(input);
		if (region.isEmpty())
			return "";
		try {
			return getInfo(getServer(region));
		} catch (IOException | URISyntaxException | InterruptedException e) {
			throw new HandlerException(e);
		}
	}
	
	private String getRegion(String input) {
		input = input.toLowerCase();
		if (input.isEmpty() || input.equals("use") || input.equals("us east"))
			return "US-Atlanta";
		else if (input.equals("usw") || input.equals("us west"))
			return "US-Fremont";
		else if (input.equals("eu") || input.equals("europe"))
			return "EU-London";
		else if (input.equals("jp") || input.equals("east asia"))
			return "JP-Tokyo";
		else if (input.equals("sg") || input.equals("oceania"))
			return "SG-Singapore";
		else if (input.equals("ru") || input.equals("russia"))
			return "RU-Russia";
		else if (input.equals("cn") || input.equals("china"))
			return "CN-China";
		else if (input.equals("br") || input.equals("south america"))
			return "BR-Brazil";
		else if (input.equals("tk") || input.equals("turkey"))
			return "TK-Turkey";
		else
			return "";
	}
	
	private ServerConnection getServer(String region) throws IOException, URISyntaxException, InterruptedException {
		final HttpURLConnection conn =
			(HttpURLConnection) mAgar.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.getOutputStream().write(region.getBytes(StandardCharsets.UTF_8));
		final BufferedReader read =
			new BufferedReader(new InputStreamReader(conn.getInputStream()));
		final String ip = read.readLine();
		read.close();
		conn.disconnect();
		return new ServerConnection(ip);
	}
	
	private String getInfo(ServerConnection server) throws InterruptedException {
		final List<String> leaderboard = server.getLeaderboard();
		final StringBuffer lead = new StringBuffer();
		for (final String leader : leaderboard)
			lead.append(String.format(LEAD_FORMAT, leader.isEmpty()
				? "An unnamed cell" : leader));
		return String.format(TEAM_FORMAT, server.getIP(),
			lead.substring(0, lead.length() - 1));
	}
	
}
