package io.github.likcoras.agar;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import lombok.Cleanup;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.UserLevel;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.List;
import java.util.SortedSet;

public class Utils {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationDeserializer())
            .create();
    private static final Splitter ARGS = Splitter.on(" ");
            
    public static String addFormat(String message) {
        return message.replaceAll("&b", "\u0002").replaceAll("&\u0002", "&b")
                .replaceAll("&r", "\u000f").replaceAll("&\u000f", "&r")
                .replaceAll("&s", "\u0016").replaceAll("&\u0016", "&s")
                .replaceAll("&i", "\u001d").replaceAll("&\u001d", "&i")
                .replaceAll("&u", "\u001f").replaceAll("&\u001f", "&u")
                .replaceAll("&(\\d\\d)", "\u0003$1").replaceAll("&\u0003", "&");
    }
    
    public static String stripFormat(String message) {
        String stripped = Colors.removeFormattingAndColors(message);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < stripped.length(); i++) {
            char c = stripped.charAt(i);
            if (c != '\u001d') {
                buffer.append(c);
            }
        }
        return buffer.toString();
    }
    
    public static boolean isTrigger(String message, String trigger) {
        return message.toLowerCase().startsWith("@" + trigger);
    }
    
    public static boolean checkBot(AgarBot bot, Channel channel) {
        SortedSet<UserLevel> levels = bot.getUserBot().getUserLevels(channel);
        return !levels.isEmpty() && levels.last().compareTo(UserLevel.OP) >= 0;
    }
    
    public static List<String> getArgs(String message, int limit) {
        return ARGS.limit(limit).splitToList(message);
    }
    
    public static void reply(GenericMessageEvent<AgarBot> event,
            String message) {
        if (event instanceof MessageEvent) {
            ((MessageEvent<AgarBot>) event).getChannel().send()
                    .message(message);
        } else {
            event.getUser().send().message(message);
        }
    }
    
    public static <T> T fromJson(String url, Class<T> type) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", null);
        @Cleanup InputStreamReader reader =
                new InputStreamReader(conn.getInputStream());
        return GSON.fromJson(reader, type);
    }
    
    public static String formatDuration(Duration duration) {
        return String.format("%02d:%02d:%02d", duration.toHours(),
                duration.getSeconds() % 3600 / 60, duration.getSeconds() % 60);
    }
    
    private static class DurationDeserializer implements JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return Duration.parse(json.getAsString());
        }
    }
}
