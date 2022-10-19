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

package com.hsofttec.intellij.querytester.ui;

import com.ceyoniq.nscale.al.core.cfg.MasterdataScope;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryType;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;
import com.hsofttec.intellij.querytester.models.NscaleQueryInformation;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.actions.AddQueryTabAction;
import com.hsofttec.intellij.querytester.ui.actions.ExecuteActiveQueryAction;
import com.hsofttec.intellij.querytester.ui.actions.ShowPluginSettingsAction;
import com.hsofttec.intellij.querytester.ui.components.*;
import com.hsofttec.intellij.querytester.ui.notifiers.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;

public class QueryTester extends SimpleToolWindowPanel {
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );

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
        actionGroup.add( new AnAction( "Add Connection", "Show the connection settings dialog", AllIcons.General.Add ) {
            @Override
            public void actionPerformed( AnActionEvent actionEvent ) {
                Project currentProject = actionEvent.getProject( );
                ConnectionSettings connectionSettings = new ConnectionSettings( );
                ConnectionSetupDialog connectionSetupDialog = new ConnectionSetupDialog( currentProject );
                connectionSetupDialog.setData( connectionSettings );
                if ( connectionSetupDialog.showAndGet( ) ) {
                    connectionSettings = connectionSetupDialog.getData( );
                    connectionSettingsService.connectionSettingsState.connectionSettings.add( connectionSettings );
                    ConnectionsModifiedNotifier notifier = project.getMessageBus( ).syncPublisher( ConnectionsModifiedNotifier.CONNECTION_MODIFIED_TOPIC );
                    notifier.connectionAdded( connectionSettings );
                }
            }
        } );
        actionGroup.add( new ShowPluginSettingsAction( this ) );
        ActionToolbar actionToolbar = ActionManager.getInstance( ).createActionToolbar( ActionPlaces.TOOLBAR, actionGroup, true );
        actionToolbar.setTargetComponent( queryTabbedPane );
        return actionToolbar.getComponent( );
    }

    /**
     * create all GUI components
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
                        queryTab.getQueryResultTable( ).setEnabled( false );
                        queryTab.getQueryTextboxPanel( ).setEnabled( false );
                        queryTab.getQueryOptionsTabbedPane( ).setEnabled( false );
                    }
                } );
            }

            @Override
            public void afterAction( ConnectionSettings settings, boolean connectedSuccessful ) {
                UIUtil.invokeLaterIfNeeded( ( ) -> {
                    QueryTab queryTab = getActiveQueryTab( );
                    if ( queryTab != null ) {
                        if ( connectedSuccessful ) {
                            queryTab.getQuerySettingsPanel( ).getInputDocumentArea( ).reloadDocumentAreas( settings );
                        }
                        queryTab.getQueryResultTable( ).setEnabled( true );
                        queryTab.getQueryTextboxPanel( ).setEnabled( true );
                        queryTab.getQueryOptionsTabbedPane( ).setEnabled( true );
                    }
                } );
            }
        } );
        messageBusConnection.subscribe( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC, ( ) -> {
            QueryTab queryTab = getActiveQueryTab( );
            if ( queryTab != null ) {
                QuerySettingsPanel querySettingsPanel = queryTab.getQuerySettingsPanel( );
                QueryOptionsTabbedPane queryOptionsTabbedPane = queryTab.getQueryOptionsTabbedPane( );
                QueryTextboxPanel queryTextboxPanel = queryTab.getQueryTextboxPanel( );
                NscaleQueryInformation queryInformation = new NscaleQueryInformation( );
                queryInformation.setQueryMode( QueryMode.REPOSITORY );

                if ( querySettingsPanel.getInputDocumentArea( ).getSelectedIndex( ) == -1 || querySettingsPanel.getInputSelectedConnection( ).getSelectedIndex( ) == -1 ) {
                    Notifier.warning( "no connection or/and no document area selected" );
                    return;
                }

                queryInformation.setConnectionSettings( ( ConnectionSettings ) querySettingsPanel.getInputSelectedConnection( ).getSelectedItem( ) );
                queryInformation.setDocumentAreaName( ( String ) querySettingsPanel.getInputDocumentArea( ).getSelectedItem( ) );

                MasterdataScope masterdataScope = ( MasterdataScope ) queryOptionsTabbedPane.getInputMasterdataScope( ).getSelectedItem( );
                if ( masterdataScope != null ) {
                    queryInformation.setMasterdataScope( masterdataScope.getAreaName( ) );
                }

                queryInformation.setRepositoryRoot( queryOptionsTabbedPane.getInputRepositoryRoot( ).getText( ) );
                queryInformation.setNqlQuery( queryTextboxPanel.getQueryTextbox( ).getText( ) );
                queryInformation.setQueryType( QueryType.DEFAULT );
                boolean aggregate = queryOptionsTabbedPane.getInputAggregate( ).isSelected( );
                boolean version = queryOptionsTabbedPane.getInputVersion( ).isSelected( );

                if ( aggregate && version ) {
                    queryInformation.setQueryType( QueryType.AGGREGATE_AND_VERSION );
                } else if ( aggregate ) {
                    queryInformation.setQueryType( QueryType.AGGREGATE );
                } else if ( version ) {
                    queryInformation.setQueryType( QueryType.VERSION );
                }

                String tabTitle = queryOptionsTabbedPane.getTitleAt( queryOptionsTabbedPane.getSelectedIndex( ) );
                switch ( tabTitle ) {
                    case "Repository":
                        queryInformation.setQueryMode( QueryMode.REPOSITORY );
                        break;
                    case "BPNM":
                        queryInformation.setQueryMode( QueryMode.BPNM );
                        break;
                    case "Masterdata":
                        queryInformation.setQueryMode( QueryMode.MASTERDATA );
                        if ( StringUtils.isEmpty( queryInformation.getMasterdataScope( ) ) ) {
                            Notifier.warning( "masterdata scope not selected, query execution stopped" );
                            return;
                        }
                        break;
                    case "Principals":
                        queryInformation.setQueryMode( QueryMode.PRINCIPALS );
                        break;
                    case "Workflow":
                        queryInformation.setQueryMode( QueryMode.WORKFLOW );
                        break;
                }

                StartQueryExecutionNotifier notifier = messageBus.syncPublisher( StartQueryExecutionNotifier.START_QUERY_EXECUTION_TOPIC );
                notifier.doAction( queryInformation );
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
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQuerySettingsPanel( ).getInputSelectedConnection( ).removeConnection( settings );
                }
            }
        } );
        messageBusConnection.subscribe( StartQueryExecutionNotifier.START_QUERY_EXECUTION_TOPIC, new StartQueryExecutionNotifier( ) {
            @Override
            public void doAction( NscaleQueryInformation queryInformation ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.setQueryInformation( queryInformation );
                    queryTab.startQueryExecution( );
                }
            }
        } );
        messageBusConnection.subscribe( DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC, new DocumentAreaChangedNotifier( ) {
            @Override
            public void doAction( String documentAreaName ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQueryOptionsTabbedPane( ).getInputMasterdataScope( ).reloadMasterdataScopes( documentAreaName );
                }
            }
        } );
        messageBusConnection.subscribe( RootResourceIdChangedNotifier.ROOT_RESOURCE_ID_CHANGED_TOPIC, new RootResourceIdChangedNotifier( ) {
            @Override
            public void doAction( String documentAreaName ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQueryOptionsTabbedPane( ).getInputMasterdataScope( ).reloadMasterdataScopes( documentAreaName );
                    PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                    notifier.doAction( );
                }
            }
        } );
        messageBusConnection.subscribe( OptimizeTableHeaderWidthNotifier.OPTIMIZE_TABLE_HEADER_WIDTH_TOPIC, new OptimizeTableHeaderWidthNotifier( ) {
            @Override
            public void doAction( ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQueryResultTablePanel( ).getQueryResultTable( ).calcHeaderWidth( );
                }
            }
        } );
        messageBusConnection.subscribe( IncrementTableHeaderWidthNotifier.INCREMENT_TABLE_HEADER_WIDTH_TOPIC, new IncrementTableHeaderWidthNotifier( ) {
            @Override
            public void doAction( ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQueryResultTablePanel( ).getQueryResultTable( ).incrementHeaderWidth( );
                }
            }
        } );
        messageBusConnection.subscribe( FontSettingsChangedNotifier.FONT_SETTINGS_CHANGED_TOPIC, new FontSettingsChangedNotifier( ) {
            @Override
            public void doAction( ) {
                QueryTab queryTab = getActiveQueryTab( );
                if ( queryTab != null ) {
                    queryTab.getQueryResultTablePanel( ).getQueryResultTable( ).fontSettingsChanged( );
                    queryTab.getQueryTextboxPanel( ).getQueryTextbox( ).fontSettingsChanged( );
                }
            }
        } );
    }
}
