package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class QuitHook extends ListenerAdapter<AgarBot> {
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event) {
        AgarBot bot = event.getBot();
        if (Utils.isTrigger(event.getMessage(), "quit")
                && bot.getAuth().checkLevel(event.getUser(), AuthLevel.ADMIN)) {
            bot.stopBotReconnect();
            bot.sendIRC().quitServer();
        }
    }
}
