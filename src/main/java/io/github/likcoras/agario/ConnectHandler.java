package io.github.likcoras.agario;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import com.google.gson.Gson;

@Log4j
public class ConnectHandler implements Handler {
	
	private static final Gson GSON = new Gson();
	private static final String QUERY =
			"http://connect.agariomods.com/json/api.php?action=GSFUC&username=";
	
	private static final String NO_USER = "There is no user by the name %s";
	private static final String NO_IMG = "User %s has not uploaded a skin yet";
	private static final String USER_IMG =
			BotUtil.addColors("User found | %cSkin: %nhttp://connect.agariomods.com/img_%s.png");
	
	@Override
	public void configure(BotConfig config) {}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {}
	
	@Override
	public String getResponse(Channel chan, User user, String message) throws HandlerException {
		if (!message.toLowerCase().startsWith("@con ") || message.length() == 5)
			return "";
		final String username = message.substring(5).toLowerCase();
		log.info("Connect info for " + username + " requested");
		try {
			return getConText(username, getConnectJson(username));
		} catch (IOException e) {
			throw new HandlerException(e);
		}
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private ConnectInfo getConnectJson(String username) throws IOException {
		final URLConnection con =
				new URL(QUERY + URLEncoder.encode(username, "UTF-8"))
						.openConnection();
		con.setRequestProperty("User-Agent", "");
		return GSON.fromJson(new InputStreamReader(con.getInputStream()),
				ConnectInfo.class);
	}
	
	@SneakyThrows(UnsupportedEncodingException.class)
	private String getConText(String username, ConnectInfo info) {
		if (!info.userExists())
			return String.format(NO_USER, username);
		else if (!info.hasImg())
			return String.format(NO_IMG, username);
		else
			return String.format(USER_IMG, URLEncoder.encode(username, "UTF-8"));
	}
	
}
