package com.hsofttec.intellij.querytester.ui.components;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

import javax.swing.*;

public class ConnectionSetupComponent {
    private JTextField inputConnectioName;
    private JPanel mainPanel;
    private JLabel labelConnectioName;
    private JLabel labelServer;
    private JLabel labelPort;
    private JCheckBox inputUseSSL;
    private JTextField inputServer;
    private JTextField inputPort;
    private JLabel labelInstance;
    private JTextField inputInstance;
    private JLabel labelUser;
    private JTextField inputUsername;
    private JPasswordField inputPassword;
    private JLabel labelPassword;
    private JTextField inputTimeout;
    private JLabel labelTimeout;

    private JTextField inputSourceFolder;

    private JTextArea inputScriptTemplate;

    public ConnectionSetupComponent( ) {
    }

    public JPanel getMainPanel( ) {
        return mainPanel;
    }

    public JTextField getInputConnectioName( ) {
        return inputConnectioName;
    }

    public JTextField getInputTimeout( ) {
        return inputTimeout;
    }

    public JCheckBox getInputUseSSL( ) {
        return inputUseSSL;
    }

    public JTextField getInputServer( ) {
        return inputServer;
    }

    public JTextField getInputPort( ) {
        return inputPort;
    }

    public JTextField getInputInstance( ) {
        return inputInstance;
    }

    public JTextField getInputUsername( ) {
        return inputUsername;
    }

    public JPasswordField getInputPassword( ) {
        return inputPassword;
    }

    public void setData( ConnectionSettingsService.ConnectionSettings data ) {
        if ( data != null ) {
            inputConnectioName.setText( data.getConnectionName( ) );
            inputUseSSL.setSelected( data.isSsl( ) );
            inputServer.setText( data.getServer( ) );
            inputPort.setText( String.valueOf( data.getPort( ) ) );
            inputTimeout.setText( String.valueOf( data.getTimeout( ) ) );
            inputInstance.setText( data.getInstance( ) );
            inputUsername.setText( data.getUsername( ) );
            inputPassword.setText( data.getPassword( ) );
        }
    }

    public void getData( ConnectionSettingsService.ConnectionSettings data ) {
        data.setConnectionName( inputConnectioName.getText( ) );
        data.setSsl( inputUseSSL.isSelected( ) );
        data.setServer( inputServer.getText( ) );
        data.setPort( Integer.parseInt( inputPort.getText( ) ) );
        data.setTimeout( Integer.parseInt( inputTimeout.getText( ) ) );
        data.setInstance( inputInstance.getText( ) );
        data.setUsername( inputUsername.getText( ) );
        data.setPassword( inputPassword.getText( ) );
    }

    public boolean isModified( ConnectionSettingsService.ConnectionSettingsState data ) {
        return true;
    }
}
