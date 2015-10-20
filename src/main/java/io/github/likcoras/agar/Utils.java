package io.github.likcoras.agar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import lombok.Cleanup;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.Duration;

public class Utils {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationDeserializer())
            .create();
            
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
        @Cleanup InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
        return GSON.fromJson(reader, type);
    }
    
    private static class DurationDeserializer implements JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return Duration.parse(json.getAsString());
        }
    }
}
