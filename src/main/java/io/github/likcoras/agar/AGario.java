package io.github.likcoras.agar;

import com.google.common.io.Resources;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.pircbotx.exception.IrcException;

import java.io.IOException;
import java.io.InputStream;
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
            new AgarBot(config).startBot();
        } catch (IOException | IrcException e) {
            log.error("Error while starting bot", e);
            return;
        }
    }
    
    private Config getConfig() throws IOException {
        Path file = Paths.get("config.json");
        if (Files.notExists(file)) {
            createConfig(file);
        }
        return Utils.GSON.fromJson(Files.newBufferedReader(file), Config.class);
    }
    
    private void createConfig(Path file) throws IOException {
        @Cleanup InputStream stream =
                Resources.getResource("config.json").openStream();
        Files.copy(stream, file);
    }
}
