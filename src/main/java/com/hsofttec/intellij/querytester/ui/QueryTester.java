package com.hsofttec.intellij.querytester.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.completion.NqlCompletionProvider;
import com.hsofttec.intellij.querytester.events.ConnectionAddedEvent;
import com.hsofttec.intellij.querytester.events.PrepareQueryExecutionEvent;
import com.hsofttec.intellij.querytester.events.StartQueryExecutionEvent;
import com.hsofttec.intellij.querytester.listeners.HistoryModifiedEventListener;
import com.hsofttec.intellij.querytester.models.HistoryComboBoxModel;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.services.HistorySettingsService;
import com.hsofttec.intellij.querytester.ui.components.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class QueryTester extends SimpleToolWindowPanel {
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );
    private static final ConnectionService connectionService = ConnectionService.getInstance( );
    private static final ProjectManager projectManager = ProjectManager.getInstance( );
    private static final HistorySettingsService HISTORY_SETTINGS_SERVICE = HistorySettingsService.getSettings( ProjectManager.getInstance( ).getOpenProjects( )[ 0 ] );
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );

    private JPanel mainPanel;
    private ConnectionSelect inputSelectedConnection;
    private NscaleTable queryResultTable;
    private NqlQueryTextbox inputNqlQuery;
    private JButton executeButton;
    private JComboBox<String> inputHistory;
    private RepositoryRootTextField inputRepositoryRoot;
    private JLabel labelDocumentArea;
    private DocumentAreaSelect inputDocumentArea;
    private JTabbedPane tabbedPane;
    private JPanel tabRepository;
    private JPanel tabMasterdata;
    private JPanel tabBPMN;
    private JPanel tabPrinicipals;
    private JPanel tabWorkflow;
    private JCheckBox inputeAggregate;
    private JComboBox inputMasterdataScope;

    public QueryTester( ) {
        super( false, true );

        EVENT_BUS.register( this );

        setToolbar( createToolBar( ) );
        setContent( mainPanel );

        ResultTableContextMenu resultTableContextMenu = new ResultTableContextMenu( );
        queryResultTable.setComponentPopupMenu( resultTableContextMenu );

        resultTableContextMenu.setSelectParentFolderListener( new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                BasicDynaBean basicDynaBean = ( BasicDynaBean ) queryResultTable.getValueAt( queryResultTable.getSelectedRow( ), queryResultTable.getSelectedColumn( ) );
                String parentResourceId = ( String ) basicDynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY );
                inputRepositoryRoot.setText( parentResourceId );
                EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
            }
        } );

        executeButton.addActionListener( new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
            }
        } );

        inputHistory.setModel( new HistoryComboBoxModel( HISTORY_SETTINGS_SERVICE.getQueryList( ) ) );
        inputHistory.setMaximumSize( inputHistory.getPreferredSize( ) );
        inputHistory.addActionListener( actionEvent -> {
            String selectedItem = ( String ) inputHistory.getSelectedItem( );
            if ( StringUtils.isNotBlank( selectedItem ) ) {
                inputNqlQuery.setText( selectedItem );
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

    @Subscribe
    public void prepareQueryExecution( PrepareQueryExecutionEvent event ) {
        QueryMode queryMode = QueryMode.REPOSITORY;
        if ( inputDocumentArea.getSelectedIndex( ) == -1 || inputSelectedConnection.getSelectedIndex( ) == -1 ) {
            Notifier.warning( "no connection or/and no document area selected" );
            return;
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
                break;
            case "Principals":
                queryMode = QueryMode.PRINCIPALS;
                break;
            case "Workflow":
                queryMode = QueryMode.WORKFLOW;
                break;
        }

        ConnectionSettingsService.ConnectionSettings connectionSettings = ( ConnectionSettingsService.ConnectionSettings ) inputSelectedConnection.getSelectedItem( );
        String documentAreaName = ( String ) inputDocumentArea.getSelectedItem( );
        String masterdataScope = ( String ) inputMasterdataScope.getSelectedItem( );
        String repositoryRoot = inputRepositoryRoot.getText( );
        String nqlQuery = inputNqlQuery.getText( );
        EVENT_BUS.post( new StartQueryExecutionEvent( connectionSettings, queryMode, documentAreaName, masterdataScope, repositoryRoot, nqlQuery ) );
    }

    private JComponent createToolBar( ) {
        DefaultActionGroup actionGroup = new DefaultActionGroup( );
        actionGroup.add( new AnAction( "Add Connection", "Show the connection settings dialog", AllIcons.General.Add ) {
            @Override
            public void actionPerformed( AnActionEvent actionEvent ) {
                Project currentProject = actionEvent.getProject( );
                ConnectionSettingsService.ConnectionSettings connectionSettings = new ConnectionSettingsService.ConnectionSettings( );
                ConnectionSetupDialog connectionSetupDialog = new ConnectionSetupDialog( currentProject );
                connectionSetupDialog.setData( connectionSettings );
                if ( connectionSetupDialog.showAndGet( ) ) {
                    connectionSetupDialog.getData( connectionSettings );
                    connectionSettingsService.connectionSettingsState.connectionSettings.add( connectionSettings );
                    EVENT_BUS.post( new ConnectionAddedEvent( connectionSettings ) );
                }
            }
        } );

        actionGroup.add( new AnAction( "Plugin Settings", "Plugin Settings for nscale QueryTester", AllIcons.General.Settings ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent e ) {
                ShowSettingsUtil.getInstance( ).showSettingsDialog( projectManager.getOpenProjects( )[ 0 ], "nscale QueryTester Settings" );
            }
        } );

        ActionToolbar actionToolbar = ActionManager.getInstance( ).createActionToolbar( ActionPlaces.MAIN_TOOLBAR, actionGroup, true );

        return actionToolbar.getComponent( );
    }

    private void createUIComponents( ) {
        inputNqlQuery = new NqlQueryTextbox( projectManager.getOpenProjects( )[ 0 ], new NqlCompletionProvider( ), "" );
        inputMasterdataScope = new MasterdataScopeSelect( );
        inputDocumentArea = new DocumentAreaSelect( );
        inputRepositoryRoot = new RepositoryRootTextField( );
        inputSelectedConnection = new ConnectionSelect( );
        queryResultTable = new NscaleTable( );
    }
}
