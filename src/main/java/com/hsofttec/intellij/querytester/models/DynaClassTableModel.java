/*
 * The MIT License (MIT)
 *
 * Copyright © 2023 Sven Homburg, <homburgs@gmail.com>
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

package com.hsofttec.intellij.querytester.models;

import com.ceyoniq.nscale.al.core.cfg.PropertyDefinition;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.QueryType;
import com.hsofttec.intellij.querytester.services.PropertyUpdateService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import lombok.Getter;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynaClassTableModel extends AbstractTableModel {
    private static final Logger logger = LoggerFactory.getLogger(DynaClassTableModel.class);
    /**
     * synthetic columns fixed on the left side of the result table
     */
    public static final Set<String> FIXED_COLUMNS = Set.of(
            QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO,
            QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);

    /**
     * synthetic columns without a server side property, never editable
     */
    private static final Set<String> READ_ONLY_COLUMNS = Set.of(
            QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO,
            QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY,
            QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED);

    private final SettingsState settingsState;
    private final NscaleResult result;

    @Getter
    private final NscaleQueryInformation queryInformation;

    /**
     * property definitions per column name, loaded on first edit attempt
     */
    private final Map<String, Optional<PropertyDefinition>> propertyDefinitions = new HashMap<>();

    public DynaClassTableModel(SettingsState settingsState, NscaleResult result, NscaleQueryInformation queryInformation) {
        this.settingsState = settingsState;
        this.result = result;
        this.queryInformation = queryInformation;
    }

    @Override
    public String getColumnName( int column ) {
        return result.getPropertyNames().get(column);
    }

    @Override
    public int getRowCount() {
        return result.getDynaBeans().size();
    }

    @Override
    public int getColumnCount() {
        return result.getPropertyNames().size();
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex ) {
        return result.getDynaBeans().get(rowIndex);
    }

    @Override
    public boolean isCellEditable( int rowIndex, int columnIndex ) {
        if (!isEditableQuery() || READ_ONLY_COLUMNS.contains(getColumnName(columnIndex))) {
            return false;
        }
        DynaBean dynaBean = result.getDynaBeans().get(rowIndex);
        if (dynaBean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY) == null) {
            return false;
        }
        return PropertyUpdateService.isEditable(getPropertyDefinition(columnIndex));
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        result.getDynaBeans().get(rowIndex).set(getColumnName(columnIndex), value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * server side definition of the column property, null if it could not be loaded
     */
    public PropertyDefinition getPropertyDefinition(int columnIndex) {
        return propertyDefinitions.computeIfAbsent(getColumnName(columnIndex), this::loadPropertyDefinition).orElse(null);
    }

    private Optional<PropertyDefinition> loadPropertyDefinition(String propertyName) {
        try {
            return Optional.ofNullable(PropertyUpdateService.getDefinition(queryInformation, propertyName));
        } catch (Exception e) {
            logger.warn("no property definition for '{}': {}", propertyName, ExceptionUtils.getRootCauseMessage(e));
            return Optional.empty();
        }
    }

    /**
     * only plain repository results and masterdata results carry a key to save changes
     */
    private boolean isEditableQuery() {
        if (queryInformation == null) {
            return false;
        }
        return switch (queryInformation.getQueryMode()) {
            case REPOSITORY -> queryInformation.getQueryType() == QueryType.DEFAULT;
            case MASTERDATA -> true;
            default -> false;
        };
    }

    @Override
    public Class<?> getColumnClass( int columnIndex ) {
        Class<?> columnClass = super.getColumnClass(columnIndex);

        if (settingsState.isDisplayLockItem()) {
            if (QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED.equals(getColumnName(columnIndex))) {
                columnClass = Icon.class;
            }
        }

        return columnClass;
    }
}
