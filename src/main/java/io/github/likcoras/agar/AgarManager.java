package io.github.likcoras.agar;

import io.github.likcoras.agar.auth.AuthLevel;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.managers.ThreadedListenerManager;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AgarManager extends ThreadedListenerManager<AgarBot> {
    private static final int STRIKES = 5;
    private static final int TIME = 5;
    
    private final LoadingCache<String, Integer> spam =
            CacheBuilder.newBuilder().expireAfterAccess(TIME, TimeUnit.SECONDS)
                    .build(new CacheLoader<String, Integer>() {
                        @Override
                        public Integer load(String key) {
                            return 0;
                        }
                    });
                    
    @Override
    public void dispatchEvent(Event<AgarBot> event) {
        if (!checkSpam(event)) {
            super.dispatchEvent(event);
        }
    }
    
    @SneakyThrows(ExecutionException.class) // See below
    private boolean checkSpam(Event<AgarBot> event) {
        if (!(event instanceof GenericMessageEvent)) {
            return false;
        }
        @SuppressWarnings("unchecked") GenericMessageEvent<AgarBot> message =
                (GenericMessageEvent<AgarBot>) event;
        User user = message.getUser();
        if (event.getBot().getAuth().checkLevel(user, AuthLevel.MOD)) {
            return false;
        }
        String host = user.getHostmask();
        int strikes = spam.get(host) + 1; // Just returns 0 + 1
        if (strikes > STRIKES) {
            return true;
        }
        spam.put(host, strikes);
        return false;
    }
}
