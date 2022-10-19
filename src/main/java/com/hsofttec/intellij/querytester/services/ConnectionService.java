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
import com.ceyoniq.nscale.al.core.cfg.FolderObjectclass;
import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyName;
import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import com.ceyoniq.nscale.al.core.common.Property;
import com.ceyoniq.nscale.al.core.common.PropertyName;
import com.ceyoniq.nscale.al.core.content.*;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceKeyInfo;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;
import com.hsofttec.intellij.querytester.ui.Notifier;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectionService {
    private static final Logger logger = LoggerFactory.getLogger( ConnectionService.class );
    private static ConnectionService instance = null;
    private Session session = null;
    private String connectionId = null;

    private ConnectionService( ) {
    }

    public static ConnectionService getInstance( ) {
        if ( instance == null ) {
            synchronized ( ConnectionService.class ) {
                logger.info( "{} created", ConnectionService.class.getSimpleName( ) );
                instance = new ConnectionService( );
            }
        }
        return instance;
    }

    public void createConnection( ConnectionSettings connectionSettings ) {

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
                advancedConnector.setConnectTimeout( connectionSettings.getConnectTimeout( ) );
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
                String localizedMessage = exception.getLocalizedMessage( );
//                logger.error( localizedMessage, rootCause );
//                Notifier.error( String.format( "%s: %s", connectionSettings.getConnectionName( ), localizedMessage ) );
                session = null;
                connectionId = null;
                throw exception;
            } finally {
                Thread.currentThread( ).setContextClassLoader( current );
            }
        }
    }

    public Session getSession( ) {
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
            propertyNames.add( new IndexingPropertyName( "displayname", resourceKeyInfo.getDocumentAreaName( ) ) );
            propertyNames.add( new IndexingPropertyName( "resourceid", resourceKeyInfo.getDocumentAreaName( ) ) );
            propertyNames.add( new IndexingPropertyName( "parentresourceid", resourceKeyInfo.getDocumentAreaName( ) ) );
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

    public List<BaseResource> getParentsUntilRootFolder( String childResourceId ) {
        List<BaseResource> list = new ArrayList<>( );

        do {
            BaseResource baseResource = getBaseResource( childResourceId );
            list.add( baseResource );
            childResourceId = baseResource.getParentresourceid( );
        } while ( childResourceId != null );

        return list;
    }

    public List<ObjectclassName> getPossibleObjectclassesForFolderCreation( BaseResource parentFolder ) {
        Session localSession = getSession( );
        List<ObjectclassName> allowedFolderObjectclassNames = new ArrayList<>( );

        if ( localSession != null ) {
            List<FolderObjectclass> folderObjectclasses = localSession.getConfigurationService( ).getFolderObjectclasses( parentFolder.getObjectclass( ).getAreaName( ) );
            for ( FolderObjectclass folderObjectclass : folderObjectclasses ) {
                if ( folderObjectclass.getName( ).getName( ).equals( parentFolder.getObjectclass( ).getName( ) ) ) {
                    allowedFolderObjectclassNames.addAll( folderObjectclass.getAllowedFolderObjectclassNames( ) );
                    break;
                }
            }
        }

        return allowedFolderObjectclassNames;
    }

    public List<ObjectclassName> getPossibleObjectclassesForDocumentCreation( BaseResource parentFolder ) {
        Session localSession = getSession( );
        List<ObjectclassName> allowedDocumentObjectclassNames = new ArrayList<>( );

        if ( localSession != null ) {
            List<FolderObjectclass> objectclasses = localSession.getConfigurationService( ).getFolderObjectclasses( parentFolder.getObjectclass( ).getAreaName( ) );
            for ( FolderObjectclass objectclass : objectclasses ) {
                if ( objectclass.getName( ).getName( ).equals( parentFolder.getObjectclass( ).getName( ) ) ) {
                    allowedDocumentObjectclassNames.addAll( objectclass.getAllowedDocumentObjectclassNames( ) );
                    break;
                }
            }
        }

        return allowedDocumentObjectclassNames;
    }

    public ResourceKey createFolder( BaseResource parentResource, ObjectclassName objectclassName, String displayname ) {
        Session localSession = getSession( );
        ResourceKey resourceKey = null;

        if ( localSession != null ) {
            List<Property> properties = Collections.singletonList( new Property( new IndexingPropertyName( "displayname", parentResource.getObjectclass( ).getAreaName( ) ), displayname ) );
            resourceKey = localSession.getRepositoryService( ).createFolder( objectclassName, new ResourceKey( parentResource.getResourceid( ) ), properties, false );
        }

        return resourceKey;
    }

    /**
     * // create document as child of a folder
     * <p>
     * // build properties
     * List props = new ArrayList ();
     * IndexingPropertyName ipn = new IndexingPropertyName ( "displayname", "myDocumentAreaName" );
     * Property prop = new Property ( ipn, "Hello world!" );
     * props.add ( prop );
     * <p>
     * ObjectclassName docOcnD1 = new ObjectclassName ( "D1", "myDocumentAreaName" );
     * <p>
     * // build extended content item and its properties
     * ContentItemProperties cips = new ContentItemProperties();
     * cips.setProperty ( ContentItemBaseProperty.name, "my_name" );
     * cips.setProperty ( ContentItemBaseProperty.displayName, "my_displayname" );
     * cips.setProperty ( ContentItemBaseProperty.contentType, "text/plain; charset=\"iso-8859-1\"" );
     * DefaultExtendedContentItem deci = new DefaultExtendedContentItem ( cips, "TestCreateDocument text." );
     * List items = new ArrayList();
     * items.add ( deci );
     * <p>
     * // build extended content and its properties
     * ContentProperties cps = new ContentProperties();
     * cps.setProperty ( ContentBaseProperty.contentType, "text/plain; charset=\"iso-8859-1\"" );
     * ExtendedContent content = new DefaultExtendedContent ( cps, items );
     * <p>
     * // create document
     * ResourceKey newDocumentRk = repositoryService.createDocument ( docOcnD1, existingFolderRk, props, false, content );
     *
     * @param parentResource
     * @param objectclassName
     * @param displayname
     * @param selectedFileName
     * @return
     */
    public ResourceKey createDocument( BaseResource parentResource, ObjectclassName objectclassName, String displayname, String selectedFileName ) {
        Session localSession = getSession( );
        ResourceKey resourceKey = null;
        String contentType = null;

        if ( localSession != null ) {
            List<Property> properties = Collections.singletonList( new Property( new IndexingPropertyName( "displayname", parentResource.getObjectclass( ).getAreaName( ) ), displayname ) );

            Path file = Paths.get( selectedFileName );
            try ( InputStream inputStream = new FileInputStream( file.toFile( ) ) ) {
                contentType = Files.probeContentType( file );
                ContentItemProperties cips = new ContentItemProperties( );
                cips.setProperty( ContentItemBaseProperty.name, displayname );
                cips.setProperty( ContentItemBaseProperty.displayName, displayname );
                cips.setProperty( ContentItemBaseProperty.contentType, contentType );

                ExtendedContentItem extendedContentItem = new DefaultExtendedContentItem( cips, inputStream );
                ContentProperties cps = new ContentProperties( );
                cps.setProperty( ContentBaseProperty.contentType, contentType );
                ExtendedContent extendedContent = new DefaultExtendedContent( cps, Collections.singletonList( extendedContentItem ) );
                resourceKey = localSession.getRepositoryService( ).createDocument( objectclassName, new ResourceKey( parentResource.getResourceid( ) ), properties, false, extendedContent );

            } catch ( IOException e ) {
                throw new RuntimeException( e );
            }
        }

        return resourceKey;
    }

    public boolean isConnectionUsable( ConnectionSettings settings ) {
        boolean usable = false;

        if ( settings != null ) {
            try {
                createConnection( settings );
                usable = true;
            } catch ( Exception e ) {
                Notifier.warning( String.format( "connection '%s' not usable: %s", settings.getConnectionName( ), ExceptionUtils.getRootCauseMessage( e ) ) );
            }
        }

        return usable;
    }

    public Thread checkConnection( ConnectionSettings settings, Runnable successCallback ) {
        return checkConnection( settings, successCallback, null, null );
    }

    public Thread checkConnection( ConnectionSettings settings, Runnable successCallback, Runnable finalCallback ) {
        return checkConnection( settings, successCallback, null, finalCallback );
    }

    public Thread checkConnection( ConnectionSettings settings, Runnable successCallback, Runnable errorCallback, Runnable finalCallback ) {
        Thread thread = new Thread( ( ) -> {

            boolean connectionUsable = isConnectionUsable( settings );
            if ( connectionUsable ) {
                if ( successCallback != null ) {
                    successCallback.run( );
                }
            } else {
                if ( errorCallback != null ) {
                    errorCallback.run( );
                }
            }
            if ( finalCallback != null ) {
                finalCallback.run( );
            }
        } );
        thread.start( );
        return thread;
    }

    public String getApplicationLayerVersion( ) {
        Session localSession = getSession( );
        String serverVersion = null;
        if ( localSession != null ) {
            serverVersion = localSession.getServerVersion( );
        }
        return serverVersion;
    }
}
