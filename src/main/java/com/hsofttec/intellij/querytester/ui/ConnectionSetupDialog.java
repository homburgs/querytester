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

package com.hsofttec.intellij.querytester.ui;

import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.ui.components.ConnectionSetupComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConnectionSetupDialog extends DialogWrapper {
    private ConnectionSetupComponent connectionSetupComponent = null;

    public ConnectionSetupDialog( Project project ) {
        super(project, true);
        setModal(true);
        init();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        connectionSetupComponent = new ConnectionSetupComponent();
        connectionSetupComponent.getInputConnectioName().requestFocus();
        return connectionSetupComponent.getMainPanel();
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        boolean activated = connectionSetupComponent.getInputConnectionActivated().isSelected();
        String connectioName = connectionSetupComponent.getInputConnectioName().getText();
        String server = connectionSetupComponent.getInputServer().getText();
        String port = connectionSetupComponent.getInputPort().getText();
        boolean ssl = connectionSetupComponent.getInputUseSSL().isSelected();
        String timeout = connectionSetupComponent.getInputTimeout().getText();
        String connectTimeout = connectionSetupComponent.getInputConnectTimeout().getText();
        String username = connectionSetupComponent.getInputUsername().getText();
        String instance = connectionSetupComponent.getInputInstance().getText();
        char[] password = connectionSetupComponent.getInputPassword().getPassword();

        if (StringUtils.isBlank(connectioName)) {
            return new ValidationInfo("Connection name is mandatory", connectionSetupComponent.getInputConnectioName());
        }

        if (StringUtils.isBlank(server)) {
            return new ValidationInfo("Server is mandatory", connectionSetupComponent.getInputServer());
        }

        if (!StringUtils.isNumeric(port)) {
            return new ValidationInfo("Port is mandatory and must be an integer value", connectionSetupComponent.getInputPort());
        }

        if (!StringUtils.isNumeric(port)) {
            return new ValidationInfo("Port is mandatory and must be an integer value", connectionSetupComponent.getInputPort());
        }

        if (!StringUtils.isNumeric(timeout)) {
            return new ValidationInfo("Timeout is mandatory and must be an integer value", connectionSetupComponent.getInputTimeout());
        }

        if (!StringUtils.isNumeric(connectTimeout)) {
            return new ValidationInfo("Connect-Timeout is mandatory and must be an integer value", connectionSetupComponent.getInputConnectTimeout());
        }

        if (StringUtils.isBlank(instance)) {
            return new ValidationInfo("Instance is mandatory and must be an integer value", connectionSetupComponent.getInputInstance());
        }

        if (StringUtils.isBlank(username)) {
            return new ValidationInfo("Username is mandatory", connectionSetupComponent.getInputUsername());
        }

        if (password.length == 0) {
            return new ValidationInfo("Password is mandatory", connectionSetupComponent.getInputPassword());
        }

        if (activated) {
            ConnectionSettings settings = new ConnectionSettings();
            settings.setConnectionName(connectioName);
            settings.setServer(server);
            settings.setPort(Integer.parseInt(port));
            settings.setInstance(instance);
            settings.setSsl(ssl);
            settings.setTimeout(Integer.parseInt(timeout));
            settings.setConnectTimeout(Integer.parseInt(connectTimeout) * 1000);
            settings.setUsername(username);
            settings.setPassword(new String(password));
        }

        return null;
    }

    public ConnectionSettings getData() {
        return connectionSetupComponent.getData();
    }

    public void setData( ConnectionSettings settings ) {

        if (StringUtils.isBlank(settings.getConnectionName())) {
            setTitle("Add New Connection Settings");
            setOKButtonText("Add");
        } else {
            setTitle(String.format("Change Settings For '%s'", settings.getConnectionName()));
            setOKButtonText("Save Changes");
        }

        connectionSetupComponent.setData(settings);
    }
}
