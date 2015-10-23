package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;

import com.google.common.collect.ImmutableList;
import org.pircbotx.hooks.Listener;

import java.util.List;

public class Hooks {
    public static final List<Listener<AgarBot>> LIST =
            ImmutableList.<Listener<AgarBot>> builder().add(new CoreHook())
                    .add(new QuitHook()).add(new RawHook()).add(new LinkHook())
                    .add(new BadwordHook()).add(new YoutubeHook())
                    .add(new ConnectHook()).add(new AuthHook())
                    .add(new ServerHook()).add(new HelpHook()).build();
}
