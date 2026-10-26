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

package com.hsofttec.intellij.querytester.ui;

import com.ceyoniq.nscale.al.core.cfg.MasterdataScope;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryType;
import com.hsofttec.intellij.querytester.models.NscaleQueryInformation;
import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.ui.actions.AddConnectionAction;
import com.hsofttec.intellij.querytester.ui.actions.AddQueryTabAction;
import com.hsofttec.intellij.querytester.ui.actions.ExecuteActiveQueryAction;
import com.hsofttec.intellij.querytester.ui.actions.ShowPluginSettingsAction;
import com.hsofttec.intellij.querytester.ui.components.*;
import com.hsofttec.intellij.querytester.ui.notifiers.*;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class QueryTester extends SimpleToolWindowPanel {
    @Getter
    private final Project project;

    @Getter
    private QueryTabbedPane queryTabbedPane;

    private final MessageBus messageBus;

    private final MessageBusConnection messageBusConnection;

    public QueryTester( Project project ) {
        super( false, true );
        this.project = project;
        messageBus = project.getMessageBus( );
        messageBusConnection = messageBus.connect( );

        subscribeNotifications( );

        queryTabbedPane = createUIComponents( );

        setToolbar( createToolBar( ) );
        setContent( queryTabbedPane );
    }


    private JComponent createToolBar( ) {
        DefaultActionGroup actionGroup = new DefaultActionGroup( );
        actionGroup.add( new ExecuteActiveQueryAction( this ) );
        actionGroup.add( new AddQueryTabAction( this ) );
        actionGroup.add(new AddConnectionAction(this));
        actionGroup.add( new ShowPluginSettingsAction( this ) );
        ActionToolbar actionToolbar = ActionManager.getInstance( ).createActionToolbar( ActionPlaces.TOOLBAR, actionGroup, true );
        actionToolbar.setTargetComponent( queryTabbedPane );
        return actionToolbar.getComponent( );
    }

    /**
     * Creates and initializes the QueryTabbedPane component.
     * Initializes the tabbed pane and creates the first query tab.
     *
     * @return the initialized QueryTabbedPane component
     */
    private QueryTabbedPane createUIComponents( ) {
        queryTabbedPane = new QueryTabbedPane( this );
        queryTabbedPane.createQueryTab( );
        return queryTabbedPane;
    }

    /**
     * get the activated query tab
     */
    public QueryTab getActiveQueryTab( ) {
        return queryTabbedPane.getActiveQueryTab( );
    }

    /**
     * subscribe diverse notifiers for message bus.
     */
    private void subscribeNotifications( ) {
        messageBusConnection.subscribe( CheckServerConnectionNotifier.CHECK_SERVER_CONNECTION_TOPIC, new CheckServerConnectionNotifier( ) {
            @Override
            public void beforeAction( ConnectionSettings settings ) {
                UIUtil.invokeLaterIfNeeded( ( ) -> {
                    QueryTab queryTab = getActiveQueryTab( );
                    if ( queryTab != null ) {
                        setQueryTabEnabled(queryTab, false);
                    }
                } );
            }

            @Override
            public void afterAction( ConnectionSettings settings, boolean connectedSuccessful ) {
                UIUtil.invokeLaterIfNeeded( ( ) -> {
                    QueryTab queryTab = getActiveQueryTab( );
                    if ( queryTab != null ) {
                        if (connectedSuccessful) {
                            queryTab.getQuerySettingsPanel().getInputDocumentArea().reloadDocumentAreas(settings);
                        }
                        setQueryTabEnabled(queryTab, true);
                    }
                } );
            }
        } );
        messageBusConnection.subscribe(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC, (PrepareQueryExecutionNotifier) () -> {
            QueryTab queryTab = getActiveQueryTab();
            if (queryTab != null) {
                NscaleQueryInformation queryInformation = buildQueryInformation(queryTab);
                if (queryInformation != null) {
                    StartQueryExecutionNotifier notifier = messageBus.syncPublisher(StartQueryExecutionNotifier.START_QUERY_EXECUTION_TOPIC);
                    notifier.doAction(queryInformation);
                }
            }
        } );
        messageBusConnection.subscribe( ConnectionsModifiedNotifier.CONNECTION_MODIFIED_TOPIC, new ConnectionsModifiedNotifier( ) {
            @Override
            public void connectionAdded( ConnectionSettings settings ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQuerySettingsPanel( ).getInputSelectedConnection( ).addConnection( settings );
                }
            }

            @Override
            public void connectionModified( ConnectionSettings settings ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQuerySettingsPanel( ).getInputSelectedConnection( ).modifyConnection( settings );
                }
            }

            @Override
            public void connectionRemoved( ConnectionSettings settings ) {
                QueryTab queryTab = getActiveQueryTab();
                if (queryTab != null) {
                    queryTab.getQuerySettingsPanel().getInputSelectedConnection().removeConnection(settings);
                }
            }
        });
        messageBusConnection.subscribe(StartQueryExecutionNotifier.START_QUERY_EXECUTION_TOPIC, (StartQueryExecutionNotifier) queryInformation -> {
            QueryTab queryTab = getActiveQueryTab();
            if (queryTab != null) {
                queryTab.setQueryInformation(queryInformation);
                queryTab.startQueryExecution();
            }
        });
        messageBusConnection.subscribe(DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC, (DocumentAreaChangedNotifier) documentAreaName -> {
            QueryTab queryTab = getActiveQueryTab();
            if (queryTab != null) {
                queryTab.getQueryOptionsTabbedPane().getInputMasterdataScope().reloadMasterdataScopes(documentAreaName);
            }
        });
        messageBusConnection.subscribe(RootResourceIdChangedNotifier.ROOT_RESOURCE_ID_CHANGED_TOPIC, (RootResourceIdChangedNotifier) documentAreaName -> {
            QueryTab queryTab = getActiveQueryTab();
            if (queryTab != null) {
                queryTab.getQueryOptionsTabbedPane().getInputMasterdataScope().reloadMasterdataScopes(documentAreaName);
                PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher(PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC);
                notifier.doAction();
            }
        });
        messageBusConnection.subscribe(FontSettingsChangedNotifier.FONT_SETTINGS_CHANGED_TOPIC, (FontSettingsChangedNotifier) () -> {
            QueryTab queryTab = getActiveQueryTab();
            if (queryTab != null) {
                queryTab.getQueryResultTablePanel().getQueryResultTable().fontSettingsChanged();
                queryTab.getQueryTextboxPanel().getQueryTextbox().fontSettingsChanged();
            }
        });
    }

    /**
     * enable or disable all input panels of a query tab
     */
    private void setQueryTabEnabled(QueryTab queryTab, boolean enabled) {
        queryTab.getQueryOptionsTabbedPane().setEnabled(enabled);
        queryTab.getQueryTextboxPanel().setEnabled(enabled);
        queryTab.getQueryResultTablePanel().setEnabled(enabled);
        queryTab.getQuerySettingsPanel().setEnabled(enabled);
    }

    /**
     * collect the UI state of a query tab into the query information needed for execution
     *
     * @param queryTab query tab to read from
     * @return query information, null if the UI state does not allow a query execution
     */
    private NscaleQueryInformation buildQueryInformation(QueryTab queryTab) {
        QuerySettingsPanel querySettingsPanel = queryTab.getQuerySettingsPanel();
        QueryOptionsTabbedPane queryOptionsTabbedPane = queryTab.getQueryOptionsTabbedPane();
        QueryTextboxPanel queryTextboxPanel = queryTab.getQueryTextboxPanel();

        if (querySettingsPanel.getInputDocumentArea().getSelectedIndex() == -1 || querySettingsPanel.getInputSelectedConnection().getSelectedIndex() == -1) {
            Notifier.warning("no connection or/and no document area selected");
            return null;
        }

        NscaleQueryInformation queryInformation = new NscaleQueryInformation();
        queryInformation.setQueryMode(QueryMode.REPOSITORY);
        queryInformation.setConnectionSettings((ConnectionSettings) querySettingsPanel.getInputSelectedConnection().getSelectedItem());
        queryInformation.setDocumentAreaName((String) querySettingsPanel.getInputDocumentArea().getSelectedItem());

        MasterdataScope masterdataScope = (MasterdataScope) queryOptionsTabbedPane.getInputMasterdataScope().getSelectedItem();
        if (masterdataScope != null) {
            queryInformation.setMasterdataScope(masterdataScope.getAreaName());
        }

        queryInformation.setRepositoryRoot(queryOptionsTabbedPane.getInputRepositoryRoot().getText());
        queryInformation.setNqlQuery(queryTextboxPanel.getQueryTextbox().getText());
        queryInformation.setQueryType(QueryType.DEFAULT);
        boolean aggregate = queryOptionsTabbedPane.getInputAggregate().isSelected();
        boolean version = queryOptionsTabbedPane.getInputVersion().isSelected();
        boolean principalAggregate = queryOptionsTabbedPane.getInputPrincipalsAggregate().isSelected();

        String tabTitle = queryOptionsTabbedPane.getTitleAt(queryOptionsTabbedPane.getSelectedIndex());
        switch (tabTitle) {
            case "Repository" -> {
                if (aggregate && version) {
                    queryInformation.setQueryType(QueryType.AGGREGATE_AND_VERSION);
                } else if (aggregate) {
                    queryInformation.setQueryType(QueryType.AGGREGATE);
                } else if (version) {
                    queryInformation.setQueryType(QueryType.VERSION);
                }
                queryInformation.setQueryMode(QueryMode.REPOSITORY);
            }
            case "BPNM" -> queryInformation.setQueryMode(QueryMode.BPNM);
            case "Masterdata" -> {
                queryInformation.setQueryMode(QueryMode.MASTERDATA);
                if (StringUtils.isEmpty(queryInformation.getMasterdataScope())) {
                    Notifier.warning("masterdata scope not selected, query execution stopped");
                    return null;
                }
            }
            case "Principals" -> {
                if (principalAggregate) {
                    queryInformation.setQueryType(QueryType.AGGREGATE);
                }
                queryInformation.setQueryMode(QueryMode.PRINCIPALS);
            }
            case "Workflow" -> queryInformation.setQueryMode(QueryMode.WORKFLOW);
        }

        return queryInformation;
    }
}
