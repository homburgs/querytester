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

import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.services.ObjectclassChangeService;
import com.hsofttec.intellij.querytester.ui.Notifier;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.SimpleListCellRenderer;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * inline editor for the objectclass column. offers only objectclasses allowed by configuration and permissions,
 * the change is only saved after the user confirmed it.
 */
public class ObjectclassCellEditor extends DefaultCellEditor {
    private static final Logger LOG = Logger.getInstance(ObjectclassCellEditor.class);
    private final QueryTester queryTester;
    private final ComboBox<ObjectclassName> comboBox;
    private String resourceId;
    private ObjectclassName originalValue;
    private ObjectclassName editedValue;
    private boolean stopping;

    public ObjectclassCellEditor(QueryTester queryTester) {
        super(new ComboBox<ObjectclassName>());
        this.queryTester = queryTester;
        //noinspection unchecked
        comboBox = (ComboBox<ObjectclassName>) getComponent();
        comboBox.setRenderer(SimpleListCellRenderer.create("", ObjectclassName::getName));
        setClickCountToStart(2);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // the model returns the whole row bean for every cell
        DynaBean dynaBean = (DynaBean) value;
        resourceId = (String) dynaBean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
        originalValue = (ObjectclassName) dynaBean.get(QueryTesterConstants.PROPERTY_NAME_OBJECTCLASS);
        editedValue = originalValue;

        List<ObjectclassName> items = new ArrayList<>();
        items.add(originalValue);
        try {
            List<ObjectclassName> possible = ObjectclassChangeService.getPossibleObjectclasses(resourceId);
            if (possible.isEmpty()) {
                Notifier.warning("no objectclass change allowed for this resource");
            }
            items.addAll(possible);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            Notifier.error(String.format("possible objectclasses not loaded: %s", ExceptionUtils.getRootCauseMessage(e)));
        }

        stopping = true;
        try {
            comboBox.setModel(new CollectionComboBoxModel<>(items, originalValue));
        } finally {
            stopping = false;
        }
        return comboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return editedValue;
    }

    @Override
    public boolean stopCellEditing() {
        // setting the model and the confirmation dialog trigger stopCellEditing again
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
        ObjectclassName selected = (ObjectclassName) comboBox.getSelectedItem();
        if (selected == null || selected.equals(originalValue)) {
            cancelCellEditing();
            return true;
        }

        int answer = Messages.showYesNoDialog(queryTester.getProject(),
                String.format("Change objectclass from '%s' to '%s'?", originalValue.getName(), selected.getName()),
                "Change Objectclass", Messages.getQuestionIcon());
        if (answer != Messages.YES) {
            cancelCellEditing();
            return true;
        }

        try {
            ObjectclassChangeService.change(resourceId, selected);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Notifier.error(String.format("objectclass not changed: %s", ExceptionUtils.getRootCauseMessage(e)));
            cancelCellEditing();
            return true;
        }

        editedValue = selected;
        Notifier.information(String.format("objectclass changed to '%s'", selected.getName()));
        return super.stopCellEditing();
    }
}
