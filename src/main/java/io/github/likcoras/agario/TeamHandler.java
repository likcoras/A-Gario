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
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;

public class TeamHandler implements Handler {
	
	private static final Logger LOG = Logger.getLogger(TeamHandler.class);
	
	private static final String TEAM_FORMAT = BotUtil
		.addColors("%cIP:port: %n%s | %cLeaderboard:%s");
	private static final String LEAD_FORMAT = BotUtil.addColors(" %n%s %c|");
	
	private URL mAgar;
	private ServerConnection UsAtlanta;
	private ServerConnection UsFremont;
	private ServerConnection EuLondon;
	
	@Override
	public void configure(BotConfig config) throws HandlerException {
		try {
			mAgar = new URL("http://m.agar.io");
		} catch (final MalformedURLException e) {
			throw new HandlerException(e);
		}
		new Timer(true).schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if (UsAtlanta != null)
						UsAtlanta.close();
					UsAtlanta = new ServerConnection(getServer("US-Atlanta"));
					if (UsFremont != null)
						UsFremont.close();
					UsFremont = new ServerConnection(getServer("US-Fremont"));
					if (EuLondon != null)
						EuLondon.close();
					EuLondon = new ServerConnection(getServer("EU-London"));
				} catch (IOException | URISyntaxException
					| InterruptedException e) {
					LOG.error("Error:", e);
				}
			}
		}, 0L, 300000L);
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
		final String region = message.substring(5);
		if (region.isEmpty() || region.equalsIgnoreCase(" east"))
			return getInfo(UsAtlanta);
		else if (region.equalsIgnoreCase(" west"))
			return getInfo(UsFremont);
		else if (region.equalsIgnoreCase(" europe"))
			return getInfo(EuLondon);
		return "";
	}
	
	private String getServer(String region) throws IOException {
		final HttpURLConnection conn =
			(HttpURLConnection) mAgar.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.getOutputStream().write(region.getBytes(StandardCharsets.UTF_8));
		final BufferedReader read =
			new BufferedReader(new InputStreamReader(conn.getInputStream()));
		final String server = read.readLine();
		read.close();
		conn.disconnect();
		return server;
	}
	
	private String getInfo(ServerConnection server) {
		final List<String> leaderboard = server.getLeaderboard();
		final StringBuffer lead = new StringBuffer();
		for (final String leader : leaderboard)
			lead.append(String.format(LEAD_FORMAT, leader.isEmpty()
				? "An unnamed cell" : leader));
		return String.format(TEAM_FORMAT, server.getIP(),
			lead.substring(0, lead.length() - 1));
	}
	
}
