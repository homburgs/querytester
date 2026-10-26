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

package com.hsofttec.intellij.querytester.ui.components;

import com.ceyoniq.nscale.al.core.cfg.PropertyDefinition;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.models.DynaClassTableModel;
import com.hsofttec.intellij.querytester.services.PropertyUpdateService;
import com.hsofttec.intellij.querytester.ui.Notifier;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;

/**
 * inline editor for result table cells. the edited value is only saved after the user confirmed it.
 */
public class ResultCellEditor extends DefaultCellEditor {
    private static final Logger LOG = Logger.getInstance(ResultCellEditor.class);
    private final QueryTester queryTester;
    private final JBTextField textField;
    private DynaClassTableModel model;
    private DynaBean dynaBean;
    private String propertyName;
    private PropertyDefinition definition;
    private String originalText;
    private Object editedValue;
    private boolean stopping;

    public ResultCellEditor(QueryTester queryTester) {
        super(new JBTextField());
        this.queryTester = queryTester;
        textField = (JBTextField) getComponent();
        setClickCountToStart(2);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // the model returns the whole row bean for every cell
        int modelColumn = table.convertColumnIndexToModel(column);
        model = (DynaClassTableModel) table.getModel();
        dynaBean = (DynaBean) value;
        propertyName = model.getColumnName(modelColumn);
        definition = model.getPropertyDefinition(modelColumn);
        editedValue = dynaBean.get(propertyName);
        originalText = PropertyUpdateService.format(editedValue);
        textField.setText(originalText);
        return textField;
    }

    @Override
    public Object getCellEditorValue() {
        return editedValue;
    }

    @Override
    public boolean stopCellEditing() {
        // the confirmation dialog takes the focus, which triggers stopCellEditing again (terminateEditOnFocusLost).
        // returning true keeps the table from cancelling the editor while the dialog is open
        if (stopping) {
            return true;
        }
        stopping = true;
        try {
            return confirmAndSave();
        } finally {
            stopping = false;
        }
    }

    private boolean confirmAndSave() {
        String text = textField.getText();
        if (text.equals(originalText)) {
            cancelCellEditing();
            return true;
        }

        Object value;
        try {
            value = PropertyUpdateService.parse(text, definition, dynaBean.get(propertyName));
        } catch (IllegalArgumentException e) {
            Notifier.error(e.getMessage());
            return false;
        }

        int answer = Messages.showYesNoDialog(queryTester.getProject(),
                String.format("Change '%s' from '%s' to '%s'?", propertyName, originalText, text),
                "Save Property", Messages.getQuestionIcon());
        if (answer != Messages.YES) {
            cancelCellEditing();
            return true;
        }

        String key = (String) dynaBean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
        try {
            PropertyUpdateService.save(model.getQueryInformation(), key, propertyName, value);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Notifier.error(String.format("'%s' not saved: %s", propertyName, ExceptionUtils.getRootCauseMessage(e)));
            cancelCellEditing();
            return true;
        }

        editedValue = value;
        Notifier.information(String.format("'%s' successful saved", propertyName));
        return super.stopCellEditing();
    }
}
