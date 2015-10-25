package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.Auth;
import io.github.likcoras.agar.auth.AuthLevel;

import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;
import java.util.Map;

public class AuthHook extends ListenerAdapter<AgarBot> {
    private static final String LIST = "Auths:";
    private static final String LIST_END = "Auths end";
    private static final String SET = "Auth set: ";
    
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event) {
        if (Utils.isTrigger(event.getMessage(), "auth ") && event.getBot()
                .getAuth().checkLevel(event.getUser(), AuthLevel.OWNER)) {
            handleTrigger(event);
        }
    }
    
    private void handleTrigger(GenericMessageEvent<AgarBot> event) {
        List<String> args = Utils.getArgs(event.getMessage(), 4);
        if (args.size() < 2) {
            return;
        } else if (args.get(1).equalsIgnoreCase("list")) {
            listAuth(event);
        } else if (args.get(1).equalsIgnoreCase("set")) {
            setAuth(event, args);
        }
    }
    
    private void listAuth(GenericMessageEvent<AgarBot> event) {
        Map<String, AuthLevel> auths = event.getBot().getAuth().listNicks();
        User user = event.getUser();
        user.send().message(LIST);
        auths.forEach((auth, level) -> user.send().message(level + " " + auth));
        user.send().message(LIST_END);
    }
    
    private void setAuth(GenericMessageEvent<AgarBot> event,
            List<String> args) {
        if (args.size() < 4) {
            return;
        }
        String level = args.get(2);
        AuthLevel authLevel =
                level.equals(".") ? null : AuthLevel.valueOf(level);
        String user = args.get(3);
        Auth auth = event.getBot().getAuth();
        if (authLevel == null) {
            auth.remNick(user);
            event.getUser().send().message(SET + user + " removed");
        } else {
            event.getBot().getAuth().addNick(user, authLevel);
            event.getUser().send().message(SET + user + " " + authLevel);
        }
    }
}
