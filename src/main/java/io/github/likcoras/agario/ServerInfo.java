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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Value;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

@Value
public class ServerInfo {
	
	private Map<String, Integer> regions;
	private int totals;
	
	public static class ServerInfoDeserializer implements JsonDeserializer<ServerInfo> {
		
		@Override
		public ServerInfo deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			final JsonObject rawInfo = json.getAsJsonObject();
			final JsonObject regionsInfo =
					rawInfo.get("regions").getAsJsonObject();
			final Map<String, Integer> regions = parseRegionsInfo(regionsInfo);
			final int totals =
					rawInfo.get("totals").getAsJsonObject().get("numPlayers")
							.getAsInt();
			return new ServerInfo(regions, totals);
		}
		
		private static Map<String, Integer> parseRegionsInfo(
				JsonObject regionsInfo) {
			final Map<String, Integer> regions = new HashMap<String, Integer>();
			for (final Entry<String, JsonElement> e : regionsInfo.entrySet()) {
				final String name = e.getKey().replaceAll(".+-", "");
				final int numPlayers =
						e.getValue().getAsJsonObject().get("numPlayers")
								.getAsInt();
				regions.put(name, numPlayers);
			}
			return ImmutableMap.copyOf(regions);
		}
		
	}
	
	public static Gson getGson() {
		return new GsonBuilder().registerTypeAdapter(ServerInfo.class,
				new ServerInfoDeserializer()).create();
	}
	
}
