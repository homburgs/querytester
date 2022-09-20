package com.hsofttec.intellij.querytester.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@State(
        name = "querytester",
        storages = @Storage( "querytester.xml" )
)
public class ConnectionSettingsService implements PersistentStateComponent<ConnectionSettingsService.ConnectionSettingsState> {

    public ConnectionSettingsService.ConnectionSettingsState connectionSettingsState = new ConnectionSettingsService.ConnectionSettingsState( );

    public ConnectionSettingsService( ) {
    }

    public static ConnectionSettingsService getSettings( ) {
        return ApplicationManager.getApplication( ).getService( ConnectionSettingsService.class );
    }


    @Override
    public ConnectionSettingsService.ConnectionSettingsState getState( ) {
        return connectionSettingsState;
    }

    @Override
    public void loadState( @NotNull ConnectionSettingsService.ConnectionSettingsState state ) {
        XmlSerializerUtil.copyBean( state, this.connectionSettingsState );
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

    public static class ConnectionSettingsState {
        public List<ConnectionSettings> connectionSettings = new ArrayList<>( );
    }

    public static class ConnectionSettings {
        private String id;
        private String connectionName;
        private boolean ssl;
        private String server;
        private int port;
        private int timeout;
        private String instance;
        private String username;
        private String password;

        public ConnectionSettings( ) {
        }

        public String getId( ) {
            if ( StringUtils.isBlank( id ) ) {
                id = RandomStringUtils.randomAlphanumeric( 20 );
            }
            return id;
        }

        public void setId( String id ) {
            this.id = id;
        }

        public String getConnectionName( ) {
            return connectionName;
        }

        public void setConnectionName( final String connectionName ) {
            this.connectionName = connectionName;
        }

        public boolean isSsl( ) {
            return ssl;
        }

        public void setSsl( final boolean ssl ) {
            this.ssl = ssl;
        }

        public String getServer( ) {
            return server;
        }

        public void setServer( final String server ) {
            this.server = server;
        }

        public int getPort( ) {
            return port;
        }

        public void setPort( final int port ) {
            this.port = port;
        }

        public String getInstance( ) {
            return instance;
        }

        public void setInstance( final String instance ) {
            this.instance = instance;
        }

        public String getUsername( ) {
            return username;
        }

        public void setUsername( final String username ) {
            this.username = username;
        }

        public String getPassword( ) {
            return password;
        }

        public void setPassword( final String password ) {
            this.password = password;
        }

        public int getTimeout( ) {
            return timeout;
        }

        public void setTimeout( int timeout ) {
            this.timeout = timeout;
        }
    }

}
