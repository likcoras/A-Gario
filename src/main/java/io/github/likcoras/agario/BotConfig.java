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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.Cleanup;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

@Data
public class BotConfig {
	
	private Bot bot;
	private Server server;
	private List<String> channels;
	private Others others;
	
	public static BotConfig getConfig() throws IOException {
		final Yaml yaml = new Yaml();
		final BotConfig config =
				yaml.loadAs(Files.newBufferedReader(getFile(),
						StandardCharsets.UTF_8), BotConfig.class);
		return config;
	}
	
	private static Path getFile() throws IOException {
		final Path path = Paths.get("config.yml");
		if (!Files.exists(path))
			createDefaultFile(path);
		return path;
	}
	
	private static void createDefaultFile(Path path) throws IOException {
		Files.createFile(path);
		@Cleanup
		final BufferedReader read =
				new BufferedReader(new InputStreamReader(BotConfig.class
						.getClassLoader().getResourceAsStream("config.yml")));
		@Cleanup
		final BufferedWriter write =
				Files.newBufferedWriter(path, StandardCharsets.UTF_8);
		String line;
		while ((line = read.readLine()) != null)
			write.write(line + "\n");
		write.flush();
	}
	
	@Data
	public static class Bot {
		private String nick;
		private String login;
		private String realname;
		private String version;
		private String password;
	}
	
	@Data
	public static class Server {
		private String host;
		private int port;
		private boolean trust;
		private boolean ssl;
	}
	
	@Data
	public static class Others {
		private String apiKey;
		private String ownerNick;
		private String ownerHost;
	}
	
}
