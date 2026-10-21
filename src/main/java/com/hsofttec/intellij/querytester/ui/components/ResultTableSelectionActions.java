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

package com.hsofttec.intellij.querytester.ui.components;

import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.Notifier;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.ui.notifiers.PrepareQueryExecutionNotifier;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * actions on the selected rows of the result table (lock, unlock, delete)
 */
public final class ResultTableSelectionActions {
    private static final Logger logger = LoggerFactory.getLogger(ResultTableSelectionActions.class);
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();

    public record SelectedRow(BasicDynaBean bean, int modelRow, String resourceId) {
        public boolean isLocked() {
            if (bean.getDynaClass().getDynaProperty(QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED) == null) {
                return false;
            }
            return Boolean.TRUE.equals(bean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED));
        }
    }

    private ResultTableSelectionActions() {
    }

    /**
     * collect all selected rows that carry a resource key
     *
     * @param table result table
     * @return selected rows
     */
    public static List<SelectedRow> collect(JTable table) {
        List<SelectedRow> selectedRows = new ArrayList<>();
        if (table.getColumnCount() == 0) {
            return selectedRows;
        }
        for (int row : table.getSelectedRows()) {
            if (table.getValueAt(row, 0) instanceof BasicDynaBean bean
                    && bean.getMap().containsKey(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY)) {
                String resourceId = (String) bean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
                selectedRows.add(new SelectedRow(bean, table.convertRowIndexToModel(row), resourceId));
            }
        }
        return selectedRows;
    }

    /**
     * delete is only allowed in repository and masterdata mode
     */
    public static boolean isDeleteAllowed(QueryTester queryTester) {
        if (queryTester.getActiveQueryTab() == null) {
            return false;
        }
        QueryMode queryMode = queryTester.getActiveQueryTab().getQueryInformation().getQueryMode();
        return queryMode == QueryMode.REPOSITORY || queryMode == QueryMode.MASTERDATA;
    }

    /**
     * lock or unlock the given rows and refresh their lock column without re-running the query
     */
    public static void lock(JTable table, List<SelectedRow> rows, boolean locked) {
        int failed = 0;
        for (SelectedRow row : rows) {
            try {
                if (locked) {
                    CONNECTION_SERVICE.lockResource(row.resourceId());
                } else {
                    CONNECTION_SERVICE.unlockResource(row.resourceId());
                }
                updateLockedColumn(table, row, locked);
            } catch (Exception e) {
                failed++;
                logger.warn(ExceptionUtils.getRootCause(e).getLocalizedMessage());
            }
        }
        notifyResult(rows.size() - failed, failed, locked ? "locked" : "un-locked");
    }

    /**
     * delete the given rows and re-run the query. repository resources are deleted logically or physically
     * (user decides in the confirmation dialog), masterdata entries are always deleted physically.
     */
    public static void delete(QueryTester queryTester, List<SelectedRow> rows) {
        if (rows.isEmpty()) {
            return;
        }
        boolean deleted = queryTester.getActiveQueryTab().getQueryInformation().getQueryMode() == QueryMode.MASTERDATA
                ? deleteMasterdata(queryTester, rows)
                : deleteResources(queryTester, rows);
        if (!deleted) {
            return;
        }
        PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
        notifier.doAction();
    }

    /**
     * @return false if the user cancelled
     */
    private static boolean deleteMasterdata(QueryTester queryTester, List<SelectedRow> rows) {
        int answer = Messages.showOkCancelDialog(queryTester.getProject(),
                String.format("Physically delete %d masterdata entries? This cannot be undone.", rows.size()),
                "Delete Masterdata", "Delete Physically", Messages.getCancelButton(), Messages.getWarningIcon());
        if (answer != Messages.OK) {
            return false;
        }

        int failed = 0;
        for (SelectedRow row : rows) {
            try {
                CONNECTION_SERVICE.deleteMasterdata(row.resourceId());
            } catch (Exception e) {
                failed++;
                logger.warn(ExceptionUtils.getRootCause(e).getLocalizedMessage());
            }
        }
        notifyResult(rows.size() - failed, failed, "physically deleted");
        return true;
    }

    /**
     * @return false if the user cancelled
     */
    private static boolean deleteResources(QueryTester queryTester, List<SelectedRow> rows) {
        int answer = Messages.showDialog(queryTester.getProject(),
                String.format("Delete %d resource(s)?%nLogically deleted resources can be restored, physically deleted resources cannot.", rows.size()),
                "Delete Resources",
                new String[]{"Delete Logically", "Delete Physically", Messages.getCancelButton()},
                0, Messages.getWarningIcon());
        if (answer != 0 && answer != 1) {
            return false;
        }
        boolean physical = answer == 1;

        int failed = 0;
        for (SelectedRow row : rows) {
            try {
                CONNECTION_SERVICE.deleteResource(CONNECTION_SERVICE.getBaseResource(row.resourceId()), physical);
            } catch (Exception e) {
                failed++;
                logger.warn(ExceptionUtils.getRootCause(e).getLocalizedMessage());
            }
        }
        notifyResult(rows.size() - failed, failed, physical ? "physically deleted" : "logically deleted");
        return true;
    }

    private static void updateLockedColumn(JTable table, SelectedRow row, boolean locked) {
        if (row.bean().getDynaClass().getDynaProperty(QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED) == null) {
            return;
        }

        row.bean().set(QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED, locked);
        if (table.getModel() instanceof AbstractTableModel tableModel) {
            UIUtil.invokeLaterIfNeeded(() -> tableModel.fireTableRowsUpdated(row.modelRow(), row.modelRow()));
        }
    }

    private static void notifyResult(int succeeded, int failed, String action) {
        UIUtil.invokeLaterIfNeeded(() -> {
            if (failed == 0) {
                Notifier.information(String.format("%d resource(s) successful %s", succeeded, action));
            } else {
                Notifier.warning(String.format("%d resource(s) successful %s, %d failed", succeeded, action, failed));
            }
        });
    }
}
