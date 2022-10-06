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

import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.ConnectionSelectionChangedEvent;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;
import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.hsofttec.intellij.querytester.ui.notifiers.CheckServerConnectionNotifier;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.messages.MessageBus;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionSelect extends ComboBox<ConnectionSettings> implements ItemListener {
    private static final ConnectionSettingsService CONNECTION_SETTINGS_SERVICE = ConnectionSettingsService.getSettings( );
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );
    private final Project project;
    private final MessageBus messageBus;
    private final List<ConnectionSettings> connections = new ArrayList<>( );

    public ConnectionSelect( Project project ) {
        this.project = project;
        messageBus = project.getMessageBus( );
        this.addItemListener( this );
        setModel( new CollectionComboBoxModel<>( connections ) );
        setRenderer( new ConnectionListCellRenderer( ) );
    }

    public void reloadItems( ) {
        List<ConnectionSettings> connectionSettings = CONNECTION_SETTINGS_SERVICE.connectionSettingsState.connectionSettings.stream( )
                .filter( ConnectionSettings::isActive ).collect( Collectors.toList( ) );

        connections.clear( );
        if ( !connectionSettings.isEmpty( ) ) {
            connections.addAll( connectionSettings );
            setSelectedIndex( 0 );
            ConnectionSettings settings = ( ConnectionSettings ) getSelectedItem( );
            if ( settings != null ) {
                checkOnlineState( settings );
            }
        }

    }

    @Override
    public void itemStateChanged( ItemEvent itemEvent ) {
        if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
            ConnectionSettings settings = ( ConnectionSettings ) getSelectedItem( );
            if ( settings != null ) {
                checkOnlineState( settings );
            }
        }
    }

    public void checkOnlineState( ConnectionSettings settings ) {
        MessageBus messageBus = project.getMessageBus( );
        CheckServerConnectionNotifier checkServerConnectionNotifier = messageBus.syncPublisher( CheckServerConnectionNotifier.CHECK_SERVER_CONNECTION_TOPIC );
        checkServerConnectionNotifier.beforeAction( settings );
        ProgressManager progressManager = ProgressManager.getInstance( );
        progressManager.runProcessWithProgressSynchronously( ( ) -> {
            final ProgressIndicator progressIndicator = progressManager.getProgressIndicator( );
            progressIndicator.setText( String.format( "Testing server '%s'", settings.getConnectionName( ) ) );
            boolean connectionUsable = CONNECTION_SERVICE.isConnectionUsable( settings );
            checkServerConnectionNotifier.afterAction( settings, connectionUsable );
        }, "Testing connection", false, project, this );
    }

    public void modifyConnection( ConnectionSettings settings ) {
        CollectionComboBoxModel<ConnectionSettings> model = ( CollectionComboBoxModel<ConnectionSettings> ) getModel( );
        for ( ConnectionSettings item : model.getItems( ) ) {
            if ( item.getId( ).equals( settings.getId( ) ) ) {
                item.setUsername( settings.getUsername( ) );
                item.setPassword( settings.getPassword( ) );
                item.setSsl( settings.isSsl( ) );
                item.setServer( settings.getServer( ) );
                item.setInstance( settings.getInstance( ) );
                item.setPort( settings.getPort( ) );
                item.setConnectionName( settings.getConnectionName( ) );
                item.setTimeout( settings.getTimeout( ) );
                item.setTimeout( settings.getTimeout( ) );
            }
        }
    }

    @Subscribe
    public void addConnection( ConnectionSettings settings ) {
        connections.add( settings );
        if ( getItemCount( ) == 1 ) {
            setSelectedIndex( 0 );
            EventBusFactory.getInstance( ).get( ).post( new ConnectionSelectionChangedEvent( ( ConnectionSettings ) getSelectedItem( ) ) );
        }
    }

    public void removeConnection( ConnectionSettings settings ) {
        connections.remove( settings );
    }
}
