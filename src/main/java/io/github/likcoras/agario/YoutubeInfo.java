package io.github.likcoras.agario;

public class YoutubeInfo {
	
	private Items[] items;
	
	public boolean hasEntries() {
		return items.length > 0;
	}
	
	public String getDuration() {
		return items[0].contentDetails.duration.substring(2).toLowerCase()
			.replaceAll("[a-z]", "$0 ").trim();
	}
	
	public long getViews() {
		return items[0].statistics.viewCount;
	}
	
	public long getLikes() {
		return items[0].statistics.likeCount;
	}
	
	public long getDislikes() {
		return items[0].statistics.dislikeCount;
	}
	
	public String getTitle() {
		return items[0].snippet.title;
	}
	
	public String getChannel() {
		return items[0].snippet.channelTitle;
	}
	
	private static class Items {
		private ContentDetails contentDetails;
		private Statistics statistics;
		private Snippet snippet;
	}
	
	private static class ContentDetails {
		private String duration;
	}
	
	private static class Statistics {
		private long viewCount;
		private long likeCount;
		private long dislikeCount;
	}
	
	private static class Snippet {
		private String title;
		private String channelTitle;
	}
	
}
