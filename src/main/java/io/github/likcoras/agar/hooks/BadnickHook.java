package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Log4j2
public class BadnickHook extends ListenerAdapter<AgarBot> {
    private static final Path BADNICK_FILE = Paths.get("badnicks.txt");
    private static final String DEFAULT_MSG = "Unacceptable name!";
    private static final String ADDED = "Nick added: ";
    private static final String REMOVED = "Nick removed: ";
    private static final String LIST = "Nicks: ";
    
    private final Cache<String, Boolean> strikes = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES).build();
    private final Map<Pattern, Data> badnicks = new ConcurrentHashMap<Pattern, Data>();
    
    private class Data {
        private String reason;
        private int severity;
        
        private Data(String reason, int severity) {
            this.reason = reason;
            this.severity = severity;
        }
    }
    
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
        List<String> args = Utils.getArgs(event.getMessage(), 5);
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
        if (args.size() < 4) {
            return;
        }
        int level = getLevel(args);
        if (level == -1) {
            return;
        }
        String regex = args.get(3);
        Pattern pattern = getPattern(regex);
        if (pattern == null) {
            return;
        }
        String reason = args.size() == 4 ? args.get(3) : DEFAULT_MSG;
        badnicks.put(pattern, new Data(reason, level));
        event.getUser().send().message(ADDED + regex);
        writeBadnicks();
    }
    
    private int getLevel(List<String> args) {
        try {
            int i = Integer.parseInt(args.get(2));
            return i < 2 || i > 3 ? -1 : i;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void removeNick(GenericMessageEvent<AgarBot> event,
            List<String> args) {
        if (args.size() < 3) {
            return;
        }
        String name = args.get(2);
        List<Pattern> selected = badnicks.keySet().stream()
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
        Matcher matcher = getMatch(user.getNick());
        if (matcher == null) {
            return;
        }
        boolean ban = true;
        if (strikes.getIfPresent(user.getHostmask()) == null) {
            strikes.put(user.getHostmask(), true);
            ban = false;
        }
        if (ban) {
            channel.send().ban(user.getHostmask());
        }
        channel.send().kick(user, badnicks.get(matcher.pattern()).reason);
    }
    
    private Pattern getPattern(String regex) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            log.error("Error while compiling regex " + regex);
            return null;
        }
    }
    
    private Matcher getMatch(String nick) {
        Matcher selected = null;
        int level = -1;
        for (Entry<Pattern, Data> entry : badnicks.entrySet()) {
            Data data = entry.getValue();
            int currentLevel = data.severity;
            if (currentLevel <= level) {
                continue;
            }
            Matcher matcher = entry.getKey().matcher(nick);
            if (!matcher.find()) {
                continue;
            }
            selected = matcher;
            level = currentLevel;
            if (level == 3) {
                break;
            }
        }
        return selected;
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
        String[] split = line.substring(1).split(" ", 2);
        Pattern regex = getPattern(split[0]);
        String reason = DEFAULT_MSG;
        if (split.length > 1) {
            reason = split[1];
        }
        if (regex != null) {
            badnicks.put(regex, new Data(reason, Character.getNumericValue(line.charAt(0))));
        }
    }
    
    private void writeBadnicks() {
        try {
            List<String> lines =
                    badnicks.entrySet().stream()
                    .map(entry -> entry.getValue().severity
                            + entry.getKey().toString() + " " + entry.getValue().reason)
                    .collect(Collectors.toList());
            Files.write(BADNICK_FILE, lines);
        } catch (IOException e) {
            log.error("Error while writing badnicks", e);
        }
    }
}
