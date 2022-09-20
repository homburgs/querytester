package com.hsofttec.intellij.querytester.services;

import com.ceyoniq.nscale.al.connector.AdvancedConnector;
import com.ceyoniq.nscale.al.core.Principal;
import com.ceyoniq.nscale.al.core.Session;
import com.hsofttec.intellij.querytester.ui.Notifier;
import org.apache.commons.lang.StringUtils;

public class ConnectionService {
    private static ConnectionService instance = null;
    private Session session = null;
    private String connectionId = null;

    private ConnectionService( ) {
    }

    public static ConnectionService getInstance( ) {
        if ( instance == null ) {
            synchronized ( ConnectionService.class ) {
                System.err.println( "ConnectionService created" );
                instance = new ConnectionService( );
            }
        }
        return instance;
    }

    public void createConnection( ConnectionSettingsService.ConnectionSettings connectionSettings ) {

        if ( !connectionSettings.getId( ).equals( connectionId ) ) {
            if ( session != null ) {
                if ( session.isOpen( ) ) {
                    session.close( );
                }
                session = null;
                connectionId = null;
            }

            ClassLoader current = Thread.currentThread( ).getContextClassLoader( );
            try {
                Thread.currentThread( ).setContextClassLoader( this.getClass( ).getClassLoader( ) );

                AdvancedConnector advancedConnector = new AdvancedConnector( );
                advancedConnector.setHost( connectionSettings.getServer( ) );
                advancedConnector.setPort( connectionSettings.getPort( ) );
                advancedConnector.setInstanceName( connectionSettings.getInstance( ) );
                advancedConnector.setSsl( connectionSettings.isSsl( ) );
                advancedConnector.setTimeout( connectionSettings.getTimeout( ) );
                connectionId = connectionSettings.getId( );

                String[] usernameParts = StringUtils.split( connectionSettings.getUsername( ), "@" );
                String username = usernameParts[ 0 ];
                String domain = null;

                if ( usernameParts.length > 1 ) {
                    domain = usernameParts[ 1 ];
                }

                Principal principal = new Principal( username, connectionSettings.getPassword( ), domain );
                session = advancedConnector.login( principal );
            } catch ( Exception exception ) {
                Notifier.error( exception.getLocalizedMessage( ) );
                throw new RuntimeException( exception );
            } finally {
                Thread.currentThread( ).setContextClassLoader( current );
            }
        }
    }

    public Session getSession( ) {
        if ( session == null ) {
            Notifier.warning( "no application layer session exists" );
        }
        return session;
    }
}
