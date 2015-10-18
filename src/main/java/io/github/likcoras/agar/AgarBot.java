package io.github.likcoras.agar;

import io.github.likcoras.agar.auth.Auth;
import io.github.likcoras.agar.hooks.Hooks;

import lombok.Getter;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;

@Getter
public class AgarBot extends PircBotX {
    private final Config config;
    private final Auth auth;
    
    public AgarBot( Config config) {
        super(getBotConfig(config));
        this.config = config;
        auth = new Auth();
    }
    
    private static Configuration<AgarBot> getBotConfig(Config config) {
        Configuration.Builder<AgarBot> builder =
                new Configuration.Builder<AgarBot>().setAutoReconnect(true)
                        .setMaxLineLength(400).setMessageDelay(0L)
                        .setVersion("A`Gario by likcoras")
                        .setListenerManager(new AgarManager())
                        .setName(config.getNick()).setLogin(config.getUser())
                        .setRealName(config.getGecos())
                        .setServer(config.getHost(), config.getPort());
        if (!config.getPassword().isEmpty()) {
            builder.setNickservPassword(config.getPassword());
        }
        if (config.isSsl()) {
            builder.setSocketFactory(
                    new UtilSSLSocketFactory().trustAllCertificates());
        }
        config.getChannels().forEach(builder::addAutoJoinChannel);
        Hooks.LIST.forEach(builder::addListener);
        return builder.buildConfiguration();
    }
}
