package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

public class ConnectionRemovedEvent {
    private final ConnectionSettingsService.ConnectionSettings connectionSettings;

    public ConnectionRemovedEvent( ConnectionSettingsService.ConnectionSettings connectionSettings ) {
        this.connectionSettings = connectionSettings;
    }

    public ConnectionSettingsService.ConnectionSettings getData( ) {
        return connectionSettings;
    }
}
