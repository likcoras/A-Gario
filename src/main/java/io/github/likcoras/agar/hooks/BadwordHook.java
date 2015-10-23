package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import com.google.common.base.Splitter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelUserEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@Log4j2
public class BadwordHook extends ListenerAdapter<AgarBot> {
    private static final Path BADWORD_FILE = Paths.get("badwords.txt");
    private static final String WARNING = "You're not allowed to say that:";
    private static final String ADDED = Utils.addFormat("&03Word added: &r");
    private static final String REMOVED =
            Utils.addFormat("&03Word removed: &r");
            
    private final Cache<String, Integer> strikes = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES).build();;
    private final Map<Pattern, Integer> badwords = new ConcurrentHashMap<>();
    private final String list = Utils.addFormat("&03Words: &r");
    
    public BadwordHook() {
        readBadwords();
    }
    
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event)
            throws IOException {
        if (Utils.isTrigger(event.getMessage(), "badword ") && event.getBot()
                .getAuth().checkLevel(event.getUser(), AuthLevel.ADMIN)) {
            handleTrigger(event);
        }
    }
    
    @Override
    public void onMessage(MessageEvent<AgarBot> event) {
        handleBadword(event, event);
    }
    
    @Override
    public void onAction(ActionEvent<AgarBot> event) {
        handleBadword(event, event);
    }
    
    private void handleTrigger(GenericMessageEvent<AgarBot> event)
            throws IOException {
        List<String> args = Splitter.on(" ").limit(4).trimResults()
                .splitToList(event.getMessage());
        if (args.size() < 2) {
            return;
        } else if (args.get(1).equalsIgnoreCase("list")) {
            listWord(event);
        } else if (args.get(1).equalsIgnoreCase("add")) {
            addWord(event, args);
        } else if (args.get(1).equalsIgnoreCase("rem")) {
            removeWord(event, args);
        }
    }
    
    private void listWord(GenericMessageEvent<AgarBot> event)
            throws IOException {
        event.getUser().send()
                .message(list + event.getBot().getConfig().getListLink());
    }
    
    private void addWord(GenericMessageEvent<AgarBot> event,
            List<String> args) {
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
        badwords.put(pattern, level);
        event.getUser().send().message(ADDED + regex);
        writeBadwords();
    }
    
    private int getLevel(List<String> args) {
        try {
            int i = Integer.parseInt(args.get(2));
            return i < 1 || i > 3 ? -1 : i;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void removeWord(GenericMessageEvent<AgarBot> event,
            List<String> args) {
        if (args.size() < 3) {
            return;
        }
        String name = args.get(2) + (args.size() > 3 ? " " + args.get(3) : "");
        List<Pattern> selected = badwords.keySet().stream()
                .filter(word -> word.pattern().equals(name))
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            return;
        }
        selected.forEach(badwords::remove);
        event.getUser().send().message(REMOVED + name);
        writeBadwords();
    }
    
    private void handleBadword(GenericChannelUserEvent<AgarBot> event,
            GenericMessageEvent<AgarBot> message) {
        AgarBot bot = event.getBot();
        User user = event.getUser();
        if (!Utils.checkBot(bot, event.getChannel()) || bot.getAuth().checkLevel(user, AuthLevel.MOD)) {
            return;
        }
        handleBadword(user, event.getChannel(), message.getMessage());
    }
    
    private void handleBadword(User user, Channel channel, String message) {
        Matcher matcher = getMatch(Colors.removeFormattingAndColors(message));
        if (matcher == null) {
            return;
        }
        int strikeValue = updateStrikes(user, badwords.get(matcher.pattern()));
        punishBadword(user, channel, strikeValue, matcher);
    }
    
    private Matcher getMatch(String message) {
        Matcher selected = null;
        int level = -1;
        for (Entry<Pattern, Integer> entry : badwords.entrySet()) {
            int currentLevel = entry.getValue();
            if (currentLevel <= level) {
                continue;
            }
            Matcher matcher = entry.getKey().matcher(message);
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
    
    @SneakyThrows(ExecutionException.class)
    private int updateStrikes(User user, int added) {
        String hostmask = user.getHostmask();
        int strikeValue = strikes.get(hostmask, () -> 0) + added;
        if (strikeValue > 3) {
            strikeValue = 3;
        }
        strikes.put(hostmask, strikeValue);
        return strikeValue;
    }
    
    private void punishBadword(User user, Channel channel, int strikeValue,
            Matcher match) {
        String capture = match.group();
        String warning =
                WARNING + capture.substring(0, Math.min(capture.length(), 20));
        switch (strikeValue) {
            case 3:
                channel.send().ban(user.getHostmask());
            case 2:
                channel.send().kick(user, warning);
            case 1:
                user.send().notice(warning);
            default:
        }
    }
    
    private Pattern getPattern(String regex) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            log.error("Error while compiling regex " + regex);
            return null;
        }
    }
    
    private void readBadwords() {
        try {
            if (Files.notExists(BADWORD_FILE)) {
                Files.createFile(BADWORD_FILE);
            }
            Files.lines(BADWORD_FILE).forEach(this::parseLine);
        } catch (IOException e) {
            log.error("Error while reading badwords", e);
        }
    }
    
    private void parseLine(String line) {
        if (line.length() < 1) {
            return;
        }
        Pattern regex = getPattern(line.substring(1));
        if (regex != null) {
            badwords.put(regex, Character.getNumericValue(line.charAt(0)));
        }
    }
    
    private void writeBadwords() {
        try {
            List<String> lines =
                    badwords.entrySet().stream()
                            .map(entry -> entry.getValue().toString()
                                    + entry.getKey())
                    .collect(Collectors.toList());
            Files.write(BADWORD_FILE, lines);
        } catch (IOException e) {
            log.error("Error while writing badwords", e);
        }
    }
    
}
