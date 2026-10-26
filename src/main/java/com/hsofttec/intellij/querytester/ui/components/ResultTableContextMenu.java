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

import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceType;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.ResourceDialogModel;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.ui.CreateResourceDialog;
import com.hsofttec.intellij.querytester.ui.CreateResourceDialog.CreationMode;
import com.hsofttec.intellij.querytester.ui.Notifier;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.ui.ResourcePathDialog;
import com.hsofttec.intellij.querytester.ui.components.ResultTableSelectionActions.SelectedRow;
import com.hsofttec.intellij.querytester.ui.notifiers.PrepareQueryExecutionNotifier;
import com.hsofttec.intellij.querytester.ui.notifiers.RootResourceIdChangedNotifier;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Map;

public class ResultTableContextMenu extends JBPopupMenu implements PopupMenuListener {
    private final QueryTester queryTester;
    private static final Logger logger = LoggerFactory.getLogger(ResultTableContextMenu.class);
    private static final SettingsState SETTINGS = SettingsService.getSettings();
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();
    private String selectedResourceId;
    private List<SelectedRow> selectedRows = List.of();
    private JTable selectedTable;
    private JBMenuItem selectParentFolderId;
    private JBMenuItem searchParentMenuItem;
    private JBMenuItem showPathMenuItem;
    private JBMenuItem lockMenuItem;
    private JBMenuItem unlockMenuItem;
    private JBMenuItem showContentMenuItem;
    private JBMenuItem addDocumentMenuItem;
    private JBMenuItem addFolderMenuItem;
    private JBMenuItem addLinkMenuItem;
    private JBMenuItem deleteResourceMenuItem;
    private JBMenuItem copyKeyMenuItem;
    private JBMenuItem copySelectedValuesMenuItem;

    public ResultTableContextMenu( QueryTester queryTester ) {
        super();
        this.queryTester = queryTester;

        selectParentFolderId = new JBMenuItem("Search From Here");
        selectParentFolderId.setEnabled(false);
        selectParentFolderId.addActionListener(actionEvent -> {
            QueryTab queryTab = queryTester.getQueryTabbedPane().getActiveQueryTab();
            if (queryTab != null) {
                String parentResourceId = (String) getSelectedBean(queryTab).get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
                queryTab.getQueryOptionsTabbedPane().getInputRepositoryRoot().setText(parentResourceId);
                executeQuery();
            }
        });

        searchParentMenuItem = new JBMenuItem("Search Parent");
        searchParentMenuItem.setEnabled(true);
        searchParentMenuItem.addActionListener(actionEvent -> {
            QueryTab queryTab = queryTester.getQueryTabbedPane().getActiveQueryTab();
            if (queryTab != null) {
                String resourceId = (String) getSelectedBean(queryTab).get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
                BaseResource parentFolder = CONNECTION_SERVICE.getBaseResource(resourceId);
                BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(parentFolder.getParentresourceid());
                if (baseResource != null) {
                    queryTab.getQueryOptionsTabbedPane().getInputRepositoryRoot().setText(baseResource.getParentresourceid());
                    executeQuery();
                }
            }
        });

        showPathMenuItem = new JBMenuItem("Show Path");
        showPathMenuItem.addActionListener(actionEvent -> {
            ResourcePathDialog dialog = new ResourcePathDialog(queryTester.getProject());
            dialog.setData(selectedResourceId);
            if (dialog.showAndGet()) {
                BaseResource baseResource = dialog.getData();
                RootResourceIdChangedNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(RootResourceIdChangedNotifier.ROOT_RESOURCE_ID_CHANGED_TOPIC);
                notifier.doAction(baseResource.getResourceid());
            }
        });
        showPathMenuItem.setEnabled(false);

        showContentMenuItem = new JBMenuItem("Show Content");
        showContentMenuItem.setEnabled(false);

        // lock item
        lockMenuItem = new JBMenuItem("Lock");
        lockMenuItem.addActionListener(actionEvent -> ResultTableSelectionActions.lock(selectedTable, rowsToChange(true), true));
        lockMenuItem.setEnabled(false);

        // unlock item
        unlockMenuItem = new JBMenuItem("Unlock");
        unlockMenuItem.addActionListener(actionEvent -> ResultTableSelectionActions.lock(selectedTable, rowsToChange(false), false));
        unlockMenuItem.setEnabled(false);

        deleteResourceMenuItem = new JBMenuItem("Delete");
        deleteResourceMenuItem.setEnabled(false);
        deleteResourceMenuItem.addActionListener(actionEvent -> ResultTableSelectionActions.delete(queryTester, selectedRows));
        add(deleteResourceMenuItem);
        addSeparator();

        addDocumentMenuItem = new JBMenuItem("Add Document");
        addDocumentMenuItem.setEnabled(false);
        addDocumentMenuItem.addActionListener(actionEvent -> createResource(CreationMode.DOCUMENT));

        addFolderMenuItem = new JBMenuItem("Add Folder");
        addFolderMenuItem.setEnabled(false);
        addFolderMenuItem.addActionListener(actionEvent -> createResource(CreationMode.FOLDER));

        addLinkMenuItem = new JBMenuItem("Add Link");
        addLinkMenuItem.setEnabled(false);

        copyKeyMenuItem = new JBMenuItem("Copy Key");
        copyKeyMenuItem.setEnabled(false);
        copyKeyMenuItem.addActionListener(actionEvent -> {
            StringSelection stringSelection = new StringSelection(selectedResourceId);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });

        copySelectedValuesMenuItem = new JBMenuItem("Copy Selected Values");
        copySelectedValuesMenuItem.setEnabled(false);
        copySelectedValuesMenuItem.addActionListener(actionEvent -> {
            QueryTab queryTab = queryTester.getQueryTabbedPane().getActiveQueryTab();
            if (queryTab != null) {
                BasicDynaBean basicDynaBean = getSelectedBean(queryTab);
                StringBuilder stringBuilder1 = new StringBuilder();
                StringBuilder stringBuilder2 = new StringBuilder();
                for (Map.Entry<String, Object> entry : basicDynaBean.getMap().entrySet()) {
                    if (!entry.getKey().equals(QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO)) {
                        stringBuilder1.append(entry.getKey()).append(SETTINGS.getFieldSeparator());
                        stringBuilder2.append(entry.getValue()).append(SETTINGS.getFieldSeparator());
                    }
                }

                stringBuilder1.append(System.getProperty("line.separator")).append(stringBuilder2).append(System.getProperty("line.separator"));
                StringSelection stringSelection = new StringSelection(stringBuilder1.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });

        add(selectParentFolderId);
        add(searchParentMenuItem);
        add(showPathMenuItem);
        add(showContentMenuItem);
        addSeparator();
        add(lockMenuItem);
        add(unlockMenuItem);
        addSeparator();
        add(addDocumentMenuItem);
        add(addFolderMenuItem);
        add(addLinkMenuItem);
        addSeparator();
        add(copyKeyMenuItem);
        add(copySelectedValuesMenuItem);

        addPopupMenuListener(this);
    }

    /**
     * row bean of the selected cell in the result table of the given query tab
     */
    private BasicDynaBean getSelectedBean(QueryTab queryTab) {
        NscaleTable resultTable = queryTab.getQueryResultTable();
        return (BasicDynaBean) resultTable.getValueAt(resultTable.getSelectedRow(), resultTable.getSelectedColumn());
    }

    /**
     * re-run the query of the active query tab
     */
    private void executeQuery() {
        PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
        notifier.doAction();
    }

    /**
     * show the create dialog and create a document or folder below the selected resource
     *
     * @param creationMode DOCUMENT or FOLDER
     */
    private void createResource(CreationMode creationMode) {
        BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(selectedResourceId);
        CreateResourceDialog createResourceDialog = new CreateResourceDialog(queryTester.getProject(), creationMode);
        createResourceDialog.setData(baseResource);
        if (createResourceDialog.showAndGet()) {
            ResourceDialogModel data = createResourceDialog.getData();
            boolean document = creationMode == CreationMode.DOCUMENT;
            ResourceKey resourceKey = document
                    ? CONNECTION_SERVICE.createDocument(data.getParentResource(), data.getObjectclassName(), data.getDisplayname(), data.getSelectedFileName())
                    : CONNECTION_SERVICE.createFolder(data.getParentResource(), data.getObjectclassName(), data.getDisplayname());
            if (resourceKey != null) {
                String message = document ? "document successful created" : "folder successful created";
                UIUtil.invokeLaterIfNeeded(() -> Notifier.information(message));
                executeQuery();
            }
        }
    }

    /**
     * rows affected by lock/unlock: a single row always (state checked on server),
     * on multi selection only rows whose lock column differs from the target state
     *
     * @param locked target lock state
     */
    private List<SelectedRow> rowsToChange(boolean locked) {
        if (selectedRows.size() == 1) {
            return selectedRows;
        }
        return selectedRows.stream().filter(row -> row.isLocked() != locked).toList();
    }

    /**
     * disable all menu items
     */
    private void disableAllMenuItems() {
        selectParentFolderId.setEnabled(false);
        searchParentMenuItem.setEnabled(false);
        showPathMenuItem.setEnabled(false);
        showContentMenuItem.setEnabled(false);
        lockMenuItem.setEnabled(false);
        unlockMenuItem.setEnabled(false);
        addDocumentMenuItem.setEnabled(false);
        addFolderMenuItem.setEnabled(false);
        addLinkMenuItem.setEnabled(false);
        deleteResourceMenuItem.setEnabled(false);
        copyKeyMenuItem.setEnabled(false);
        copySelectedValuesMenuItem.setEnabled(false);
    }

    @Override
    public void popupMenuWillBecomeVisible( PopupMenuEvent popupMenuEvent ) {
        ResultTableContextMenu menu = (ResultTableContextMenu) popupMenuEvent.getSource();
        JTable invoker = (JTable) menu.getInvoker();

        // disable all menu items
        disableAllMenuItems();

        selectedTable = invoker;

        // get selected table rows with resource id
        try {
            selectedRows = ResultTableSelectionActions.collect(invoker);
        } catch (Exception e) {
            selectedRows = List.of();
            logger.warn(ExceptionUtils.getRootCause(e).getLocalizedMessage());
            return;
        }

        if (selectedRows.isEmpty()) {
            return;
        }

        QueryMode queryMode = queryTester.getActiveQueryTab().getQueryInformation().getQueryMode();

        // masterdata: only delete (single and multi selection)
        if (queryMode == QueryMode.MASTERDATA) {
            deleteResourceMenuItem.setEnabled(ResultTableSelectionActions.isDeleteAllowed(queryTester));
            return;
        }

        // multi selection: only lock, unlock and delete, lock state taken from lock column
        if (selectedRows.size() > 1) {
            if (queryMode == QueryMode.REPOSITORY) {
                lockMenuItem.setEnabled(selectedRows.stream().anyMatch(row -> !row.isLocked()));
                unlockMenuItem.setEnabled(selectedRows.stream().anyMatch(SelectedRow::isLocked));
                deleteResourceMenuItem.setEnabled(ResultTableSelectionActions.isDeleteAllowed(queryTester));
            }
            return;
        }

        // single selection: enable menu items depending on resource
        selectedResourceId = selectedRows.get(0).resourceId();
        if (queryMode == QueryMode.REPOSITORY) {
            BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(selectedResourceId);
            if (!baseResource.isLocked()) {
                lockMenuItem.setEnabled(true);
                unlockMenuItem.setEnabled(false);
            } else {
                lockMenuItem.setEnabled(false);
                unlockMenuItem.setEnabled(true);
            }

            deleteResourceMenuItem.setEnabled(true);

            if (baseResource.getResourcetype() == ResourceType.FOLDER.getId()) {
                addFolderMenuItem.setEnabled(true);
                addDocumentMenuItem.setEnabled(true);
                addLinkMenuItem.setEnabled(true);
            }
            showPathMenuItem.setEnabled(true);
            searchParentMenuItem.setEnabled(true);
            copyKeyMenuItem.setEnabled(true);
            copySelectedValuesMenuItem.setEnabled(true);
            selectParentFolderId.setEnabled(baseResource.getResourcetype() == ResourceType.FOLDER.getId());
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible( PopupMenuEvent popupMenuEvent ) {
    }

    @Override
    public void popupMenuCanceled( PopupMenuEvent popupMenuEvent ) {
    }
}
