/**
 * Copyright (C) 2008, Claus Nielsen, clausn999@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package dk.clanie.test.logging;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import lombok.NonNull;
import lombok.Value;

/**
 * Immutable collection of LoggingEvents captured by {@link LogCapturer}.
 * 
 * @author Claus Nielsen
 */
@Value
public class CapturedLoggingEvents {

	@NonNull
	List<ILoggingEvent> events;
	Exception exception;
	

	public boolean completedNormally() {
		return exception == null;
	}


	/**
	 * Gets all LoggingEvents which satisfies the supplied Predicate.
	 * 
	 * @param predicate
	 * @return List&lt;LoggingEvent&gt;
	 */
	public List<ILoggingEvent> getEvents(Predicate<ILoggingEvent> predicate) {
		return events == null ? emptyList() : events.stream().filter(predicate).collect(toList());
	}


	/**
	 * Gets messages from all LoggingEvents.
	 * 
	 * @return List&lt;String&gt;
	 */
	public List<String> getMessages() {
		return getMessages(events);
	}

	private List<String> getMessages(List<ILoggingEvent> events) {
		List<String> messages = new ArrayList<>();
		for (ILoggingEvent event : events) {
			messages.add(event.getMessage());
		}
		return messages;
	}


	/**
	 * Gets messages from all LoggingEvents which satisfies the supplied Predicate.
	 * 
	 * @param predicate
	 * @return List&lt;String&gt;
	 */
	public List<String> getMessages(Predicate<ILoggingEvent> predicate) {
		return getMessages(getEvents(predicate));
	}


	/**
	 * Gets Thowables from all LoggingEvents.
	 * 
	 * Note that not all LoggingEvents have Throwables and therefore the 
	 * returned Collection may be less than the number of LoggingEvents.
	 * 
	 * @return List&lt;Throwable&gt;
	 */
	public List<Throwable> getThrowables() {
		return getThrowables(events);
	}

	private List<Throwable> getThrowables(List<ILoggingEvent> events) {
		List<Throwable> throwables = new ArrayList<>();
		for (ILoggingEvent event : events) {
			ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
			if (throwableProxy != null)
				throwables.add(throwableProxy.getThrowable());
		}
		return throwables;
	}


	/**
	 * Gets Thowables from all LoggingEvents which satisfies the supplied Predicate.
	 * 
	 * @param predicate
	 * @return List&lt;Throwable&gt;
	 */
	public List<Throwable> getThrowables(Predicate<ILoggingEvent> predicate) {
		return getThrowables(getEvents(predicate));
	}


	/**
	 * Gets Levels from all LoggingEvents.
	 * 
	 * @return List&lt;Level&gt;
	 */
	public List<Level> getLevels() {
		return getLevels(events);
	}

	private List<Level> getLevels(List<ILoggingEvent> events) {
		List<Level> levels = new ArrayList<>();
		for (ILoggingEvent event : events) {
			levels.add(event.getLevel());
		}
		return levels;
	}


	/**
	 * Gets Levels from all LoggingEvents which satisfies the supplied Matcher.
	 * 
	 * @param predicate
	 * @return List&lt;Level&gt;
	 */
	public List<Level> getLevels(Predicate<ILoggingEvent> predicate) {
		return getLevels(getEvents(predicate));
	}


	/**
	 * Gets the number of LoggingEvents.
	 * 
	 * @return the number of LoggingEvents.
	 */
	public Integer getSize() {
		return events.size();
	}


}
