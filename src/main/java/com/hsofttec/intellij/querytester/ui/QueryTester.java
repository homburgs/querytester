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

import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.QueryType;
import com.hsofttec.intellij.querytester.listeners.HistoryModifiedEventListener;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;
import com.hsofttec.intellij.querytester.models.HistoryComboBoxModel;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.services.HistorySettingsService;
import com.hsofttec.intellij.querytester.ui.components.*;
import com.hsofttec.intellij.querytester.ui.notifiers.*;
import com.hsofttec.intellij.querytester.utils.QueryTab;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class QueryTester extends SimpleToolWindowPanel {
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );
    private static final ProjectManager projectManager = ProjectManager.getInstance( );
    private static final HistorySettingsService HISTORY_SETTINGS_SERVICE = HistorySettingsService.getSettings( projectManager.getOpenProjects( )[ 0 ] );
    private final Project project;
    private final MessageBus messageBus;
    private final MessageBusConnection messageBusConnection;

    private JPanel mainPanel;
    private ConnectionSelect inputSelectedConnection;
    private JComboBox<String> inputHistory;
    private RepositoryRootTextField inputRepositoryRoot;
    private DocumentAreaSelect inputDocumentArea;
    private JBTabbedPane tabbedPane;
    private JCheckBox inputAggregate;
    private JCheckBox inputVersion;
    private MasterdataScopeSelect inputMasterdataScope;
    private ReconnectIcon iconReconnect;
    private QueryTabbedPane queryTabbedPane;

    public QueryTester( Project project ) {
        super( false, true );
        this.project = project;
        messageBus = project.getMessageBus( );
        messageBusConnection = messageBus.connect( );

        subscribeNotifications( );

        setToolbar( createToolBar( ) );
        setContent( createUIComponents( ) );

        inputSelectedConnection.reloadItems( );

        ResultTableContextMenu resultTableContextMenu = new ResultTableContextMenu( project );
        QueryTab activeQueryTab = queryTabbedPane.getActiveQueryTab( );
        if ( activeQueryTab != null ) {
            activeQueryTab.getQueryResultTable( ).setComponentPopupMenu( resultTableContextMenu );
        }

        resultTableContextMenu.setSelectParentFolderListener( new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                QueryTab activeQueryTab = queryTabbedPane.getActiveQueryTab( );
                if ( activeQueryTab != null ) {
                    BasicDynaBean basicDynaBean = ( BasicDynaBean ) activeQueryTab.getQueryResultTable( ).getValueAt( activeQueryTab.getQueryResultTable( ).getSelectedRow( ), activeQueryTab.getQueryResultTable( ).getSelectedColumn( ) );
                    String parentResourceId = ( String ) basicDynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY );
                    inputRepositoryRoot.setText( parentResourceId );
                    PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                    notifier.doAction( );
                }
            }
        } );

        resultTableContextMenu.setSearchFromParentFolderListener( new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                QueryTab activeQueryTab = queryTabbedPane.getActiveQueryTab( );
                if ( activeQueryTab != null ) {
                    BasicDynaBean basicDynaBean = ( BasicDynaBean ) activeQueryTab.getQueryResultTable( ).getValueAt( activeQueryTab.getQueryResultTable( ).getSelectedRow( ), activeQueryTab.getQueryResultTable( ).getSelectedColumn( ) );
                    String resourceId = ( String ) basicDynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY );
                    BaseResource baseResource = CONNECTION_SERVICE.getBaseResource( resourceId );
                    inputRepositoryRoot.setText( baseResource.getParentresourceid( ) );
                    PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                    notifier.doAction( );
                }
            }
        } );

        inputHistory.setModel( new HistoryComboBoxModel( HISTORY_SETTINGS_SERVICE.getQueryList( ) ) );
        inputHistory.setMaximumSize( inputHistory.getPreferredSize( ) );
        inputHistory.addActionListener( actionEvent -> {
            String selectedItem = ( String ) inputHistory.getSelectedItem( );
            if ( StringUtils.isNotBlank( selectedItem ) ) {
                QueryTab activeQueryTab1 = queryTabbedPane.getActiveQueryTab( );
                if ( activeQueryTab1 != null ) {
                    activeQueryTab1.getQueryTextbox( ).setText( selectedItem );
                }
            }
        } );

        HISTORY_SETTINGS_SERVICE.addListener( new HistoryModifiedEventListener( ) {
            @Override
            public void notifyAdd( String query ) {
                inputHistory.addItem( query );
            }

            @Override
            public void notifyRemove( String query ) {
            }
        } );

    }

    /**
     * subscribe diverse notifiers for message bus.
     */
    private void subscribeNotifications( ) {
        messageBusConnection.subscribe( CheckServerConnectionNotifier.CHECK_SERVER_CONNECTION_TOPIC, new CheckServerConnectionNotifier( ) {
            @Override
            public void beforeAction( ConnectionSettings settings ) {
                UIUtil.invokeLaterIfNeeded( ( ) -> {
                    inputSelectedConnection.setEnabled( false );
                    queryTabbedPane.setEnabled( false );
                    inputHistory.setEnabled( false );
                    inputDocumentArea.setEnabled( false );
                    iconReconnect.setEnabled( false );
                    inputAggregate.setEnabled( false );
                    inputMasterdataScope.setEnabled( false );
                    inputRepositoryRoot.setEnabled( false );
                } );
            }

            @Override
            public void afterAction( ConnectionSettings settings, boolean connectedSuccessful ) {
                UIUtil.invokeLaterIfNeeded( ( ) -> {
                    inputSelectedConnection.setEnabled( true );
                    queryTabbedPane.setEnabled( true );
                    inputHistory.setEnabled( true );
                    inputDocumentArea.setEnabled( true );
                    iconReconnect.setEnabled( true );
                    inputAggregate.setEnabled( true );
                    inputMasterdataScope.setEnabled( true );
                    inputRepositoryRoot.setEnabled( true );

                    if ( connectedSuccessful ) {
                        inputDocumentArea.reloadDocumentAreas( settings );
                    }
                } );
            }
        } );
        messageBusConnection.subscribe( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC, ( ) -> {
            QueryMode queryMode = QueryMode.REPOSITORY;

            if ( inputDocumentArea.getSelectedIndex( ) == -1 || inputSelectedConnection.getSelectedIndex( ) == -1 ) {
                Notifier.warning( "no connection or/and no document area selected" );
                return;
            }

            ConnectionSettings connectionSettings = ( ConnectionSettings ) inputSelectedConnection.getSelectedItem( );
            String documentAreaName = ( String ) inputDocumentArea.getSelectedItem( );
            String masterdataScope = ( String ) inputMasterdataScope.getSelectedItem( );
            String repositoryRoot = inputRepositoryRoot.getText( );
            String nqlQuery = queryTabbedPane.getActiveQueryTab( ).getQueryTextbox( ).getText( );
            QueryType queryType = QueryType.DEFAULT;
            boolean aggregate = inputAggregate.isSelected( );
            boolean version = inputVersion.isSelected( );

            if ( aggregate && version ) {
                queryType = QueryType.AGGREGATE_AND_VERSION;
            } else if ( aggregate ) {
                queryType = QueryType.AGGREGATE;
            } else if ( version ) {
                queryType = QueryType.VERSION;
            }

            String tabTitle = tabbedPane.getTitleAt( tabbedPane.getSelectedIndex( ) );
            switch ( tabTitle ) {
                case "Repository":
                    queryMode = QueryMode.REPOSITORY;
                    break;
                case "BPNM":
                    queryMode = QueryMode.BPNM;
                    break;
                case "Masterdata":
                    queryMode = QueryMode.MASTERDATA;
                    if ( StringUtils.isEmpty( masterdataScope ) ) {
                        Notifier.warning( "masterdata scope not selected, query execution stopped" );
                        return;
                    }
                    break;
                case "Principals":
                    queryMode = QueryMode.PRINCIPALS;
                    break;
                case "Workflow":
                    queryMode = QueryMode.WORKFLOW;
                    break;
            }

            StartQueryExecutionNotifier notifier = messageBus.syncPublisher( StartQueryExecutionNotifier.START_QUERY_EXECUTION_TOPIC );
            notifier.doAction( connectionSettings, queryMode, documentAreaName, masterdataScope, repositoryRoot, nqlQuery, queryType );
        } );
        messageBusConnection.subscribe( ConnectionsModifiedNotifier.CONNECTION_MODIFIED_TOPIC, new ConnectionsModifiedNotifier( ) {
            @Override
            public void connectionAdded( ConnectionSettings settings ) {
                inputSelectedConnection.addConnection( settings );
            }

            @Override
            public void connectionModified( ConnectionSettings settings ) {
                inputSelectedConnection.modifyConnection( settings );
            }

            @Override
            public void connectionRemoved( ConnectionSettings settings ) {
                inputSelectedConnection.removeConnection( settings );
            }
        } );
        messageBusConnection.subscribe( StartQueryExecutionNotifier.START_QUERY_EXECUTION_TOPIC, new StartQueryExecutionNotifier( ) {
            @Override
            public void doAction( ConnectionSettings connectionSettings, QueryMode queryMode, String documentAreaName, String masterdataScope, String rootResourceId, String nqlQuery, QueryType queryType ) {
                NscaleTable queryResultTable = queryTabbedPane.getActiveQueryTab( ).getQueryResultTable( );
                queryResultTable.startQueryExecution( connectionSettings, queryMode, documentAreaName, masterdataScope, rootResourceId, nqlQuery, queryType );
            }
        } );
        messageBusConnection.subscribe( DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC, new DocumentAreaChangedNotifier( ) {
            @Override
            public void doAction( String documentAreaName ) {
                inputMasterdataScope.reloadMasterdataScopes( documentAreaName );
            }
        } );
        messageBusConnection.subscribe( RootResourceIdChangedNotifier.ROOT_RESOURCE_ID_CHANGED_TOPIC, new RootResourceIdChangedNotifier( ) {
            @Override
            public void doAction( String rootResourceId ) {
                inputRepositoryRoot.setText( rootResourceId );
                PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                notifier.doAction( );
            }
        } );
        messageBusConnection.subscribe( OptimizeTableHeaderWidthNotifier.OPTIMIZE_TABLE_HEADER_WIDTH_TOPIC, new OptimizeTableHeaderWidthNotifier( ) {
            @Override
            public void doAction( ) {
                QueryTab activeQueryTab = queryTabbedPane.getActiveQueryTab( );
                activeQueryTab.getQueryResultTable( ).calcHeaderWidth( );
            }
        } );
        messageBusConnection.subscribe( FontSettingsChangedNotifier.FONT_SETTINGS_CHANGED_TOPIC, new FontSettingsChangedNotifier( ) {
            @Override
            public void doAction( ) {
                NscaleTable queryResultTable = queryTabbedPane.getActiveQueryTab( ).getQueryResultTable( );
                NqlQueryTextbox queryTextbox = queryTabbedPane.getActiveQueryTab( ).getQueryTextbox( );
                queryResultTable.fontSettingsChanged( );
                queryTextbox.fontSettingsChanged( );
            }
        } );
    }

    private JComponent createToolBar( ) {
        DefaultActionGroup actionGroup = new DefaultActionGroup( );

        actionGroup.add( new AnAction( "Execute Active Query", "The active query execution", AllIcons.RunConfigurations.TestState.Run ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent e ) {
                PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                notifier.doAction( );
            }
        } );

        actionGroup.add( new AnAction( "Add Query Tab", "Open a new query tab", AllIcons.Ide.Rating ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent e ) {
                queryTabbedPane.createQueryTab( ( String ) inputDocumentArea.getSelectedItem( ) );
            }
        } );

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
                    ConnectionsModifiedNotifier notifier = messageBus.syncPublisher( ConnectionsModifiedNotifier.CONNECTION_MODIFIED_TOPIC );
                    notifier.connectionAdded( connectionSettings );
                }
            }
        } );

        actionGroup.add( new AnAction( "Plugin Settings", "Plugin Settings for nscale QueryTester", AllIcons.General.Settings ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent e ) {
                ShowSettingsUtil.getInstance( ).showSettingsDialog( projectManager.getOpenProjects( )[ 0 ], "QueryTester" );
            }
        } );

        ActionToolbar actionToolbar = ActionManager.getInstance( ).createActionToolbar( ActionPlaces.TOOLBAR, actionGroup, true );
        actionToolbar.setTargetComponent( mainPanel );

        return actionToolbar.getComponent( );
    }

    private JComponent createUIComponents( ) {
        mainPanel = new JPanel( new BorderLayout( -1, -1 ) );

        JBSplitter mainSplitter = new OnePixelSplitter( false, 0.8f );
        mainSplitter.setHonorComponentsMinimumSize( true );
        mainSplitter.setSplitterProportionKey( "main.splitter.key" );

        JPanel leftPanel = new JBPanel<>( new BorderLayout( 3, 3 ) );
        JPanel rightPanel = new JBPanel<>( new BorderLayout( 3, 3 ) );
        rightPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        leftPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

        queryTabbedPane = new QueryTabbedPane( project );
        queryTabbedPane.createQueryTab( null );

        mainSplitter.setFirstComponent( queryTabbedPane );
        mainSplitter.setSecondComponent( rightPanel );

        mainPanel.add( mainSplitter );

        rightPanel.add( createFormPanel( rightPanel ), BorderLayout.NORTH );
        rightPanel.add( createTabPanel( ), BorderLayout.CENTER, 1 );

        return mainPanel;
    }


    private JComponent createTabPanel( ) {
        CellConstraints cc = new CellConstraints( );
        FormLayout formLayout = new FormLayout(
                "5px, left:pref, 4dlu, pref:grow",
                "pref, 3dlu, pref, 3dlu, pref"
        );
        JPanel repositoryPanel = new JPanel( formLayout );
        JPanel masterdataPanel = new JPanel( formLayout );

        inputRepositoryRoot = new RepositoryRootTextField( project );
        repositoryPanel.add( new JBLabel( "Root resource" ), cc.xy( 2, 1 ) );
        repositoryPanel.add( inputRepositoryRoot, cc.xy( 4, 1 ) );

        inputAggregate = new JBCheckBox( "Aggregate" );
        repositoryPanel.add( inputAggregate, cc.xy( 4, 3 ) );

        inputVersion = new JBCheckBox( "Version" );
        repositoryPanel.add( inputVersion, cc.xy( 4, 5 ) );
//        inputVersion.addItemListener( itemEvent -> {
//            if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
//                inputAggregate.setSelected( false );
//                inputAggregate.setEnabled( false );
//            } else {
//                inputAggregate.setEnabled( true );
//            }
//        } );

        inputMasterdataScope = new MasterdataScopeSelect( project );
        masterdataPanel.add( new JBLabel( "Scope" ), cc.xy( 2, 1 ) );
        masterdataPanel.add( inputMasterdataScope, cc.xy( 4, 1 ) );

        tabbedPane = new JBTabbedPane( );
        tabbedPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        tabbedPane.addTab( "Repository", repositoryPanel );
        tabbedPane.addTab( "Masterdata", masterdataPanel );
        tabbedPane.setBorder( BorderFactory.createEtchedBorder( ) );
        return tabbedPane;
    }

    private JComponent createFormPanel( JPanel panel ) {
        int formRow;
        int[] formColNums = { 2, 4, 6 };
        CellConstraints cc = new CellConstraints( );
        FormLayout formLayout = new FormLayout(
                "5px, left:pref, 4dlu, pref:grow, 4dlu, pref,5px",
                "pref, 3dlu, pref, 3dlu, pref"
        );
        JPanel formPanel = new JPanel( formLayout );
        panel.add( formPanel, BorderLayout.NORTH );
        formPanel.setBorder( BorderFactory.createEtchedBorder( ) );

        formRow = 1;
        inputSelectedConnection = new ConnectionSelect( project );
        iconReconnect = new ReconnectIcon( inputSelectedConnection );

        formPanel.add( new JBLabel( "Connection" ), cc.xy( formColNums[ 0 ], formRow ) );
        formPanel.add( inputSelectedConnection, cc.xy( formColNums[ 1 ], formRow ) );
        formPanel.add( iconReconnect, cc.xy( formColNums[ 2 ], formRow ) );

        formRow += 2;
        inputDocumentArea = new DocumentAreaSelect( project );
        formPanel.add( new JBLabel( "Document area" ), cc.xy( formColNums[ 0 ], formRow ) );
        formPanel.add( inputDocumentArea, cc.xyw( formColNums[ 1 ], formRow, 3 ) );

        formRow += 2;
        inputHistory = new HistorySelect( project );
        inputHistory.setPrototypeDisplayValue( "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" );
        formPanel.add( new JBLabel( "History" ), cc.xy( formColNums[ 0 ], formRow ) );
        formPanel.add( inputHistory, cc.xyw( formColNums[ 1 ], formRow, 3 ) );

        return formPanel;
    }
}
