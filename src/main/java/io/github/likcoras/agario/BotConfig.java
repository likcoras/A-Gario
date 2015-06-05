/*
 * Copyright 2015 likcoras
 * 
 * This file is part of A-Gario
 * 
 * A-Gario is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * A-Gario is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with A-Gario.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.likcoras.agario;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.yaml.snakeyaml.Yaml;

public class BotConfig {
	
	private Bot bot;
	private Server server;
	private List<String> channels;
	
	public static BotConfig getConfig() throws IOException {
		final Yaml yaml = new Yaml();
		final FileReader read = new FileReader(getFile());
		final BotConfig config = yaml.loadAs(read, BotConfig.class);
		read.close();
		return config;
	}
	
	private static File getFile() throws IOException {
		final File file = new File("config.yml");
		if (!file.exists())
			createDefaultFile(file);
		return file;
	}
	
	private static void createDefaultFile(File file) throws IOException {
		file.createNewFile();
		final BufferedReader read =
			new BufferedReader(new InputStreamReader(BotConfig.class
				.getClassLoader().getResourceAsStream("config.yml")));
		final BufferedWriter write = new BufferedWriter(new FileWriter(file));
		String line;
		while ((line = read.readLine()) != null)
			write.write(line + "\n");
		read.close();
		write.flush();
		write.close();
	}
	
	public Bot getBot() {
		return bot;
	}
	
	public void setBot(Bot bot) {
		this.bot = bot;
	}
	
	public Server getServer() {
		return server;
	}
	
	public void setServer(Server server) {
		this.server = server;
	}
	
	public List<String> getChannels() {
		return channels;
	}
	
	public void setChannels(List<String> channels) {
		this.channels = channels;
	}
	
	public static class Bot {
		
		private String nick;
		private String login;
		private String realname;
		private String version;
		private String password;
		
		public String getNick() {
			return nick;
		}
		
		public void setNick(String nick) {
			this.nick = nick;
		}
		
		public String getLogin() {
			return login;
		}
		
		public void setLogin(String login) {
			this.login = login;
		}
		
		public String getRealname() {
			return realname;
		}
		
		public void setRealname(String realname) {
			this.realname = realname;
		}
		
		public String getVersion() {
			return version;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword(String password) {
			this.password = password;
		}
	}
	
	public static class Server {
		
		private String host;
		private int port;
		private boolean trust;
		private boolean ssl;
		
		public String getHost() {
			return host;
		}
		
		public void setHost(String host) {
			this.host = host;
		}
		
		public int getPort() {
			return port;
		}
		
		public void setPort(int port) {
			this.port = port;
		}
		
		public boolean isTrust() {
			return trust;
		}
		
		public void setTrust(boolean trust) {
			this.trust = trust;
		}
		
		public boolean isSsl() {
			return ssl;
		}
		
		public void setSsl(boolean ssl) {
			this.ssl = ssl;
		}
		
	}
	
}
