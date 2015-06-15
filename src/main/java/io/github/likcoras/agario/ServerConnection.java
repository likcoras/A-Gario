package io.github.likcoras.agario;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.UnsignedBytes;

public class ServerConnection extends WebSocketClient {
	
	private static final Logger LOG = Logger.getLogger(ServerConnection.class);
	
	private static final Map<String, String> HEADERS =
		new ImmutableMap.Builder<String, String>().put("Origin",
			"http://agar.io").build();
	
	private final String ip;
	private final List<String> leaderboard;
	
	public ServerConnection(String ip) throws URISyntaxException,
		InterruptedException {
		super(new URI("ws://" + ip), new Draft_17(), HEADERS, 0);
		this.ip = ip;
		leaderboard = new ArrayList<String>();
		connect();
	}
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		this.send(ByteBuffer.allocate(5).put(UnsignedBytes.MAX_POWER_OF_TWO)
			.putInt(4).array());
		this.send(ByteBuffer.allocate(5).put(UnsignedBytes.MAX_VALUE)
			.putInt(673720361).array());
	}
	
	@Override
	public void onMessage(String message) {}
	
	@Override
	public synchronized void onMessage(ByteBuffer bytes) {
		final ByteArrayDataInput data = ByteStreams.newDataInput(bytes.array());
		if (data.readByte() != 49)
			return;
		final int count = Integer.reverseBytes(data.readInt());
		leaderboard.clear();
		for (int i = 0; i < count && i < 3; i++) {
			data.skipBytes(4);
			final StringBuffer name = new StringBuffer();
			char c;
			while ((c = data.readChar()) != 0)
				name.append(Character.reverseBytes(c));
			leaderboard.add(name.toString());
		}
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote) {}
	
	@Override
	public void onError(Exception e) {
		LOG.error("Server Connection Error: ", e);
	}
	
	public String getIP() {
		return ip;
	}
	
	public synchronized List<String> getLeaderboard() {
		return ImmutableList.copyOf(leaderboard);
	}
	
}
