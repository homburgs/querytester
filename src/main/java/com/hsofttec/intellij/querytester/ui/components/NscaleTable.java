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

import com.hsofttec.intellij.querytester.models.DynaClassTableModel;
import com.hsofttec.intellij.querytester.renderer.DynaPropertyTableCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.ui.TableColumnAdjuster;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;

public class NscaleTable extends JBTable {
    private static final Logger logger = LoggerFactory.getLogger(NscaleTable.class);
    private static final SettingsState SETTINGS = SettingsService.getSettings();
    private final ConnectionService connectionService;
    private final QueryTester queryTester;
    private final ResultTableContextMenu contextMenu;
    private final TableColumnAdjuster tableColumnAdjuster;

    /**
     * shows the fixed columns (lineno, key, locked) as row header, so they stay visible on horizontal scrolling.
     * shares model and selection with this table.
     */
    private final JBTable fixedTable;
    private final TableColumnAdjuster fixedTableColumnAdjuster;

    public NscaleTable( QueryTester queryTester ) {
        this.queryTester = queryTester;
        connectionService = ConnectionService.getInstance();
        fixedTable = new JBTable(getModel());
        fixedTable.setAutoCreateColumnsFromModel(false);
        fixedTable.setSelectionModel(getSelectionModel());
        fixedTable.getTableHeader().setReorderingAllowed(false);

        DynaPropertyTableCellRenderer dynaPropertyTableCellRenderer = new DynaPropertyTableCellRenderer();
        contextMenu = new ResultTableContextMenu(queryTester);
        for (JBTable table : new JBTable[]{this, fixedTable}) {
            table.setFont(new Font(SETTINGS.getFontFace(), Font.PLAIN, SETTINGS.getFontSize()));
            table.setAutoResizeMode(JBTable.AUTO_RESIZE_OFF);
            table.setDefaultRenderer(Object.class, dynaPropertyTableCellRenderer);
            table.setDefaultRenderer(Icon.class, dynaPropertyTableCellRenderer);
            table.setComponentPopupMenu(contextMenu);
            installDeleteAction(table);
            installRightClickSelection(table);
        }

        // cells are edited on double click only, editability is decided by DynaClassTableModel
        setDefaultEditor(Object.class, new ResultCellEditor(queryTester));
        putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        tableColumnAdjuster = new TableColumnAdjuster(this);
        fixedTableColumnAdjuster = new TableColumnAdjuster(fixedTable);
        moveFixedColumns();
    }

    @Override
    public void setModel(@NotNull TableModel model) {
        super.setModel(model);
        // called by the JTable constructor before the fixed table exists
        if (fixedTable != null) {
            fixedTable.setModel(model);
            moveFixedColumns();
        }
    }

    /**
     * put the fixed table as row header into the scroll pane of this table
     */
    public void installFixedColumns(JScrollPane scrollPane) {
        scrollPane.setRowHeaderView(fixedTable);
        scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, fixedTable.getTableHeader());
        fixedTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent event) {
                updateFixedTableWidth(scrollPane);
            }

            @Override
            public void columnRemoved(TableColumnModelEvent event) {
                updateFixedTableWidth(scrollPane);
            }

            @Override
            public void columnMarginChanged(ChangeEvent event) {
                updateFixedTableWidth(scrollPane);
            }

            @Override
            public void columnMoved(TableColumnModelEvent event) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent event) {
            }
        });
        updateFixedTableWidth(scrollPane);
    }

    public void fontSettingsChanged() {
        setFont(new Font(SETTINGS.getFontFace(), Font.PLAIN, SETTINGS.getFontSize()));
        fixedTable.setFont(getFont());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // called by the JTable constructor before the fixed table exists
        if (fixedTable != null) {
            fixedTable.setEnabled(enabled);
        }
    }

    /**
     * Calculates the optimal width for the header of the given table. The
     * calculation is based on the preferred width of the header renderer.
     */
    public void calcHeaderWidth() {
        tableColumnAdjuster.adjustColumns();
        fixedTableColumnAdjuster.adjustColumns();
    }

    /**
     * increments every data column width, the fixed columns keep their width.
     */
    public void incrementHeaderWidth() {
        TableColumnModel columns = this.getColumnModel();
        for (int col = 0; col < columns.getColumnCount(); col++) {
            TableColumn column = columns.getColumn(col);
            int width = column.getPreferredWidth();
            column.setPreferredWidth(width + 100);
        }
    }

    /**
     * hide a column of this or the fixed table by collapsing its width
     *
     * @param propertyName property name of the column
     */
    public void hideColumn(String propertyName) {
        for (JBTable table : new JBTable[]{this, fixedTable}) {
            for (TableColumn column : Collections.list(table.getColumnModel().getColumns())) {
                if (propertyName.equals(getModel().getColumnName(column.getModelIndex()))) {
                    column.setMinWidth(0);
                    column.setMaxWidth(0);
                }
            }
        }
    }

    /**
     * move the fixed columns from this table to the fixed table, needed after every model change
     * because this table recreates all columns from the model. disabled by setting "fixedColumns".
     */
    private void moveFixedColumns() {
        TableColumnModel fixedColumns = fixedTable.getColumnModel();
        for (TableColumn column : Collections.list(fixedColumns.getColumns())) {
            fixedColumns.removeColumn(column);
        }
        if (!SETTINGS.isFixedColumns()) {
            return;
        }
        for (TableColumn column : Collections.list(getColumnModel().getColumns())) {
            if (DynaClassTableModel.FIXED_COLUMNS.contains(getModel().getColumnName(column.getModelIndex()))) {
                getColumnModel().removeColumn(column);
                fixedColumns.addColumn(column);
            }
        }
    }

    private void updateFixedTableWidth(JScrollPane scrollPane) {
        fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
        scrollPane.revalidate();
    }

    private void installDeleteAction(JTable table) {
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteResources");
        table.getActionMap().put("deleteResources", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (ResultTableSelectionActions.isDeleteAllowed(queryTester)) {
                    ResultTableSelectionActions.delete(queryTester, ResultTableSelectionActions.collect(table));
                }
            }
        });
    }

    private void installRightClickSelection(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                // on right click selects the row at which point the mouse is clicked, keeps an existing multi selection
                Point point = mouseEvent.getPoint();
                int currentRow = table.rowAtPoint(point);
                if (currentRow != -1 && SwingUtilities.isRightMouseButton(mouseEvent) && !table.isRowSelected(currentRow)) {
                    if (mouseEvent.getClickCount() == 1) {
                        try {
                            table.setRowSelectionInterval(currentRow, currentRow);
                        } catch (Exception e) {
                            logger.warn(ExceptionUtils.getRootCause(e).getLocalizedMessage());
                        }
                    }
                }
            }
        });
    }
}
