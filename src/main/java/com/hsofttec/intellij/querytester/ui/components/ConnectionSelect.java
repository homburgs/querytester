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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.*;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;
import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionSelect extends ComboBox<ConnectionSettings> implements ItemListener {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ConnectionSettingsService CONNECTION_SETTINGS_SERVICE = ConnectionSettingsService.getSettings( );
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );
    private final Project project;

    public ConnectionSelect( Project project ) {
        EVENT_BUS.register( this );
        this.project = project;
        this.addItemListener( this );
        setRenderer( new ConnectionListCellRenderer( ) );
    }

    public void reloadItems( ) {
        List<ConnectionSettings> connectionSettings = CONNECTION_SETTINGS_SERVICE.connectionSettingsState.connectionSettings.stream( )
                .filter( ConnectionSettings::isActive ).collect( Collectors.toList( ) );

        setModel( new CollectionComboBoxModel<>( connectionSettings ) );

        ConnectionSettings settings = ( ConnectionSettings ) getSelectedItem( );
        if ( settings != null ) {
            checkOnlineState( settings );
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
        EVENT_BUS.post( new CheckServerConnectionEvent( ) );
        ProgressManager progressManager = ProgressManager.getInstance( );
        progressManager.runProcessWithProgressSynchronously( ( ) -> {
            final ProgressIndicator progressIndicator = progressManager.getProgressIndicator( );
            progressIndicator.setText( String.format( "Testing server '%s'", settings.getConnectionName( ) ) );
            if ( CONNECTION_SERVICE.isConnectionUsable( settings ) ) {
                EVENT_BUS.post( new ConnectionSelectionChangedEvent( settings ) );
            } else {
                EVENT_BUS.post( new ConnectionSelectionChangedEvent( null ) );
            }
            EVENT_BUS.post( new CheckedServerConnectionEvent( ) );
        }, "Testing connection", false, project, this );
    }

    @Subscribe
    public void connectionSettingsChanged( ConnectionChangedEvent event ) {
        boolean foundInSelectBox = false;
        ConnectionSettings changedSettings = event.getData( );
        CollectionComboBoxModel<ConnectionSettings> model = ( CollectionComboBoxModel<ConnectionSettings> ) getModel( );
        for ( ConnectionSettings item : model.getItems( ) ) {
            if ( item.getId( ).equals( changedSettings.getId( ) ) ) {
                foundInSelectBox = true;
                if ( !changedSettings.isActive( ) ) {
                    removeItem( item );
                    return;
                } else {
                    item.setUsername( changedSettings.getUsername( ) );
                    item.setPassword( changedSettings.getPassword( ) );
                    item.setSsl( changedSettings.isSsl( ) );
                    item.setServer( changedSettings.getServer( ) );
                    item.setInstance( changedSettings.getInstance( ) );
                    item.setPort( changedSettings.getPort( ) );
                    item.setConnectionName( changedSettings.getConnectionName( ) );
                    item.setTimeout( changedSettings.getTimeout( ) );
                    item.setTimeout( changedSettings.getTimeout( ) );
                }
            }
        }

        if ( !foundInSelectBox && changedSettings.isActive( ) ) {
            EVENT_BUS.post( new ConnectionAddedEvent( changedSettings ) );
        }
    }

    @Subscribe
    public void connectionSettingsAdded( ConnectionAddedEvent event ) {
        addItem( event.getData( ) );
        if ( getItemCount( ) == 1 ) {
            setSelectedIndex( 0 );
            EVENT_BUS.post( new ConnectionSelectionChangedEvent( ( ConnectionSettings ) getSelectedItem( ) ) );
        }
    }

    @Subscribe
    public void connectionSettingsRemoved( ConnectionRemovedEvent event ) {
        removeItem( event.getData( ) );
    }

}
