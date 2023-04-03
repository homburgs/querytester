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
import com.hsofttec.intellij.querytester.ui.Notifier;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.ui.ResourcePathDialog;
import com.hsofttec.intellij.querytester.ui.notifiers.PrepareQueryExecutionNotifier;
import com.hsofttec.intellij.querytester.ui.notifiers.RootResourceIdChangedNotifier;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class ResultTableContextMenu extends JBPopupMenu implements PopupMenuListener {
    private final QueryTester queryTester;
    private static final Logger logger = LoggerFactory.getLogger(ResultTableContextMenu.class);
    private static final SettingsState SETTINGS = SettingsService.getSettings();
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();
    private String selectedResourceId;
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

    private ActionListener actionListener = new AbstractAction() {
        @Override
        public void actionPerformed( ActionEvent e ) {

        }
    };

    public ResultTableContextMenu( QueryTester queryTester ) {
        super();
        this.queryTester = queryTester;

        selectParentFolderId = new JBMenuItem("Search From Here");
        selectParentFolderId.setEnabled(false);
        selectParentFolderId.addActionListener(actionEvent -> {
            QueryTab queryTab = queryTester.getQueryTabbedPane().getActiveQueryTab();
            if (queryTab != null) {
                BasicDynaBean basicDynaBean = (BasicDynaBean) queryTab.getQueryResultTable().getValueAt(queryTab.getQueryResultTable().getSelectedRow(), queryTab.getQueryResultTable().getSelectedColumn());
                String parentResourceId = (String) basicDynaBean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
                queryTab.getQueryOptionsTabbedPane().getInputRepositoryRoot().setText(parentResourceId);
                PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
                notifier.doAction();
            }
        });

        searchParentMenuItem = new JBMenuItem("Search Parent");
        searchParentMenuItem.setEnabled(true);
        searchParentMenuItem.addActionListener(actionEvent -> {
            QueryTab queryTab = queryTester.getQueryTabbedPane().getActiveQueryTab();
            if (queryTab != null) {
                BasicDynaBean basicDynaBean = (BasicDynaBean) queryTab.getQueryResultTable().getValueAt(queryTab.getQueryResultTable().getSelectedRow(), queryTab.getQueryResultTable().getSelectedColumn());
                String resourceId = (String) basicDynaBean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
                BaseResource parentFolder = CONNECTION_SERVICE.getBaseResource(resourceId);
                BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(parentFolder.getParentresourceid());
                if (baseResource != null) {
                    queryTab.getQueryOptionsTabbedPane().getInputRepositoryRoot().setText(baseResource.getParentresourceid());
                    PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
                    notifier.doAction();
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
        lockMenuItem.addActionListener(actionEvent -> {
            CONNECTION_SERVICE.lockResource(selectedResourceId);
            UIUtil.invokeLaterIfNeeded(() -> {
                Notifier.information("resource successful locked");
            });
        });
        lockMenuItem.setEnabled(false);

        // unlock item
        unlockMenuItem = new JBMenuItem("Unlock");
        unlockMenuItem.addActionListener(actionEvent -> {
            CONNECTION_SERVICE.unlockResource(selectedResourceId);
            UIUtil.invokeLaterIfNeeded(() -> {
                Notifier.information("resource successful un-locked");
            });
        });
        unlockMenuItem.setEnabled(false);

        deleteResourceMenuItem = new JBMenuItem("Delete Document/Folder");
        deleteResourceMenuItem.setEnabled(false);
        deleteResourceMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(selectedResourceId);
                CONNECTION_SERVICE.deleteResource(baseResource);
                UIUtil.invokeLaterIfNeeded(() -> {
                    Notifier.information("resource successful deleted");
                });
                PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
                notifier.doAction();
            }
        });
        add(deleteResourceMenuItem);
        addSeparator();

        addDocumentMenuItem = new JBMenuItem("Add Document");
        addDocumentMenuItem.setEnabled(false);
        addDocumentMenuItem.addActionListener(actionEvent -> {
            BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(selectedResourceId);
            CreateResourceDialog createResourceDialog = new CreateResourceDialog(queryTester.getProject(), CreateResourceDialog.CreationMode.DOCUMENT);
            createResourceDialog.setData(baseResource);
            if (createResourceDialog.showAndGet()) {
                ResourceDialogModel data = createResourceDialog.getData();
                ResourceKey resourceKey = CONNECTION_SERVICE.createDocument(data.getParentResource(), data.getObjectclassName(), data.getDisplayname(), data.getSelectedFileName());
                if (resourceKey != null) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        Notifier.information("document successful created");
                    });
                    PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
                    notifier.doAction();
                }
            }
        });

        addFolderMenuItem = new JBMenuItem("Add Folder");
        addFolderMenuItem.setEnabled(false);
        addFolderMenuItem.addActionListener(actionEvent -> {
            BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(selectedResourceId);
            CreateResourceDialog createResourceDialog = new CreateResourceDialog(queryTester.getProject(), CreateResourceDialog.CreationMode.FOLDER);
            createResourceDialog.setData(baseResource);
            if (createResourceDialog.showAndGet()) {
                ResourceDialogModel data = createResourceDialog.getData();
                ResourceKey resourceKey = CONNECTION_SERVICE.createFolder(data.getParentResource(), data.getObjectclassName(), data.getDisplayname());
                if (resourceKey != null) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        Notifier.information("folder successful created");
                    });
                    PrepareQueryExecutionNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
                    notifier.doAction();
                }
            }
        });

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
                BasicDynaBean basicDynaBean = (BasicDynaBean) queryTab.getQueryResultTable().getValueAt(queryTab.getQueryResultTable().getSelectedRow(), queryTab.getQueryResultTable().getSelectedColumn());
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
        BasicDynaBean basicDynaBean = null;
        ResultTableContextMenu menu = (ResultTableContextMenu) popupMenuEvent.getSource();
        JTable invoker = (JTable) menu.getInvoker();

        // disable all menu items
        disableAllMenuItems();

        // get selected table row and search for selected resource id
        try {
            int selectedRow = invoker.getSelectedRow();
            int selectedColumn = invoker.getSelectedColumn();
            if (selectedRow > -1) {
                basicDynaBean = (BasicDynaBean) invoker.getValueAt(selectedRow, selectedColumn);
            }
        } catch (Exception e) {
            logger.warn(ExceptionUtils.getRootCause(e).getLocalizedMessage());
            return;
        }

        // if resource id found, enable some menu items
        if (basicDynaBean != null) {
            // check if resource key is exists
            if (basicDynaBean.getMap().containsKey(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY)) {
                selectedResourceId = (String) basicDynaBean.get(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY);
                QueryMode queryMode = queryTester.getActiveQueryTab().getQueryInformation().getQueryMode();
                if (queryMode == QueryMode.REPOSITORY) {
                    BaseResource baseResource = CONNECTION_SERVICE.getBaseResource(selectedResourceId);
                    if (!baseResource.isLocked()) {
                        lockMenuItem.setEnabled(true);
                        unlockMenuItem.setEnabled(false);
                    } else {
                        lockMenuItem.setEnabled(false);
                        unlockMenuItem.setEnabled(true);
                    }

                    if (SETTINGS.isShowDelete()) {
                        deleteResourceMenuItem.setEnabled(true);
                    }

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
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible( PopupMenuEvent popupMenuEvent ) {
    }

    @Override
    public void popupMenuCanceled( PopupMenuEvent popupMenuEvent ) {
    }
}
