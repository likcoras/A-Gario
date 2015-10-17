package io.github.likcoras.agar;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2(topic = "errorlog")
public class AGario {
    public static void main(String[] args) {
        new AGario().start();
    }
    
    private void start() {
        Config config;
        try {
            config = getConfig();
        } catch (IOException e) {
            log.error("Error while reading config", e);
            return;
        }
        new AgarBot(config);
    }
    
    private Config getConfig() throws IOException {
        Path file = Paths.get("config");
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
        return Utils.GSON.fromJson(Files.newBufferedReader(Paths.get("config")),
                Config.class);
    }
}
