package com.hsofttec.intellij.querytester.ui.components;

import com.google.common.eventbus.EventBus;
import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.ui.components.JBList;

import javax.swing.*;

public class ConnectionList extends JBList<ConnectionSettingsService.ConnectionSettings> {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );

    public ConnectionList( ) {
        super( connectionSettingsService.connectionSettingsState.connectionSettings );
        EVENT_BUS.register( this );
        setCellRenderer( new ConnectionListCellRenderer( ) );
    }

    public ConnectionSettingsService.ConnectionSettings getSelectedItem( ) {
        ListModel<ConnectionSettingsService.ConnectionSettings> model = getModel( );
        return model.getElementAt( getSelectedIndex( ) );
    }

    public void addElement( ConnectionSettingsService.ConnectionSettings connectionSettings ) {
        DefaultListModel<ConnectionSettingsService.ConnectionSettings> model = ( DefaultListModel<ConnectionSettingsService.ConnectionSettings> ) getModel( );
        model.addElement( connectionSettings );
    }

    public void removeElement( ConnectionSettingsService.ConnectionSettings connectionSettings ) {
        DefaultListModel<ConnectionSettingsService.ConnectionSettings> model = ( DefaultListModel<ConnectionSettingsService.ConnectionSettings> ) getModel( );
        model.removeElement( connectionSettings );
    }
}
