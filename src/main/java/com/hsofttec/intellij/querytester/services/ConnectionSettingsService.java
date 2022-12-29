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

package com.hsofttec.intellij.querytester.services;

import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.states.ConnectionSettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

@State(
        name = "querytester",
        storages = @Storage("querytester.xml")
)
public class ConnectionSettingsService implements PersistentStateComponent<ConnectionSettingsState> {

    public ConnectionSettingsState connectionSettingsState = new ConnectionSettingsState();

    public ConnectionSettingsService() {
    }

    public static ConnectionSettingsService getSettings() {
        return ApplicationManager.getApplication().getService(ConnectionSettingsService.class);
    }


    @Override
    public ConnectionSettingsState getState() {
        return connectionSettingsState;
    }

    @Override
    public void loadState( @NotNull ConnectionSettingsState state ) {
        XmlSerializerUtil.copyBean(state, this.connectionSettingsState);
    }

    public void removeConnection( String connectionId ) {
        connectionSettingsState.connectionSettings = connectionSettingsState.connectionSettings
                .stream( )
                .filter( connectionConfiguration -> !connectionConfiguration.getId( ).equals( connectionId ) )
                .collect( Collectors.toList( ) );
    }

    public void updateConnection( ConnectionSettings connectionSettings ) {
        for ( ConnectionSettings configuration : this.connectionSettingsState.connectionSettings ) {
            if ( configuration.getId( ).equals( connectionSettings.getId( ) ) ) {
                configuration.setConnectionName( connectionSettings.getConnectionName( ) );
                configuration.setServer( connectionSettings.getServer( ) );
                configuration.setPort( connectionSettings.getPort( ) );
                configuration.setSsl( connectionSettings.isSsl( ) );
                configuration.setInstance( connectionSettings.getInstance( ) );
                configuration.setUsername( connectionSettings.getUsername( ) );
                configuration.setPassword( connectionSettings.getPassword( ) );
                return;
            }
        }
    }
}
