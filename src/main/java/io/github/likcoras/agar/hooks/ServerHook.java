package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.util.Map;

public class ServerHook extends ListenerAdapter<AgarBot> {
    private static final String LIST = Utils.addFormat("&03%s: &r%d");
    private static final String LIST_SEPARATOR = " | ";
    private static final String API_DATA = "http://m.agar.io/info";
    
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event)
            throws IOException {
        if (!Utils.isTrigger(event.getMessage(), "servers")
                || !event.getBot().getSpam().check(event.getUser())) {
            return;
        }
        ConnectInfo info = getInfo();
        StringBuilder builder = new StringBuilder();
        info.getRegions()
                .forEach(
                        (name, server) -> builder
                                .append(String.format(LIST, name,
                                        server.players))
                        .append(LIST_SEPARATOR));
        builder.append(String.format(LIST, "Total", info.getTotals().players));
        Utils.reply(event, builder.toString());
    }
    
    private ConnectInfo getInfo() throws IOException {
        return Utils.fromJson(String.format(API_DATA), ConnectInfo.class);
    }
    
    @Data
    private class ConnectInfo {
        Map<String, ServerInfo> regions;
        ServerInfo totals;
    }
    
    @Data
    private class ServerInfo {
        @SerializedName(value = "numPlayers") int players;
    }
}
