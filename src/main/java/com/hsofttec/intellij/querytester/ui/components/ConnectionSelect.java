package com.hsofttec.intellij.querytester.ui.components;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.ConnectionAddedEvent;
import com.hsofttec.intellij.querytester.events.ConnectionSelectionChangedEvent;
import com.hsofttec.intellij.querytester.models.ConnectionConfigurationComboBoxModel;
import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;

import javax.swing.*;
import java.awt.event.ItemEvent;

public class ConnectionSelect extends JComboBox<ConnectionSettingsService.ConnectionSettings> {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );

    public ConnectionSelect( ) {
        EVENT_BUS.register( this );
        this.addItemListener( itemEvent -> {
            if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
                EVENT_BUS.post( new ConnectionSelectionChangedEvent( ( ConnectionSettingsService.ConnectionSettings ) itemEvent.getItem( ) ) );
            }
        } );

        setRenderer( new ConnectionListCellRenderer( ) );
        if ( !connectionSettingsService.connectionSettingsState.connectionSettings.isEmpty( ) ) {
            setModel( new ConnectionConfigurationComboBoxModel( connectionSettingsService.connectionSettingsState.connectionSettings.toArray( new ConnectionSettingsService.ConnectionSettings[]{ } ) ) );
            setSelectedIndex( 0 );
            EVENT_BUS.post( new ConnectionSelectionChangedEvent( ( ConnectionSettingsService.ConnectionSettings ) getSelectedItem( ) ) );
        }
    }

    @Subscribe
    public void connectionSettingsAdded( ConnectionAddedEvent event ) {
        addItem( event.getData( ) );
        if ( getItemCount( ) == 1 ) {
            setSelectedIndex( 0 );
            EVENT_BUS.post( new ConnectionSelectionChangedEvent( ( ConnectionSettingsService.ConnectionSettings ) getSelectedItem( ) ) );
        }
    }
}
