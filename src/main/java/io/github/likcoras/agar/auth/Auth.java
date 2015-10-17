package io.github.likcoras.agar.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Cleanup;
import org.pircbotx.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Auth {
    private static final Path AUTH_FILE = Paths.get("auths");
    
    private final Map<String, AuthLevel> nicks;
    private final Cache<UUID, AuthLevel> auths;
    
    private final Gson gson;
    
    public Auth() throws IOException {
        auths = CacheBuilder.newBuilder()
                .expireAfterAccess(1L, TimeUnit.MINUTES).build();
        nicks = new ConcurrentHashMap<>();
        gson = new Gson();
        readNicks();
    }
    
    public void addNick(String nick, AuthLevel level) {
        nicks.put(nick, level);
        writeNicks();
    }
    
    public void remNick(String nick) {
        AuthLevel level = nicks.remove(nick);
        if (level != null) {
            writeNicks();
        }
    }
    
    public Map<String, AuthLevel> listNicks() {
        return ImmutableMap.copyOf(nicks);
    }
    
    public AuthLevel getLevel(User user) {
        UUID uid = user.getUserId();
        if (!nicks.containsKey(uid)) {
            return AuthLevel.USER;
        }
        try {
            return auths.get(uid, new AuthChecker(user, nicks.get(uid)));
        } catch (ExecutionException e) {
            return AuthLevel.USER;
        }
    }
    
    private void readNicks() {
        try {
            @Cleanup BufferedReader reader = Files.newBufferedReader(AUTH_FILE);
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> rawMap = gson.fromJson(reader, type);
            rawMap.forEach(
                    (key, value) -> nicks.put(key, AuthLevel.valueOf(value)));
        } catch (IOException e) {
            // TODO log
        }
    }
    
    private void writeNicks() {
        try {
            @Cleanup BufferedWriter writer = Files.newBufferedWriter(AUTH_FILE);
            gson.toJson(nicks, writer);
        } catch (IOException e) {
            // TODO log
        }
    }
}
