package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

public class ConnectionAddedEvent {
    private final ConnectionSettingsService.ConnectionSettings connectionSettings;

    public ConnectionAddedEvent( ConnectionSettingsService.ConnectionSettings connectionSettings ) {
        this.connectionSettings = connectionSettings;
    }

    public ConnectionSettingsService.ConnectionSettings getData( ) {
        return connectionSettings;
    }
}
