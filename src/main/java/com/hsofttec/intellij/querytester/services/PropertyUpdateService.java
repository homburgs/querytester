/*
 * The MIT License (MIT)
 *
 * Copyright © 2026 Sven Homburg, <homburgs@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hsofttec.intellij.querytester.services;

import com.ceyoniq.nscale.al.core.ConfigurationService;
import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyName;
import com.ceyoniq.nscale.al.core.cfg.MasterdataPropertyName;
import com.ceyoniq.nscale.al.core.cfg.PropertyDefinition;
import com.ceyoniq.nscale.al.core.cfg.type.PropertyType;
import com.ceyoniq.nscale.al.core.common.Property;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataKey;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.models.NscaleQueryInformation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * property definitions, value conversion and saving of edited result table cells
 */
public final class PropertyUpdateService {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String NULL_TEXT = "<null>";

    private PropertyUpdateService() {
    }

    /**
     * read the server side definition of a result column property
     *
     * @param queryInformation query the result belongs to (repository or masterdata)
     * @param propertyName     property name
     */
    public static PropertyDefinition getDefinition(NscaleQueryInformation queryInformation, String propertyName) {
        ConfigurationService configurationService = CONNECTION_SERVICE.getSession().getConfigurationService();
        if (queryInformation.getQueryMode() == QueryMode.MASTERDATA) {
            return configurationService.getMasterdataPropertyDefinition(masterdataPropertyName(queryInformation, propertyName));
        }
        return configurationService.getIndexingPropertyDefinition(indexingPropertyName(queryInformation, propertyName));
    }

    /**
     * only single value, non system properties with a simple type can be edited
     */
    public static boolean isEditable(PropertyDefinition definition) {
        if (definition == null || definition.isSystem() || definition.isMultiValue()) {
            return false;
        }
        PropertyType type = definition.getType();
        return type.isStringType() || type.isLongType() || type.isIntegerType() || type.isDoubleType()
                || type.isBooleanType() || type.isDateType() || type.isTimestampType();
    }

    /**
     * format a property value as editable text, counterpart of {@link #parse}
     */
    public static String format(Object value) {
        if (value == null) {
            return NULL_TEXT;
        }
        if (value instanceof DateTime dateTime) {
            return dateTime.toString(TIMESTAMP_PATTERN);
        }
        if (value instanceof YearMonthDay yearMonthDay) {
            return yearMonthDay.toString(DATE_PATTERN);
        }
        if (value instanceof Date date) {
            return DateFormatUtils.format(date, DATE_PATTERN);
        }
        return String.valueOf(value);
    }

    /**
     * convert edited text into a value of the property type, empty text or {@value #NULL_TEXT} clears the value
     *
     * @param text         edited text
     * @param definition   property definition
     * @param currentValue current value, decides the class of date values
     * @throws IllegalArgumentException if the text does not match the property type
     */
    public static Object parse(String text, PropertyDefinition definition, Object currentValue) {
        if (StringUtils.isEmpty(text) || NULL_TEXT.equals(text.trim())) {
            return null;
        }

        PropertyType type = definition.getType();
        if (type.isStringType()) {
            return text;
        }

        String value = text.trim();
        try {
            if (type.isLongType()) {
                return Long.valueOf(value);
            }
            if (type.isIntegerType()) {
                return Integer.valueOf(value);
            }
            if (type.isDoubleType()) {
                return Double.valueOf(value);
            }
            if (type.isBooleanType() && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))) {
                return Boolean.valueOf(value);
            }
            if (type.isTimestampType()) {
                return DateTimeFormat.forPattern(TIMESTAMP_PATTERN).parseDateTime(value);
            }
            if (type.isDateType()) {
                DateTime date = DateTimeFormat.forPattern(DATE_PATTERN).parseDateTime(value);
                if (currentValue instanceof YearMonthDay) {
                    return new YearMonthDay(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
                }
                return date.toDate();
            }
        } catch (IllegalArgumentException e) {
            // invalid number or date, reported below
        }

        throw new IllegalArgumentException(String.format("'%s' is not a valid value for property type %s (date: %s, timestamp: %s)",
                text, type, DATE_PATTERN, TIMESTAMP_PATTERN));
    }

    /**
     * save a property value of a repository resource or masterdata entry
     *
     * @param queryInformation query the result belongs to (repository or masterdata)
     * @param key              resource id or masterdata id
     * @param propertyName     property name
     * @param value            new value, null clears the value
     */
    public static void save(NscaleQueryInformation queryInformation, String key, String propertyName, Object value) {
        Session session = CONNECTION_SERVICE.getSession();
        // hessian transfers the list class name, the server cannot instantiate JDK internal lists like List.of(...)
        List<Property> properties = new ArrayList<>();
        if (queryInformation.getQueryMode() == QueryMode.MASTERDATA) {
            properties.add(new Property(masterdataPropertyName(queryInformation, propertyName), value));
            session.getMasterdataService().update(new MasterdataKey<>(key), properties);
        } else {
            properties.add(new Property(indexingPropertyName(queryInformation, propertyName), value));
            session.getRepositoryService().updateProperties(new ResourceKey(key), properties);
        }
    }

    private static IndexingPropertyName indexingPropertyName(NscaleQueryInformation queryInformation, String propertyName) {
        return new IndexingPropertyName(propertyName, queryInformation.getDocumentAreaName());
    }

    private static MasterdataPropertyName masterdataPropertyName(NscaleQueryInformation queryInformation, String propertyName) {
        return new MasterdataPropertyName(propertyName, queryInformation.getMasterdataScope(), queryInformation.getDocumentAreaName());
    }
}
