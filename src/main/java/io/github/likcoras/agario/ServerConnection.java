package io.github.likcoras.agario;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;

public class ServerConnection extends WebSocketClient {
	
	private static final Logger LOG = Logger.getLogger(ServerConnection.class);
	
	private static final Map<String, String> HEADERS =
		new ImmutableMap.Builder<String, String>().put("Origin",
			"http://agar.io").build();
	
	private final Lock lock;
	private final Condition done;
	private final String ip;
	private final List<String> leaderboard;
	
	public ServerConnection(String ip) throws URISyntaxException,
		InterruptedException {
		super(new URI("ws://" + ip), new Draft_17(), HEADERS, 1);
		this.ip = ip;
		leaderboard = new ArrayList<String>();
		lock = new ReentrantLock();
		done = lock.newCondition();
		connectBlocking();
	}
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
		lock.lock();
		send(ByteBuffer.allocate(5).put(UnsignedBytes.MAX_POWER_OF_TWO)
			.putInt(4).array());
		send(ByteBuffer.allocate(5).put(UnsignedBytes.MAX_VALUE)
			.putInt(673720361).array());
	}
	
	@Override
	public void onMessage(String message) {}
	
	@Override
	public void onMessage(ByteBuffer bytes) {
		if (bytes.get() != 49)
			return;
		final int count = Integer.reverseBytes(bytes.getInt());
		for (int i = 0; i < count; i++) {
			bytes.position(bytes.position() + 4);
			final StringBuffer name = new StringBuffer();
			char c;
			while ((c = bytes.getChar()) != 0)
				name.append(Character.reverseBytes(c));
			leaderboard.add(name.toString());
		}
		done.signal();
		lock.unlock();
		close();
	}
	
	@Override
	public void onClose(int code, String reason, boolean remote) {}
	
	@Override
	public void onError(Exception e) {
		LOG.error("Server Connection Error:", e);
	}
	
	public String getIP() {
		return ip;
	}
	
	public List<String> getLeaderboard() throws InterruptedException {
		lock.lock();
		while (leaderboard.size() < 1)
			done.await();
 		return ImmutableList.copyOf(leaderboard);
	}
	
}
