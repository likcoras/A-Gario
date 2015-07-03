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

import lombok.extern.log4j.Log4j;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.ConnectEvent;

@Log4j
public class InfoHandler implements Handler {
	
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat
			.longTime();
	private static final PeriodFormatter PERIOD_FORMAT =
			new PeriodFormatterBuilder().printZeroNever().appendDays()
					.appendSuffix("d").appendSeparator(" ").appendHours()
					.appendSuffix("h").appendSeparator(" ").appendMinutes()
					.appendSuffix("m").appendSeparator(" ").appendSeconds()
					.appendSuffix("s").toFormatter();
	
	private static final String INFO_MSG =
			BotUtil.addColors("%cUptime: %n%s | %cTime: %n%s | %cSource: %nhttps://github.com/likcoras/A-Gario");
	
	private Instant start;
	
	@Override
	public void configure(BotConfig config) {}
	
	@Override
	public void handleEvent(Event<PircBotX> event) {
		if (event instanceof ConnectEvent)
			start = Instant.now();
	}
	
	@Override
	public String getResponse(Channel chan, User user, String message) {
		if (!message.equalsIgnoreCase("@info"))
			return "";
		log.info("Info requested");
		return String.format(INFO_MSG,
				new Period(start, Instant.now()).toString(PERIOD_FORMAT),
				Instant.now().toString(DATE_FORMAT));
	}
	
}
