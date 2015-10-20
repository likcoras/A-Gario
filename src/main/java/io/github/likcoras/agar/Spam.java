package io.github.likcoras.agar;

import io.github.likcoras.agar.auth.AuthLevel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.pircbotx.User;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Spam {
    private final Cache<UUID, Boolean> spam = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.SECONDS).build();
            
    public boolean check(User user) {
        if (checkBypass(user)) {
            return true;
        }
        UUID uid = user.getUserId();
        if (spam.getIfPresent(uid) != null) {
            return false;
        }
        spam.put(uid, true);
        return true;
    }
    
    private boolean checkBypass(User user) {
        AgarBot bot = (AgarBot) user.getBot();
        return bot.getAuth().checkLevel(user, AuthLevel.BYPASS);
    }
}
