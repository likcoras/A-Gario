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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class ServerInfo {
	
	private Regions regions;
	private Totals totals;
	
	public static class RegionDeserializer implements JsonDeserializer<Regions> {
		@Override
		public Regions deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
			final Regions regions = new Regions();
			regions.regions = new HashMap<String, Integer>();
			for (final Entry<String, JsonElement> e : json.getAsJsonObject()
				.entrySet()) {
				final String name = e.getKey();
				final int numPlayers =
					e.getValue().getAsJsonObject().get("numPlayers").getAsInt();
				regions.regions.put(name, numPlayers);
			}
			return regions;
		}
	}
	
	public static GsonBuilder getBuilder() {
		return new GsonBuilder().registerTypeAdapter(Regions.class,
			new RegionDeserializer());
	}
	
	public Map<String, Integer> getRegions() {
		return regions.regions;
	}
	
	public int getTotals() {
		return totals.numPlayers;
	}
	
	private static class Regions {
		private Map<String, Integer> regions;
	}
	
	private static class Totals {
		private int numPlayers;
	}
	
}
