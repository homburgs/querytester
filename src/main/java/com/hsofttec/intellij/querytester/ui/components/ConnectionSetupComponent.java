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

import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.states.ConnectionSettingsState;
import com.hsofttec.intellij.querytester.ui.Notifier;
import com.intellij.ide.plugins.newui.LinkComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.AnimatedIcon;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConnectionSetupComponent {
    private final static ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();

    private ConnectionSettings settings;
    private JTextField inputConnectionName;
    private JPanel mainPanel;
    private JLabel labelConnectionName;
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
    private JTextField inputConnectTimeout;
    private JLabel labelTimeout;
    private JCheckBox inputConnectionActivated;
    private JLabel labelConnectTimeout;
    private LinkComponent linkTestConnection;
    private JTextField inputSourceFolder;
    private JTextArea inputScriptTemplate;

    public ConnectionSetupComponent() {
        linkTestConnection.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked( MouseEvent mouseEvent ) {
                linkTestConnection.setIcon(new AnimatedIcon.Default());
                linkTestConnection.setEnabled(false);

                ApplicationManager.getApplication().invokeAndWait(() -> {
                    ConnectionSettings data = getData();

                    System.err.println(data.getPassword());

                    boolean connectionUsable = CONNECTION_SERVICE.isConnectionUsable(data);
                    if (connectionUsable) {
                        Notifier.information("Connection tested successfully");
                    } else {
                        Notifier.warning("Connection not successfully");
                    }
                    linkTestConnection.setEnabled(true);
                    linkTestConnection.setIcon(null);
                });
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTextField getInputConnectioName() {
        return inputConnectionName;
    }

    public JTextField getInputTimeout() {
        return inputTimeout;
    }

    public JTextField getInputConnectTimeout() {
        return inputConnectTimeout;
    }

    public JCheckBox getInputUseSSL() {
        return inputUseSSL;
    }

    public JTextField getInputServer() {
        return inputServer;
    }

    public JTextField getInputPort() {
        return inputPort;
    }

    public JTextField getInputInstance() {
        return inputInstance;
    }

    public JTextField getInputUsername() {
        return inputUsername;
    }

    public JPasswordField getInputPassword() {
        return inputPassword;
    }

    public JCheckBox getInputConnectionActivated() {
        return inputConnectionActivated;
    }

    public void setData( ConnectionSettings data ) {
        settings = data;
        if (settings == null || StringUtils.isBlank(settings.getConnectionName())) {
            settings.setActive(true);
            settings.setConnectionName("New connection");
            settings.setServer("127.0.0.1");
            settings.setPort(8080);
            settings.setSsl(false);
            settings.setInstance("nscalealinst1");
            settings.setUsername("admin@nscale");
            settings.setTimeout(10);
            settings.setConnectTimeout(3000);
            settings.setActive(true);
        }

        inputConnectionActivated.setSelected(settings.isActive());
        inputConnectionName.setText(settings.getConnectionName());
        inputUseSSL.setSelected(settings.isSsl());
        inputServer.setText(settings.getServer());
        inputPort.setText(String.valueOf(settings.getPort()));
        inputTimeout.setText(String.valueOf(settings.getTimeout()));
        inputConnectTimeout.setText(String.valueOf(settings.getConnectTimeout() / 1000));
        inputInstance.setText(settings.getInstance());
        inputUsername.setText(settings.getUsername());
        inputPassword.setText(settings.getPassword());
    }

    public ConnectionSettings getData() {
        settings.setActive(inputConnectionActivated.isSelected());
        settings.setConnectionName(inputConnectionName.getText());
        settings.setSsl(inputUseSSL.isSelected());
        settings.setServer(inputServer.getText());
        settings.setPort(Integer.parseInt(inputPort.getText()));
        settings.setTimeout(Integer.parseInt(inputTimeout.getText()));
        settings.setConnectTimeout(Integer.parseInt(inputConnectTimeout.getText()) * 1000);
        settings.setInstance(inputInstance.getText());
        settings.setUsername(inputUsername.getText());
        settings.setPassword(new String(inputPassword.getPassword()));
        return settings;
    }

    public boolean isModified( ConnectionSettingsState data ) {
        return true;
    }
}
