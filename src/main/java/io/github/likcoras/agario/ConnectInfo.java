package io.github.likcoras.agario;

public class ConnectInfo {
	
	private String username;
	private boolean has_img;
	
	public boolean userExists() {
		return username != null;
	}
	
	public boolean hasImg() {
		return has_img;
	}
	
}
