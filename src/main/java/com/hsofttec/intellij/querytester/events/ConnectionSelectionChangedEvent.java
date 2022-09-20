package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

public class ConnectionSelectionChangedEvent {
    private final ConnectionSettingsService.ConnectionSettings connectionSettings;

    private static final ConnectionService connectionService = ConnectionService.getInstance( );

    public ConnectionSelectionChangedEvent( ConnectionSettingsService.ConnectionSettings connectionSettings ) {
        this.connectionSettings = connectionSettings;
        connectionService.createConnection( connectionSettings );

    }

    public ConnectionSettingsService.ConnectionSettings getConnectionSettings( ) {
        return connectionSettings;
    }
}
