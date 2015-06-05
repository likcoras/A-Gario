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

import org.pircbotx.Colors;
import org.pircbotx.User;

public class BotUtil {
	
	public static String addColors(String message) {
		return message.replaceAll("%n", Colors.NORMAL)
			.replaceAll("%y", Colors.YELLOW).replaceAll("%g", Colors.GREEN);
	}
	
	public static boolean isLikc(User user) {
		return user.getNick().equalsIgnoreCase("likcoras")
			&& user.getHostmask().equalsIgnoreCase("tis.very.bored");
	}
	
}
