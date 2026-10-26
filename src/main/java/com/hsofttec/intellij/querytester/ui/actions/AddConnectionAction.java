/*
 * The MIT License (MIT)
 *
 * Copyright © 2026 Sven Homburg, <homburgs@gmail.com>
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

package com.hsofttec.intellij.querytester.ui.actions;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.ui.ConnectionSetupDialog;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.ui.notifiers.ConnectionsModifiedNotifier;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class AddConnectionAction extends AnAction {
    private final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings();
    private final QueryTester queryTester;

    public AddConnectionAction(QueryTester queryTester) {
        super("Add Connection", "Show the connection settings dialog", AllIcons.General.Add);
        this.queryTester = queryTester;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        ConnectionSettings connectionSettings = new ConnectionSettings();
        ConnectionSetupDialog connectionSetupDialog = new ConnectionSetupDialog(actionEvent.getProject());
        connectionSetupDialog.setData(connectionSettings);
        if (connectionSetupDialog.showAndGet()) {
            connectionSettings = connectionSetupDialog.getData();
            connectionSettingsService.connectionSettingsState.connectionSettings.add(connectionSettings);
            ConnectionsModifiedNotifier notifier = queryTester.getProject().getMessageBus().syncPublisher(ConnectionsModifiedNotifier.CONNECTION_MODIFIED_TOPIC);
            notifier.connectionAdded(connectionSettings);
        }
    }
}
