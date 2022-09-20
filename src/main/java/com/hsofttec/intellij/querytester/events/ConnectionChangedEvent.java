package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

public class ConnectionChangedEvent {
    private final ConnectionSettingsService.ConnectionSettings connectionSettings;

    public ConnectionChangedEvent( ConnectionSettingsService.ConnectionSettings connectionSettings ) {
        this.connectionSettings = connectionSettings;
    }

    public ConnectionSettingsService.ConnectionSettings getData( ) {
        return connectionSettings;
    }
}
