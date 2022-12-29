/*
 * The MIT License (MIT)
 *
 * Copyright © 2022 Sven Homburg, <homburgs@gmail.com>
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

import com.hsofttec.intellij.querytester.QueryType;
import com.hsofttec.intellij.querytester.listeners.HistoryModifiedEventListener;
import com.hsofttec.intellij.querytester.models.DynaClassTableModel;
import com.hsofttec.intellij.querytester.models.HistoryComboBoxModel;
import com.hsofttec.intellij.querytester.models.NscaleQueryInformation;
import com.hsofttec.intellij.querytester.models.NscaleResult;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.HistoryService;
import com.hsofttec.intellij.querytester.services.QueryService;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.roots.IconActionComponent;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;

public class QueryTab {
    private static final SettingsState SETTINGS = SettingsService.getSettings();

    private final HistoryService historyService;
    private final ConnectionService connectionService;
    private final QueryService queryService;

    @Getter
    private final QueryTester queryTester;

    private final JBTabbedPane parent;
    private final MessageBus messageBus;

    private String tabTitle;

    @Getter
    private QueryTextboxPanel queryTextboxPanel;

    @Getter
    private QueryOptionsTabbedPane queryOptionsTabbedPane;

    @Getter
    private QuerySettingsPanel querySettingsPanel;

    @Getter
    private QueryResultTablePanel queryResultTablePanel;

    @Setter
    @Getter
    private NscaleQueryInformation queryInformation;

    @Setter
    @Getter
    private int resultPageNumber;

    private IconActionComponent btnClose;

    private GridBagConstraints gbc;

    private JPanel pnlTab;

    public QueryTab( QueryTester queryTester, JBTabbedPane parent, String tabTitle ) {
        this.queryTester = queryTester;
        this.parent = parent;
        this.tabTitle = tabTitle;

        messageBus = queryTester.getProject().getMessageBus();
        resultPageNumber = 1;
        queryService = queryTester.getProject().getService(QueryService.class);
        historyService = HistoryService.getSettings(queryTester.getProject());
        connectionService = ConnectionService.getInstance();

        createUI();

        loadComponentsData();
    }

    public NqlQueryTextbox getQueryTextbox() {
        return queryTextboxPanel.getQueryTextbox();
    }

    public NscaleTable getQueryResultTable() {
        return queryResultTablePanel.getQueryResultTable();
    }


    public void startQueryExecution() {
        ProgressManager progressManager = ProgressManager.getInstance();
        progressManager.runProcessWithProgressSynchronously(() -> {
            final ProgressIndicator progressIndicator = progressManager.getProgressIndicator();
            progressIndicator.setText("Execute query ...");

            NscaleResult nscaleResult = queryService.proccessQuery(queryInformation, resultPageNumber);

            if (nscaleResult != null) {

                queryInformation.setTotalSelectedItems(nscaleResult.getItemsTotal());

                UIUtil.invokeLaterIfNeeded(() -> {

//                    if ( queryInformation.getTotalSelectedItems( ) < 0 ) {
//                        actionToolbarComponent.setEnabled( true );
//                    }

                    getQueryResultTable().setModel(new DynaClassTableModel(nscaleResult));
                    getQueryResultTable().calcHeaderWidth();
                    if (!SETTINGS.isShowIdColumn()) {
                        getQueryResultTable().getColumnModel().getColumn(0).setMinWidth(0);
                        getQueryResultTable().getColumnModel().getColumn(0).setMaxWidth(0);
                    }
                    if (!SETTINGS.isShowKeyColumn() || queryInformation.getQueryType() == QueryType.AGGREGATE) {
                        getQueryResultTable().getColumnModel().getColumn(1).setMinWidth(0);
                        getQueryResultTable().getColumnModel().getColumn(1).setMaxWidth(0);
                    }
                    historyService.addQuery(queryInformation.getNqlQuery());
                });
            }
        }, "Executing query", true, queryTester.getProject());

    }

    /**
     * create all GUI components
     */
    private void createUI() {
        JBSplitter topBottomSplitter = createTopBottomSplitter();
        JBSplitter leftRightSplitter = createLeftRightSplitter(topBottomSplitter);

        parent.addTab(tabTitle, AllIcons.Actions.Close, leftRightSplitter);

        int index = parent.indexOfTab(tabTitle);
        pnlTab = new JPanel(new GridBagLayout());
        pnlTab.setOpaque(false);
        JLabel lblTitle = new JBLabel(tabTitle);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        pnlTab.add(lblTitle, gbc);

        parent.setTabComponentAt(index, pnlTab);
        parent.setSelectedIndex(index);

        historyService.addListener(new HistoryModifiedEventListener() {
            @Override
            public void notifyAdd( String query ) {
                querySettingsPanel.getInputHistory().addItem(query);
            }

            @Override
            public void notifyRemove( String query ) {
            }
        });

    }

    public void addCloseButton() {
        if (btnClose == null) {
            btnClose = new IconActionComponent(AllIcons.Actions.Close, null, "Close query tab", () -> {
                int index1 = parent.indexOfTab(tabTitle);
                if (index1 >= 0) {
                    parent.removeTabAt(index1);
                }
            });
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 0;
            pnlTab.add(btnClose, gbc);
        }
    }

    public void removeCloseButton() {
        pnlTab.remove(btnClose);
        btnClose = null;
    }

    /**
     * after all components created, we load settings and update data
     */
    private void loadComponentsData() {
        querySettingsPanel.getInputSelectedConnection().reloadItems();
        querySettingsPanel.getInputHistory().setModel(new HistoryComboBoxModel(historyService.getQueryList()));
        querySettingsPanel.getInputHistory().setMaximumSize(querySettingsPanel.getInputHistory().getPreferredSize());
        querySettingsPanel.getInputHistory().addActionListener(actionEvent -> {
            String selectedItem = (String) querySettingsPanel.getInputHistory().getSelectedItem();
            if (StringUtils.isNotBlank(selectedItem)) {
                queryTextboxPanel.getQueryTextbox().setText(selectedItem);
            }
        });
    }

    private JBSplitter createTopBottomSplitter() {
        JBSplitter splitter = new OnePixelSplitter(true, 0.3f);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.setSplitterProportionKey("query.splitter.key");

        queryTextboxPanel = new QueryTextboxPanel(this);
        queryResultTablePanel = new QueryResultTablePanel(this);

        splitter.setFirstComponent(queryTextboxPanel);
        splitter.setSecondComponent(queryResultTablePanel);

        return splitter;
    }

    private JBSplitter createLeftRightSplitter( JComponent firstComponent ) {
        JBSplitter splitter = new OnePixelSplitter(false, 0.3f);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.setSplitterProportionKey("main.splitter.key");
        splitter.setFirstComponent(firstComponent);

        JPanel secondComponent = JBUI.Panels.simplePanel();
        querySettingsPanel = new QuerySettingsPanel(this);
        queryOptionsTabbedPane = new QueryOptionsTabbedPane(this);
        secondComponent.add(querySettingsPanel, BorderLayout.NORTH);
        secondComponent.add(queryOptionsTabbedPane, BorderLayout.CENTER);

        splitter.setSecondComponent(secondComponent);
        return splitter;
    }
}
