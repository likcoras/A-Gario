package io.github.likcoras.agar;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AgarManager extends ThreadedListenerManager<AgarBot> {
    private static final int STRIKES = 5;
    private static final int TIME = 1;
    
    private final Cache<String, Integer> spam = CacheBuilder.newBuilder()
            .expireAfterWrite(TIME, TimeUnit.SECONDS).build();
            
    @Override
    public void dispatchEvent(Event<AgarBot> event) {
        if (!checkSpam(event)) {
            super.dispatchEvent(event);
        }
    }
    
    @SneakyThrows(ExecutionException.class)
    private boolean checkSpam(Event<AgarBot> event) {
        if (!(event instanceof GenericMessageEvent)) {
            return false;
        }
        @SuppressWarnings("rawtypes") GenericMessageEvent message =
                (GenericMessageEvent) event;
        User user = message.getUser();
        if (user.getNick().equalsIgnoreCase("nickserv")) {
            return false;
        }
        String host = user.getHostmask();
        int strikes = spam.get(host, () -> 0) + 1;
        spam.put(host, strikes);
        return strikes > STRIKES;
    }
}
