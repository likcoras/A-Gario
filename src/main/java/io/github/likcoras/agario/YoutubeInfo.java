package io.github.likcoras.agario;

import java.lang.reflect.Type;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

@Value
public class YoutubeInfo {
	
	@Getter(AccessLevel.NONE)
	private boolean found;
	private String title;
	private String channel;
	private String duration;
	private long views;
	private long likes;
	private long dislikes;
	
	public static class YoutubeInfoDeserializer implements JsonDeserializer<YoutubeInfo> {
		
		@Override
		public YoutubeInfo deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
			final JsonArray items =
				json.getAsJsonObject().get("items").getAsJsonArray();
			if (items.size() < 0)
				return new YoutubeInfo(false, "", "", "", 0, 0, 0);
			final JsonObject item = items.get(0).getAsJsonObject();
			final JsonObject snippet = item.get("snippet").getAsJsonObject();
			final JsonObject contentDetails =
				item.get("contentDetails").getAsJsonObject();
			final JsonObject statistics =
				item.get("statistics").getAsJsonObject();
			final String title = snippet.get("title").getAsString();
			final String channel = snippet.get("channelTitle").getAsString();
			final String duration =
				contentDetails.get("duration").getAsString().substring(2)
					.toLowerCase().replaceAll("[a-z]", "$0 ");
			final long views = statistics.get("viewCount").getAsLong();
			final long likes = statistics.get("likeCount").getAsLong();
			final long dislikes = statistics.get("dislikeCount").getAsLong();
			return new YoutubeInfo(true, title, channel, duration, views,
				likes, dislikes);
		}
		
	}
	
	public boolean wasFound() {
		return found;
	}
	
}
