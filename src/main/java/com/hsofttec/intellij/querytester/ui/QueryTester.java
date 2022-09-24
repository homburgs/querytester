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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.events.ConnectionAddedEvent;
import com.hsofttec.intellij.querytester.events.PrepareQueryExecutionEvent;
import com.hsofttec.intellij.querytester.events.RootResourceIdChangedEvent;
import com.hsofttec.intellij.querytester.events.StartQueryExecutionEvent;
import com.hsofttec.intellij.querytester.listeners.HistoryModifiedEventListener;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.HistoryComboBoxModel;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.services.HistorySettingsService;
import com.hsofttec.intellij.querytester.services.SettingsService;
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
    private static final SettingsState settings = SettingsService.getSettings( );

    private JPanel mainPanel;
    private ConnectionSelect inputSelectedConnection;
    private NscaleTable queryResultTable;
    private NqlQueryTextbox inputNqlQuery;
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
    private JSplitPane mainSplitter;
    private JSplitPane leftSplitPane;
    private JPanel rightPanel;
    private JPanel leftPanel;

    public QueryTester( ) {
        super( false, true );

        EVENT_BUS.register( this );

        setToolbar( createToolBar( ) );
        setContent( mainPanel );
        mainSplitter.setOneTouchExpandable( false );
        leftSplitPane.setOneTouchExpandable( false );
        mainSplitter.setDividerLocation( settings.getLastMainDividerPosition( ) );
        if ( settings.getLastLeftDividerPosition( ) > 0 ) {
            leftSplitPane.setDividerLocation( settings.getLastLeftDividerPosition( ) );
        }
//        mainSplitter.setContinuousLayout( true );
//        mainSplitter.setResizeWeight( .5 );
//        mainSplitter.setDividerSize( 3 );
//        mainSplitter.setBorder( BorderFactory.createEmptyBorder(  ) );
        mainSplitter.addPropertyChangeListener( JSplitPane.DIVIDER_LOCATION_PROPERTY, propertyChangeEvent -> {
            settings.setLastMainDividerPosition( ( Integer ) propertyChangeEvent.getNewValue( ) );
        } );
        leftSplitPane.addPropertyChangeListener( JSplitPane.DIVIDER_LOCATION_PROPERTY, propertyChangeEvent -> {
            settings.setLastLeftDividerPosition( ( Integer ) propertyChangeEvent.getNewValue( ) );
        } );

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

        resultTableContextMenu.setSearchFromParentFolderListener( new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                BasicDynaBean basicDynaBean = ( BasicDynaBean ) queryResultTable.getValueAt( queryResultTable.getSelectedRow( ), queryResultTable.getSelectedColumn( ) );
                String resourceId = ( String ) basicDynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY );
                BaseResource baseResource = connectionService.getBaseResource( resourceId );
                inputRepositoryRoot.setText( baseResource.getParentresourceid( ) );
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

    /**
     * prepare parameters for query execution
     *
     * @param event empty event
     */
    @Subscribe
    public void prepareQueryExecution( PrepareQueryExecutionEvent event ) {
        QueryMode queryMode = QueryMode.REPOSITORY;

        if ( inputDocumentArea.getSelectedIndex( ) == -1 || inputSelectedConnection.getSelectedIndex( ) == -1 ) {
            Notifier.warning( "no connection or/and no document area selected" );
            return;
        }

        ConnectionSettingsService.ConnectionSettings connectionSettings = ( ConnectionSettingsService.ConnectionSettings ) inputSelectedConnection.getSelectedItem( );
        String documentAreaName = ( String ) inputDocumentArea.getSelectedItem( );
        String masterdataScope = ( String ) inputMasterdataScope.getSelectedItem( );
        String repositoryRoot = inputRepositoryRoot.getText( );
        String nqlQuery = inputNqlQuery.getText( );
        boolean aggregate = inputeAggregate.isSelected( );

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

        EVENT_BUS.post( new StartQueryExecutionEvent( connectionSettings, queryMode, documentAreaName, masterdataScope, repositoryRoot, nqlQuery, aggregate ) );
    }

    @Subscribe
    public void rootResourceIdChanged( RootResourceIdChangedEvent event ) {
        inputRepositoryRoot.setText( event.getRootResourceId( ) );
        EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
    }

    private JComponent createToolBar( ) {
        DefaultActionGroup actionGroup = new DefaultActionGroup( );

        actionGroup.add( new AnAction( "Execute Query", "Start query execution", AllIcons.RunConfigurations.TestState.Run ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent e ) {
                EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
            }
        } );

        actionGroup.add( new AnAction( "Add Connection", "Show the connection settings dialog", AllIcons.General.Add ) {
            @Override
            public void actionPerformed( AnActionEvent actionEvent ) {
                Project currentProject = actionEvent.getProject( );
                ConnectionSettingsService.ConnectionSettings connectionSettings = new ConnectionSettingsService.ConnectionSettings( );
                ConnectionSetupDialog connectionSetupDialog = new ConnectionSetupDialog( currentProject );
                connectionSetupDialog.setData( connectionSettings );
                if ( connectionSetupDialog.showAndGet( ) ) {
                    connectionSettings = connectionSetupDialog.getData( );
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

        ActionToolbar actionToolbar = ActionManager.getInstance( ).createActionToolbar( ActionPlaces.TOOLBAR, actionGroup, true );
        actionToolbar.setTargetComponent( mainPanel );

        return actionToolbar.getComponent( );
    }

    private void createUIComponents( ) {
        inputNqlQuery = new NqlQueryTextbox( );
        inputMasterdataScope = new MasterdataScopeSelect( );
        inputDocumentArea = new DocumentAreaSelect( );
        inputSelectedConnection = new ConnectionSelect( );
    }
}
