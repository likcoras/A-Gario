package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

public class HelpHook extends ListenerAdapter<AgarBot> {
    private static final String HELP = Utils.addFormat(
            "&03Help: &r@auth, @badword, @con, @help, @link, @quit, @raw, @servers");
            
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event) {
        Utils.reply(event, HELP);
    }
}
