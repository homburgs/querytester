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

import com.ceyoniq.nscale.al.connector.AdvancedConnector;
import com.ceyoniq.nscale.al.core.Principal;
import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyName;
import com.ceyoniq.nscale.al.core.common.Property;
import com.ceyoniq.nscale.al.core.common.PropertyName;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceKeyInfo;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.ui.Notifier;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * lock a nscale repository resource
     *
     * @param resourceId resource id
     */
    public void lockResource( String resourceId ) {
        Session localSession = getSession( );
        if ( localSession != null && localSession.isOpen( ) ) {
            localSession.getRepositoryService( ).lock( new ResourceKey( resourceId ) );
        }
    }

    /**
     * unlock a nscale repository resource
     *
     * @param resourceId resource id
     */
    public void unlockResource( String resourceId ) {
        Session localSession = getSession( );
        if ( localSession != null && localSession.isOpen( ) ) {
            localSession.getRepositoryService( ).unlock( new ResourceKey( resourceId ) );
        }
    }

    public BaseResource getBaseResource( String resourceId ) {
        Session localSession = getSession( );
        BaseResource baseResource = new BaseResource( );
        if ( localSession != null && localSession.isOpen( ) ) {
            ResourceKeyInfo resourceKeyInfo = ResourceKeyInfo.get( resourceId );
            List<PropertyName> propertyNames = new ArrayList<>( );
            propertyNames.add( new IndexingPropertyName( "objectclass", resourceKeyInfo.getDocumentAreaName( ) ) );
            propertyNames.add( new IndexingPropertyName( "resourcetype", resourceKeyInfo.getDocumentAreaName( ) ) );
            propertyNames.add( new IndexingPropertyName( "lockdate", resourceKeyInfo.getDocumentAreaName( ) ) );
            List<Property> properties = localSession.getRepositoryService( ).getProperties( new ResourceKey( resourceId ), propertyNames );
            for ( Property property : properties ) {
                if ( property.getValue( ) != null ) {
                    String name = property.getPropertyName( ).getName( );
                    Class<?> valueClass = property.getValue( ).getClass( );
                    try {
                        Method method = baseResource.getClass( ).getMethod( "set" + StringUtils.capitalize( name ), valueClass );
                        method.invoke( baseResource, property.getValue( ) );
                    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
                        throw new RuntimeException( e );
                    }
                }
            }
        }
        return baseResource;
    }
}
