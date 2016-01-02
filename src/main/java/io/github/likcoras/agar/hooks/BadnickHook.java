package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import lombok.extern.log4j.Log4j2;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.hooks.types.GenericUserModeEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Log4j2
public class BadnickHook extends ListenerAdapter<AgarBot> {
    private static final Path BADNICK_FILE = Paths.get("badnicks.txt");
    private static final String ADDED = "Nick added: ";
    private static final String REMOVED = "Nick removed: ";
    private static final String LIST = "Nicks: ";
    
    private final List<Pattern> badnicks = new CopyOnWriteArrayList<Pattern>();
    
    public BadnickHook() {
        readBadnicks();
    }
    
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event)
            throws IOException {
        if (Utils.isTrigger(event.getMessage(), "badnick ") && event.getBot()
                .getAuth().checkLevel(event.getUser(), AuthLevel.ADMIN)) {
            handleTrigger(event);
        }
    }
    
    @Override
    public void onGenericUserMode(GenericUserModeEvent<AgarBot> event) {
        AgarBot bot = event.getBot();
        Channel channel = event.getChannel();
        if (event.getRecipient().equals(bot.getUserBot()) && Utils.checkBot(bot, channel)) {
            channel.getUsers().forEach(user -> handleNick(bot, user, channel));
        }
    }
    
    @Override
    public void onJoin(JoinEvent<AgarBot> event) {
        handleNick(event.getBot(), event.getUser(), event.getChannel());
    }
    
    @Override
    public void onNickChange(NickChangeEvent<AgarBot> event) {
        User user = event.getUser();
        user.getChannels().forEach(channel -> handleNick(event.getBot(), user, channel));
    }
    
    private void handleTrigger(GenericMessageEvent<AgarBot> event) {
        List<String> args = Utils.getArgs(event.getMessage(), 3);
        if (args.size() < 2) {
            return;
        } else if (args.get(1).equalsIgnoreCase("list")) {
            listNick(event);
        } else if (args.get(1).equalsIgnoreCase("add")) {
            addNick(event, args);
        } else if (args.get(1).equalsIgnoreCase("rem")) {
            removeNick(event, args);
        }
    }
    
    private void listNick(GenericMessageEvent<AgarBot> event) {
        event.getUser().send().message(LIST + event.getBot().getConfig().getNickLink());
    }
    
    private void addNick(GenericMessageEvent<AgarBot> event, List<String> args) {
        String regex = args.get(2);
        Pattern pattern = getPattern(regex);
        if (pattern == null) {
            return;
        }
        badnicks.add(pattern);
        event.getUser().send().message(ADDED + regex);
        writeBadnicks();
    }
    
    private void removeNick(GenericMessageEvent<AgarBot> event,
            List<String> args) {
        String name = args.get(2);
        List<Pattern> selected = badnicks.stream()
                .filter(nick -> nick.pattern().equals(name))
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            return;
        }
        selected.forEach(badnicks::remove);
        event.getUser().send().message(REMOVED + name);
        writeBadnicks();
    }
    
    private void handleNick(AgarBot bot, User user, Channel channel) {
        if (user.equals(user.getBot().getUserBot()) || bot.getAuth().checkLevel(user, AuthLevel.BYPASS) || !Utils.checkBot(bot, channel)) {
            return;
        }
        if (!badnicks.stream().anyMatch(p -> p.matcher(user.getNick()).find())) {
            return;
        }
        channel.send().ban(user.getHostmask());
        channel.send().kick(user, "Unacceptable name!");
    }
    
    private Pattern getPattern(String regex) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            log.error("Error while compiling regex " + regex);
            return null;
        }
    }
    
    private void readBadnicks() {
        try {
            if (Files.notExists(BADNICK_FILE)) {
                Files.createFile(BADNICK_FILE);
            }
            Files.lines(BADNICK_FILE).forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Error while reading badnicks", e);
        }
    }
    
    private void parseLine(String line) {
        if (line.length() < 1) {
            return;
        }
        Pattern regex = getPattern(line);
        if (regex != null) {
            badnicks.add(regex);
        }
    }
    
    private void writeBadnicks() {
        try {
            List<String> lines =
                    badnicks.stream().map(p -> p.toString())
                    .collect(Collectors.toList());
            Files.write(BADNICK_FILE, lines);
        } catch (IOException e) {
            log.error("Error while writing badnicks", e);
        }
    }
}
