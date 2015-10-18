package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;

import lombok.extern.log4j.Log4j2;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.NickAlreadyInUseEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.SocketConnectEvent;

@Log4j2
public class CoreHook extends ListenerAdapter<AgarBot> {
    @Override
    public void onSocketConnect(SocketConnectEvent<AgarBot> event) {
        log.info("Connecting...");
    }
    
    @Override
    public void onConnect(ConnectEvent<AgarBot> event) {
        log.info("Connected.");
    }
    
    @Override
    public void onNickAlreadyInUse(NickAlreadyInUseEvent<AgarBot> event) {
        log.error("Nick already in use.");
        event.getBot().stopBotReconnect();
        event.getBot().sendIRC().quitServer();
    }
    
    @Override
    public void onDisconnect(DisconnectEvent<AgarBot> event) {
        log.info("Disconnected.");
    }
    
    @Override
    public void onNotice(NoticeEvent<AgarBot> event) {
        if (event.getUser().getNick().equals("NickServ")
                && event.getMessage().startsWith("Password accepted")) {
            AgarBot bot = event.getBot();
            bot.getConfig().getChannels().forEach(bot.sendIRC()::joinChannel);
        }
    }
}
