package io.github.likcoras.agar.hooks;

import io.github.likcoras.agar.AgarBot;
import io.github.likcoras.agar.Utils;

import lombok.Value;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeHook extends ListenerAdapter<AgarBot> {
    private static final String API_DATA =
            "https://www.googleapis.com/youtube/v3/videos/?part=contentDetails,snippet,statistics&maxResults=1&id=%s&key=%s";
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)\\byoutu(?:\\.be|be\\.com)\\/(?:.*v(?:/|=)|(?:.*/)?)([\\w-]+)");
    private static final String INFO = Utils.addFormat(
            "&r[&04Youtube&r] %s by %s [%s] [%d views] &03[%d] &04[%d]");
            
    @Override
    public void onGenericMessage(GenericMessageEvent<AgarBot> event)
            throws IOException {
        Matcher match = LINK_PATTERN.matcher(event.getMessage());
        AgarBot bot = event.getBot();
        if (!match.find() || !bot.getSpam().check(event.getUser())) {
            return;
        }
        YoutubeInfo info =
                getInfo(match.group(1), bot.getConfig().getGoogleApi());
        if (info != null && !info.getItems().isEmpty()) {
            Utils.reply(event, formatInfo(info));
        }
    }
    
    private YoutubeInfo getInfo(String id, String key) throws IOException {
        return Utils.fromJson(String.format(API_DATA, id, key),
                YoutubeInfo.class);
    }
    
    private String formatInfo(YoutubeInfo info) {
        Item item = info.getItems().get(0);
        Snippet snippet = item.snippet;
        ContentDetails contentDetails = item.contentDetails;
        Statistics statistics = item.statistics;
        return String.format(INFO, snippet.title, snippet.channelTitle,
                Utils.formatDuration(contentDetails.duration),
                statistics.viewCount, statistics.likeCount,
                statistics.dislikeCount);
    }
    
    @Value
    private class YoutubeInfo {
        List<Item> items;
    }
    
    @Value
    private class Item {
        Snippet snippet;
        ContentDetails contentDetails;
        Statistics statistics;
    }
    
    @Value
    private class Snippet {
        String title;
        String channelTitle;
    }
    
    @Value
    private class ContentDetails {
        Duration duration;
    }
    
    @Value
    private class Statistics {
        long viewCount;
        long likeCount;
        long dislikeCount;
    }
}
