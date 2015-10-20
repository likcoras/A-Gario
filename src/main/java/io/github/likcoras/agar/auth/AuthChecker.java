package io.github.likcoras.agar.auth;

import lombok.RequiredArgsConstructor;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.NoticeEvent;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class AuthChecker implements Callable<AuthLevel> {
    private final User user;
    private final AuthLevel level;
    
    @Override
    public AuthLevel call() throws Exception {
        user.getBot().sendIRC().message("NickServ", "STATUS " + user.getNick());
        WaitForQueue queue = new WaitForQueue(user.getBot());
        while (true) {
            @SuppressWarnings("unchecked")
            NoticeEvent<PircBotX> event = queue.waitFor(NoticeEvent.class);
            if (!isMessage(event)) {
                continue;
            }
            queue.close();
            return getLevel(event.getMessage());
        }
    }
    
    private boolean isMessage(NoticeEvent<PircBotX> event) {
        User nickserv = event.getUser();
        String message = event.getMessage().toLowerCase();
        return nickserv.getNick().equalsIgnoreCase("nickserv")
                && message.startsWith("status " + user.getNick().toLowerCase());
    }
    
    private AuthLevel getLevel(String message) {
        int status = Integer.parseInt(message.substring(message.length() - 1));
        return status > 1 ? level : AuthLevel.USER;
    }
}
