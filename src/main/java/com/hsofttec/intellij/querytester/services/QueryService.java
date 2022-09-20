package com.hsofttec.intellij.querytester.services;

import com.ceyoniq.nscale.al.core.*;
import com.ceyoniq.nscale.al.core.cfg.FulltextPropertyName;
import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyName;
import com.ceyoniq.nscale.al.core.cfg.MasterdataPropertyName;
import com.ceyoniq.nscale.al.core.cfg.PropertyDefinition;
import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import com.ceyoniq.nscale.al.core.common.PropertyName;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataKey;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataResults;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceResults;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.models.NscaleResult;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.ui.Notifier;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryService {
    private static final ConnectionService connectionService = ConnectionService.getInstance( );
    private static final SettingsState SETTINGS = SettingsService.getSettings( );

    public NscaleResult proccessQuery( ConnectionSettingsService.ConnectionSettings configuration,
                                       QueryMode queryMode,
                                       String documentAreaName,
                                       String masterdataScope,
                                       String repositoryRoot,
                                       String nqlQuery ) {
        NscaleResult nscaleResult = null;

        if ( StringUtils.isBlank( nqlQuery ) ) {
            Notifier.warning( "query is blank, query execution stopped" );
            return null;
        }

        try {

            switch ( queryMode ) {
                case REPOSITORY:
                    nscaleResult = proccessRepositoryQuery( documentAreaName, repositoryRoot, nqlQuery );
                    break;
                case MASTERDATA:
                    nscaleResult = proccessMasterdatQuery( documentAreaName, masterdataScope, nqlQuery );
                    break;
            }

            return nscaleResult;
        } catch ( IllegalAccessException | InstantiationException e ) {
            throw new RuntimeException( e );
        }
    }

    private NscaleResult proccessMasterdatQuery( String documentAreaName, String masterdataScope, String nqlQuery ) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession( );
        MasterdataService masterdataService = session.getMasterdataService( );
        ConfigurationService configurationService = session.getConfigurationService( );
        List<DynaBean> dynaBeans = new ArrayList<>( );

        MasterdataResults masterdataResults = masterdataService.search( masterdataScope, documentAreaName, nqlQuery );
        BasicDynaClass dynaClass = createDynaClass( configurationService, masterdataResults.getResultTable( ).getPropertyNames( ) );

        int counter = 0;
        for ( MasterdataKey masterdataKey : masterdataResults.getResultTable( ).getMasterdataKeys( ) ) {
            DynaBean dynaBean = dynaClass.newInstance( );
            dynaBean.set( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, masterdataKey.getMasterdataId( ) );
            dynaBean.set( QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, ++counter );
            for ( PropertyName propertyName : masterdataResults.getResultTable( ).getPropertyNames( ) ) {
                if ( dynaBean.getDynaClass( ).getDynaProperty( propertyName.getName( ) ) != null ) {
                    Object value = masterdataResults.getResultTable( ).getCell( ( MasterdataPropertyName ) propertyName, masterdataKey.getValue( ) );
                    dynaBean.set( propertyName.getName( ), value );
                }
            }
            dynaBeans.add( dynaBean );
        }

        return new NscaleResult( dynaClass, dynaBeans );
    }

    private NscaleResult proccessRepositoryQuery( String documentAreaName, String repositoryRoot, String nqlQuery ) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession( );
        RepositoryService repositoryService = session.getRepositoryService( );
        ConfigurationService configurationService = session.getConfigurationService( );
        ResourceKey rootFolder;
        List<DynaBean> dynaBeans = new ArrayList<>( );

        if ( StringUtils.isBlank( repositoryRoot ) ) {
            rootFolder = repositoryService.getRootFolder( documentAreaName );
        } else {
            try {
                rootFolder = new ResourceKey( repositoryRoot );
            } catch ( Exception e ) {
                Notifier.error( String.format( "parent resource id '%s': %s", repositoryRoot, e.getLocalizedMessage( ) ) );
                return null;
            }
            if ( !repositoryService.exists( rootFolder ) ) {
                Notifier.error( String.format( "parent resource id '%s' not exists", repositoryRoot ) );
                return null;
            }
        }

        ResourceResults resourceResults = null;
        try {
            resourceResults = repositoryService.search( rootFolder, nqlQuery );
        } catch ( ServerException serverException ) {
            Notifier.error( serverException.getLocalizedMessage( ) );
            return null;
        }

        BasicDynaClass dynaClass = createDynaClass( configurationService, resourceResults.getResultTable( ).getPropertyNames( ) );
        int counter = 0;
        for ( ResourceKey resourceKey : resourceResults.getResultTable( ).getResourceKeys( ) ) {
            DynaBean dynaBean = dynaClass.newInstance( );
            dynaBean.set( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, resourceKey.getResourceId( ) );
            dynaBean.set( QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, ++counter );
            for ( PropertyName propertyName : resourceResults.getResultTable( ).getPropertyNames( ) ) {
                if ( dynaBean.getDynaClass( ).getDynaProperty( propertyName.getName( ) ) != null ) {
                    Object value = resourceResults.getResultTable( ).getCell( propertyName, resourceKey );
                    dynaBean.set( propertyName.getName( ), value );
                }
            }
            dynaBeans.add( dynaBean );
        }

        return new NscaleResult( dynaClass, dynaBeans );
    }

    private BasicDynaClass createDynaClass( ConfigurationService configurationService, PropertyName[] propertyNames ) {
        DynaProperty[] dynaProperties = new DynaProperty[]{ };
        dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, Integer.class ) );
        dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, String.class ) );
        for ( PropertyName propertyName : propertyNames ) {
            PropertyDefinition definition = getPropertyDefinition( configurationService, propertyName );
            if ( definition == null ) {
                Notifier.error( String.format( "definition for property '%s' not found", propertyName.getName( ) ) );
                continue;
            }
            if ( definition.isMultiValue( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), ArrayList.class ) );
            } else if ( definition.getType( ).isStringType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), String.class ) );
            } else if ( definition.getType( ).isDateType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), Date.class ) );
            } else if ( definition.getType( ).isIntegerType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), Integer.class ) );
            } else if ( definition.getType( ).isDoubleType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), Double.class ) );
            } else if ( definition.getType( ).isBlobType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), byte[].class ) );
            } else if ( definition.getType( ).isLongType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), Long.class ) );
            } else if ( definition.getType( ).isBooleanType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), Boolean.class ) );
            } else if ( definition.getType( ).isTimestampType( ) ) {
                dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), DateTime.class ) );
            } else if ( definition.getType( ).isAreaQualifiedIdentifierType( ) ) {
                if ( definition.getName( ).getName( ).equals( "objectclass" ) ) {
                    dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), ObjectclassName.class ) );
                } else if ( definition.getName( ).getName( ).equals( "formname" ) ) {
                    dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), String.class ) );
                } else if ( definition.getName( ).getName( ).equals( "foldertemplatename" ) ) {
                    dynaProperties = ( DynaProperty[] ) ArrayUtils.add( dynaProperties, new DynaProperty( definition.getName( ).getName( ), String.class ) );
                }
            } else {
                Notifier.error( String.format( "type for property '%s' unknow", definition.getName( ).getName( ) ) );
            }
        }

        return new BasicDynaClass( "record", null, dynaProperties );
    }

    private PropertyDefinition getPropertyDefinition( ConfigurationService configurationService, PropertyName propertyName ) {
        PropertyDefinition definition = null;

        if ( propertyName instanceof IndexingPropertyName ) {
            definition = configurationService.getIndexingPropertyDefinition( ( IndexingPropertyName ) propertyName );
            if ( definition == null ) {
                definition = configurationService.getComputedIndexingPropertyDefinition( ( IndexingPropertyName ) propertyName );
            }
        }

        if ( propertyName instanceof MasterdataPropertyName ) {
            definition = configurationService.getMasterdataPropertyDefinition( ( MasterdataPropertyName ) propertyName );
            if ( definition == null ) {
                definition = configurationService.getComputedMasterdataPropertyDefinition( ( MasterdataPropertyName ) propertyName );
            }
        }

        if ( propertyName instanceof FulltextPropertyName ) {
            definition = configurationService.getFulltextPropertyDefinition( ( FulltextPropertyName ) propertyName );
        }

        return definition;
    }
}
