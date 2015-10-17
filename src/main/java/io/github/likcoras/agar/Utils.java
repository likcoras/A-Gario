package io.github.likcoras.agar;

import com.google.gson.Gson;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Utils {
    public static final Gson GSON = new Gson();
    
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
    
    public static void reply(GenericMessageEvent<AgarBot> event, String message) {
        if (event instanceof MessageEvent) {
            ((MessageEvent<AgarBot>) event).getChannel().send()
                    .message(message);
        } else {
            event.getUser().send().message(message);
        }
    }
    
    public static <T> T fromJson(URL url, Class<T> type) throws IOException {
        return GSON.fromJson(new InputStreamReader(url.openStream()), type);
    }
}
