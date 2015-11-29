package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;
import io.github.likcoras.agar.auth.AuthLevel;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.UserLevel;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.OpEvent;
import org.pircbotx.hooks.events.OwnerEvent;
import org.pircbotx.hooks.events.SuperOpEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.hooks.types.GenericUserModeEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

public class BadnickHook extends ListenerAdapter<AgarBot> {
    private static final Path BADNICK_FILE = Paths.get("badnicks.txt");
    private static final String DEFAULT_MESSAGE = "This name is not allowed.";
    private static final String ADDED = "Nick added: ";
    private static final String REMOVED = "Nick removed: ";
    
    private final Map<String, String> badnicks = new ConcurrentHashMap<>();
    
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
        
    }
    
    private void handleNick(AgarBot bot, User user, Channel channel) {
        if (user.equals(user.getBot().getUserBot()) || ) {
            return;
        }
    }
    
    private void readBadnicks() {
        
    }
    
    private void writeBadnicks() {
        
    }
}
