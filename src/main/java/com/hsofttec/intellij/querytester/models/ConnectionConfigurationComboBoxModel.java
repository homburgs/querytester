package com.hsofttec.intellij.querytester.models;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

import javax.swing.*;

public class ConnectionConfigurationComboBoxModel extends DefaultComboBoxModel<ConnectionSettingsService.ConnectionSettings> {
    public ConnectionConfigurationComboBoxModel( ConnectionSettingsService.ConnectionSettings[] items ) {
        super( items );

    }

    @Override
    public ConnectionSettingsService.ConnectionSettings getSelectedItem( ) {
        return ( ConnectionSettingsService.ConnectionSettings ) super.getSelectedItem( );
    }
}
