package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
public class LinkHook extends ListenerAdapter<AgarBot> {
    private static final Path LINK_FILE = Paths.get("links.txt");
    private static final String ADDED = Utils.addFormat("&03Link added: &r");
    private static final String REMOVED =
            Utils.addFormat("&03Link removed: &r");
    private static final Pattern PATTERN = Pattern.compile("~(\\S+)");
    
    private final Properties links = new Properties();
    
    public LinkHook() {
        readLinks();
    }
    
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event) {
        if (Utils.isTrigger(event.getMessage(), "link ")) {
            handleTrigger(event);
        } else {
            handleLink(event);
        }
    }
    
    private void handleTrigger(GenericMessageEvent<AgarBot> event) {
        List<String> args = Utils.getArgs(event.getMessage(), 4);
        if (args.size() < 2) {
            return;
        } else if (args.get(1).equalsIgnoreCase("list")) {
            listLink(event);
        } else if (!event.getBot().getAuth().checkLevel(event.getUser(),
                AuthLevel.MOD)) {
            return;
        } else if (args.get(1).equalsIgnoreCase("add")) {
            addLink(event, args);
        } else if (args.get(1).equalsIgnoreCase("rem")) {
            removeLink(event, args);
        }
    }
    
    private void listLink(GenericMessageEvent<AgarBot> event) {
        String list = links.stringPropertyNames().stream()
                .map(link -> "~" + link).sorted()
                .collect(Collectors.joining(", ", "Links: ", ""));
        event.getUser().send().message(list);
    }
    
    private void addLink(GenericMessageEvent<AgarBot> event,
            List<String> args) {
        if (args.size() < 4) {
            return;
        }
        String name =
                Colors.removeFormattingAndColors(args.get(2)).toLowerCase();
        String target = args.get(3);
        links.setProperty(name, target);
        event.getUser().send().message(ADDED + name);
        writeLinks();
    }
    
    private void removeLink(GenericMessageEvent<AgarBot> event,
            List<String> args) {
        if (args.size() < 3) {
            return;
        }
        String name = args.get(2).toLowerCase();
        String removed = (String) links.remove(name);
        if (removed != null) {
            event.getUser().send().message(REMOVED + removed);
            writeLinks();
        }
    }
    
    private void handleLink(GenericMessageEvent<AgarBot> event) {
        Matcher matcher = PATTERN.matcher(event.getMessage());
        if (!matcher.find()) {
            return;
        }
        String link = matcher.group(1).toLowerCase();
        if (links.containsKey(link)
                && event.getBot().getSpam().check(event.getUser())) {
            Utils.reply(event, links.getProperty(link));
        }
    }
    
    private void readLinks() {
        try {
            if (Files.notExists(LINK_FILE)) {
                Files.createFile(LINK_FILE);
            }
            @Cleanup BufferedReader reader = Files.newBufferedReader(LINK_FILE);
            links.load(reader);
        } catch (IOException e) {
            log.error("Error while reading links", e);
        }
    }
    
    private void writeLinks() {
        try {
            @Cleanup BufferedWriter writer = Files.newBufferedWriter(LINK_FILE);
            links.store(writer, "Link file");
        } catch (IOException e) {
            log.error("Error while writing links", e);
        }
    }
}
