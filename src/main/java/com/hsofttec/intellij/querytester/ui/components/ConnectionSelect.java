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

import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.ui.notifiers.CheckServerConnectionNotifier;
import com.hsofttec.intellij.querytester.ui.notifiers.ConnectionsModifiedNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.util.messages.MessageBus;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionSelect extends ComboBox<ConnectionSettings> implements ItemListener {
    private static final ConnectionSettingsService CONNECTION_SETTINGS_SERVICE = ConnectionSettingsService.getSettings();
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();
    private final Project project;
    private final MessageBus messageBus;
    private final List<ConnectionSettings> connections = new ArrayList<>();
    private final QueryTab owner;

    public ConnectionSelect( QueryTab owner ) {
        this.owner = owner;
        this.project = owner.getQueryTester().getProject();
        messageBus = project.getMessageBus();
        this.addItemListener(this);
        setModel(new CollectionComboBoxModel<>(connections));
        setRenderer(new ConnectionListCellRenderer());
    }

    public void reloadItems() {
        List<ConnectionSettings> connectionSettings = CONNECTION_SETTINGS_SERVICE.connectionSettingsState.connectionSettings.stream()
                .filter(ConnectionSettings::isActive).collect(Collectors.toList());

        connections.clear();
        if (!connectionSettings.isEmpty()) {
            connections.addAll(connectionSettings);
            setSelectedIndex(0);
            ConnectionSettings settings = (ConnectionSettings) getSelectedItem();
            if (settings != null) {
                checkOnlineState(settings);
            }
        }
    }

    @Override
    public void itemStateChanged( ItemEvent itemEvent ) {
        if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
            ConnectionSettings settings = (ConnectionSettings) getSelectedItem();
            if (settings != null) {
                checkOnlineState(settings);
            }
        }
    }

    public void checkOnlineState( ConnectionSettings settings ) {
        MessageBus messageBus = project.getMessageBus();
        CheckServerConnectionNotifier checkServerConnectionNotifier = messageBus.syncPublisher(CheckServerConnectionNotifier.CHECK_SERVER_CONNECTION_TOPIC);
        checkServerConnectionNotifier.beforeAction(settings);

        ApplicationManager.getApplication().invokeLater(() -> {
            boolean connectionUsable = CONNECTION_SERVICE.isConnectionUsable(settings);
            checkServerConnectionNotifier.afterAction(settings, connectionUsable);
        }, ModalityState.nonModal());
    }

    public void modifyConnection( ConnectionSettings settings ) {
        CollectionComboBoxModel<ConnectionSettings> model = (CollectionComboBoxModel<ConnectionSettings>) getModel();
        for (ConnectionSettings item : model.getItems()) {
            if (item.getId().equals(settings.getId())) {
                item.setUsername(settings.getUsername());
                item.setPassword(settings.getPassword());
                item.setSsl(settings.isSsl());
                item.setServer(settings.getServer());
                item.setInstance(settings.getInstance());
                item.setPort(settings.getPort());
                item.setConnectionName(settings.getConnectionName());
                item.setTimeout(settings.getTimeout());
                item.setTimeout(settings.getTimeout());
            }
        }
    }

    @Subscribe
    public void addConnection( ConnectionSettings settings ) {
        connections.add(settings);
        if (getItemCount() == 1) {
            setSelectedIndex(0);
            ConnectionsModifiedNotifier notifier = messageBus.syncPublisher(ConnectionsModifiedNotifier.CONNECTION_MODIFIED_TOPIC);
            notifier.connectionAdded((ConnectionSettings) getSelectedItem());
        }
    }

    public void removeConnection( ConnectionSettings settings ) {
        connections.remove(settings);
    }
}
