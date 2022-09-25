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

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

import javax.swing.*;

public class ConnectionSetupComponent {
    private ConnectionSettingsService.ConnectionSettings settings;
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
    private JCheckBox inputConnectionActivated;
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

    public JCheckBox getInputConnectionActivated( ) {
        return inputConnectionActivated;
    }

    public void setData( ConnectionSettingsService.ConnectionSettings data ) {
        settings = data;
        if ( settings != null ) {
            inputConnectionActivated.setSelected( settings.isActive( ) );
            inputConnectioName.setText( settings.getConnectionName( ) );
            inputUseSSL.setSelected( settings.isSsl( ) );
            inputServer.setText( settings.getServer( ) );
            inputPort.setText( String.valueOf( settings.getPort( ) ) );
            inputTimeout.setText( String.valueOf( settings.getTimeout( ) ) );
            inputInstance.setText( settings.getInstance( ) );
            inputUsername.setText( settings.getUsername( ) );
            inputPassword.setText( settings.getPassword( ) );
        } else {
            settings.setActive( true );
            settings.setServer( "127.0.0.1" );
            settings.setPort( 8080 );
            settings.setSsl( false );
            settings.setInstance( "nscalealinst1" );
            settings.setUsername( "admin@nscale" );
            settings.setTimeout( 0 );
        }
    }

    public ConnectionSettingsService.ConnectionSettings getData( ) {
        settings.setActive( inputConnectionActivated.isSelected( ) );
        settings.setConnectionName( inputConnectioName.getText( ) );
        settings.setSsl( inputUseSSL.isSelected( ) );
        settings.setServer( inputServer.getText( ) );
        settings.setPort( Integer.parseInt( inputPort.getText( ) ) );
        settings.setTimeout( Integer.parseInt( inputTimeout.getText( ) ) );
        settings.setInstance( inputInstance.getText( ) );
        settings.setUsername( inputUsername.getText( ) );
        settings.setPassword( inputPassword.getText( ) );
        return settings;
    }

    public boolean isModified( ConnectionSettingsService.ConnectionSettingsState data ) {
        return true;
    }
}