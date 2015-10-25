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
import org.pircbotx.User;
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
import java.util.Set;

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
    
    public static boolean isTrigger(String message, String trigger) {
        return message.toLowerCase().startsWith("@" + trigger);
    }
    
    public static boolean checkBot(AgarBot bot, Channel channel) {
        User user = bot.getUserBot();
        Set<UserLevel> levels = user.getUserLevels(channel);
        return !levels.isEmpty() && (levels.contains(UserLevel.HALFOP)
                || levels.contains(UserLevel.OP)
                || levels.contains(UserLevel.SUPEROP)
                || levels.contains(UserLevel.OWNER));
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
