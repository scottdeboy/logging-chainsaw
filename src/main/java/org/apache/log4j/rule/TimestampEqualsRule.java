/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.rule;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.chainsaw.logevents.ChainsawLoggingEvent;

import org.apache.log4j.spi.LoggingEventFieldResolver;

/**
 * A Rule class implementing equality evaluation for timestamps.
 *
 * @author Scott Deboy (sdeboy@apache.org)
 */
public class TimestampEqualsRule extends AbstractRule {
    /**
     * Serialization ID.
     */
  static final long serialVersionUID = 1639079557187790321L;
    /**
     * Resolver.
     */
  private static final LoggingEventFieldResolver RESOLVER =
          LoggingEventFieldResolver.getInstance();
    /**
     * Date format.
     */
  private static final DateFormat DATE_FORMAT =
          new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * time stamp.
     */
  private long timeStamp;

    /**
     * Create new instance.
     * @param value string representation of date.
     */
  private TimestampEqualsRule(final String value) {
    super();
    //expects value to be a timestamp value represented as a long
    try {
        timeStamp = DATE_FORMAT.parse(value).getTime();
    } catch (ParseException pe) {
        throw new IllegalArgumentException("Could not parse date: " + value);
    }
  }

    /**
     * Create new instance.
     * @param value string representation of date.
     * @return new instance
     */
  public static Rule getRule(final String value) {
      return new TimestampEqualsRule(value);
  }

    /** {@inheritDoc} */
  public boolean evaluate(final ChainsawLoggingEvent event, Map matches) {
    String eventTimeStampString = RESOLVER.getValue(LoggingEventFieldResolver.TIMESTAMP_FIELD, event).toString();
    long eventTimeStamp = Long.parseLong(eventTimeStampString) / 1000 * 1000;
    boolean result = (eventTimeStamp == timeStamp);
    if (result && matches != null) {
        Set entries = (Set) matches.get(LoggingEventFieldResolver.TIMESTAMP_FIELD);
        if (entries == null) {
            entries = new HashSet();
            matches.put(LoggingEventFieldResolver.TIMESTAMP_FIELD, entries);
        }
        entries.add(eventTimeStampString);
    }
    return result;
  }
}
