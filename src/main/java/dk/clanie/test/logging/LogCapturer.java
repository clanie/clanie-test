/*
 * Copyright (C) 2024, Claus Nielsen, clausn999@gmail.com
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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Utility for capturing LoggingEvents.
 * 
 * Intended to be used in unit tests to monitor what's logged from the class under test.
 * <p>
 * This class is <a href="http://www.logback.org">Logback</a> specific
 * because it needs to configure logging, but logging in the monitored
 * code need not be. It must use <a href="http://www.slf4j">SLF4J</a>, though.
 * </p>
 * 
 * @author Claus Nielsen
 */
public class LogCapturer {


	/**
	 * Executes the given Runnable and returns the LoggingEvents captured from it.
	 * 
	 * @return List&lt;LoggingEvent&gt;
	 */
	public static CapturedLoggingEvents capture(Class<?> loggingClass, Runnable runnable) {
		Logger logger = (Logger) LoggerFactory.getLogger(loggingClass);
		return capture(logger, runnable);
	}


	/**
	 * Executes the given Runnable and returns the LoggingEvents captured from it.
	 * 
	 * @return List&lt;LoggingEvent&gt;
	 */
	public static CapturedLoggingEvents capture(String loggerName, Runnable runnable) {
		Logger logger = (Logger) LoggerFactory.getLogger(loggerName);
		return capture(logger, runnable);
	}


	/**
	 * Executes the given Runnable and returns the LoggingEvents captured from it.
	 * 
	 * @return List&lt;LoggingEvent&gt;
	 */
	public static CapturedLoggingEvents capture(Logger logger, Runnable runnable) {
		// Configure logging so that LoggingEvents can be captured
		logger.setAdditive(false);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<ILoggingEvent>();
		listAppender.start();
		logger.addAppender(listAppender);
		Exception e = null;
		// Perform monitored action
		try {
			runnable.run();
		} catch (Exception caughtException) {
			e = caughtException;
		} finally {
			// Restore normal logging
			logger.detachAppender(listAppender);
			logger.setAdditive(true);
		}
		// Return captured LoggingEvents
		return new CapturedLoggingEvents(listAppender.list, e);
	}


}
