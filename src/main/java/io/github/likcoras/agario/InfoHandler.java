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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.log4j.Logger;

public class InfoHandler {
	
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60L;
	private static final long HOUR = 60L;
	private static final long DAY = 24L;
	
	private static final String INFO_MSG =
		BotUtil
			.addColors("%cUptime: %n%s| %cTime: %n%s UTC | %cSource: %nhttps://github.com/likcoras/A-Gario");
	
	private static final Logger LOG = Logger.getLogger(InfoHandler.class);
	
	private final TimeZone utcTimeZone;
	private final DateFormat dateFormat;
	
	private long start;
	
	public InfoHandler() {
		utcTimeZone = TimeZone.getTimeZone("UTC");
		dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
		dateFormat.setTimeZone(utcTimeZone);
	}
	
	public void setStart() {
		start = System.currentTimeMillis();
	}
	
	public String getInfo() {
		LOG.info("Info requested");
		final String uptime = getTime(System.currentTimeMillis() - start);
		final String time = dateFormat.format(new Date());
		return String.format(INFO_MSG, uptime, time);
	}
	
	private String getTime(long millis) {
		final long seconds = millis / SECOND;
		final long minutes = seconds / MINUTE;
		final long hours = minutes / HOUR;
		final long days = hours / DAY;
		final StringBuffer out = new StringBuffer();
		if (days > 0)
			out.append(days + "d ");
		if (hours > 0)
			out.append(hours % DAY + "h ");
		if (minutes > 0)
			out.append(minutes % HOUR + "m ");
		if (seconds > 0)
			out.append(seconds % MINUTE + "s ");
		return out.toString();
	}
	
}
