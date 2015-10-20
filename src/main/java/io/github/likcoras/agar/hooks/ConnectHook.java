package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;

public class ConnectHook extends ListenerAdapter<AgarBot> {
    private static final String API_DATA =
            "http://connect.agariomods.com/json/api.php?action=GSFUC&username=";
    private static final String NO_USER =
            Utils.addFormat("&04No user named &r");
    private static final String NO_IMG =
            Utils.addFormat("&04No image uploaded by &r");
    private static final String INFO = Utils.addFormat(
            "&03User: &r%s | &03Skin: &rhttps://connect.agariomods.com/img_%s.png");
            
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event)
            throws IOException {
        AgarBot bot = event.getBot();
        String message = event.getMessage();
        if (!Utils.isTrigger(message, "@con ")
                || !bot.getSpam().check(event.getUser())) {
            return;
        }
        String username = message.substring(5).toLowerCase();
        ConnectInfo info = getInfo(username);
        if (info.username == null) {
            event.respond(NO_USER + username);
        } else if (!info.hasImg) {
            event.respond(NO_IMG + username);
        } else {
            event.respond(String.format(INFO, username, username));
        }
    }
    
    private ConnectInfo getInfo(String username) throws IOException {
        return Utils.fromJson(API_DATA + username, ConnectInfo.class);
    }
    
    @Value
    private class ConnectInfo {
        String username;
        @SerializedName(value = "has_img") boolean hasImg;
    }
}
