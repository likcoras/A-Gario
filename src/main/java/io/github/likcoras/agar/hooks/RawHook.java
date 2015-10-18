package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import lombok.extern.log4j.Log4j2;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

@Log4j2
public class RawHook extends ListenerAdapter<AgarBot> {
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event) {
        AgarBot bot = event.getBot();
        String message = event.getMessage();
        User user = event.getUser();
        if (!bot.getAuth().checkLevel(user, AuthLevel.ADMIN)) {
            return;
        }
        if (Utils.isTrigger(message, "raw ")) {
            String raw = message.substring(5);
            event.getBot().sendRaw().rawLineNow(raw);
            log.warn("Raw: " + user.getNick() + "@" + user.getHostmask() + ": "
                    + raw);
        }
    }
}
