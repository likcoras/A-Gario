package io.github.likcoras.agar.auth;

import io.github.likcoras.agar.Utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
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

@Log4j2(topic = "errorlog")
public class Auth {
    private static final Path AUTH_FILE = Paths.get("auths.json");
    
    private final Map<String, AuthLevel> nicks = new ConcurrentHashMap<>();
    private final Cache<UUID, AuthLevel> auths = CacheBuilder.newBuilder()
            .expireAfterAccess(15L, TimeUnit.MINUTES).build();
            
    public Auth() {
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
    
    public boolean checkLevel(User user, AuthLevel level) {
        return getLevel(user).compareTo(level) >= 0;
    }
    
    public AuthLevel getLevel(User user) {
        UUID uid = user.getUserId();
        if (!nicks.containsKey(user.getNick())) {
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
            if (Files.notExists(AUTH_FILE)) {
                Files.createFile(AUTH_FILE);
                @Cleanup BufferedWriter writer =
                        Files.newBufferedWriter(AUTH_FILE);
                writer.write("{}");
            }
            @Cleanup BufferedReader reader = Files.newBufferedReader(AUTH_FILE);
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> rawMap = Utils.GSON.fromJson(reader, type);
            rawMap.forEach(
                    (key, value) -> nicks.put(key, AuthLevel.valueOf(value)));
        } catch (IOException e) {
            log.error("Error while reading nicks", e);
        }
    }
    
    private void writeNicks() {
        try {
            @Cleanup BufferedWriter writer = Files.newBufferedWriter(AUTH_FILE);
            Utils.GSON.toJson(nicks, writer);
        } catch (IOException e) {
            log.error("Error while writing nicks", e);
        }
    }
}
