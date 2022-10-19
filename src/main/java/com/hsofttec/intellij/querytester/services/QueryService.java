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

import com.ceyoniq.nscale.al.core.MasterdataService;
import com.ceyoniq.nscale.al.core.RepositoryService;
import com.ceyoniq.nscale.al.core.ServerException;
import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.MasterdataPropertyName;
import com.ceyoniq.nscale.al.core.common.PropertyName;
import com.ceyoniq.nscale.al.core.common.ResultTable;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataKey;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataResults;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceResultTable;
import com.ceyoniq.nscale.al.core.repository.ResourceResults;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.models.NscaleQueryInformation;
import com.hsofttec.intellij.querytester.models.NscaleResult;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.ui.Notifier;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class QueryService {
    private static final ConnectionService connectionService = ConnectionService.getInstance( );
    private static final SettingsState SETTINGS = SettingsService.getSettings( );

    public NscaleResult proccessQuery( NscaleQueryInformation queryInformation,
                                       int pageNumber ) {
        NscaleResult nscaleResult = null;

        if ( queryInformation == null || StringUtils.isBlank( queryInformation.getNqlQuery( ) ) ) {
            Notifier.warning( "query is blank, query execution stopped" );
            return null;
        }

        try {

            switch ( queryInformation.getQueryMode( ) ) {
                case REPOSITORY:
                    nscaleResult = proccessRepositoryQuery( queryInformation, pageNumber );
                    break;
                case MASTERDATA:
                    nscaleResult = proccessMasterdatQuery( queryInformation, pageNumber );
                    break;
            }

            return nscaleResult;
        } catch ( IllegalAccessException | InstantiationException e ) {
            throw new RuntimeException( e );
        }
    }

    private NscaleResult proccessMasterdatQuery( NscaleQueryInformation queryInformation, int pageNumber ) throws IllegalAccessException, InstantiationException {
        try {
            Session session = connectionService.getSession( );
            MasterdataService masterdataService = session.getMasterdataService( );
            List<DynaBean> dynaBeans = new ArrayList<>( );

            MasterdataResults masterdataResults = masterdataService.search( queryInformation.getMasterdataScope( ),
                    queryInformation.getDocumentAreaName( ),
                    prepareQuery( queryInformation.getNqlQuery( ), pageNumber ) );
            BasicDynaClass dynaClass = createDynaClass( masterdataResults.getResultTable( ).getPropertyNames( ) );

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

            return new NscaleResult( dynaClass, dynaBeans, masterdataResults.getResultCount( ) );
        } catch ( ServerException | IllegalAccessException | InstantiationException exception ) {
            Notifier.error( String.format( "%s: %s", exception.getClass( ).getSimpleName( ), exception.getLocalizedMessage( ) ) );
            return null;
        }
    }

    private NscaleResult proccessRepositoryQuery( NscaleQueryInformation queryInformation, int pageNumber ) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession( );
        RepositoryService repositoryService = session.getRepositoryService( );
        ResourceKey rootFolder;
        List<DynaBean> dynaBeans = new ArrayList<>( );
        int itemsTotal = 0;

        if ( StringUtils.isBlank( queryInformation.getRepositoryRoot( ) ) ) {
            rootFolder = repositoryService.getRootFolder( queryInformation.getDocumentAreaName( ) );
        } else {
            try {
                rootFolder = new ResourceKey( queryInformation.getRepositoryRoot( ) );
            } catch ( Exception e ) {
                Notifier.error( String.format( "parent resource id '%s': %s", queryInformation.getRepositoryRoot( ), e.getLocalizedMessage( ) ) );
                return null;
            }
            if ( !repositoryService.exists( rootFolder ) ) {
                Notifier.error( String.format( "parent resource id '%s' not exists", queryInformation.getRepositoryRoot( ) ) );
                return null;
            }
        }

        ResultTable resultTable = null;
        try {
            switch ( queryInformation.getQueryType( ) ) {
                case DEFAULT:
                    ResourceResults results = repositoryService.search( rootFolder, prepareQuery( queryInformation.getNqlQuery( ), pageNumber ) );
                    itemsTotal = results.getResultCount( );
                    resultTable = results.getResultTable( );
                    break;
                case AGGREGATE:
                    resultTable = repositoryService.searchAggregate( rootFolder, prepareQuery( queryInformation.getNqlQuery( ), pageNumber ) );
                    itemsTotal = resultTable.getRowCount( );
                    break;
                case VERSION:
                    ResourceResults versionResult = repositoryService.searchVersions( rootFolder, prepareQuery( queryInformation.getNqlQuery( ), pageNumber ) );
                    resultTable = versionResult.getResultTable( );
                    itemsTotal = versionResult.getResultCount( );
                    break;
                case AGGREGATE_AND_VERSION:
                    resultTable = repositoryService.searchVersionsAggregate( rootFolder, prepareQuery( queryInformation.getNqlQuery( ), pageNumber ) );
                    itemsTotal = resultTable.getRowCount( );
                    break;
            }
        } catch ( ServerException serverException ) {
            Notifier.error( serverException.getLocalizedMessage( ) );
            return null;
        }

        BasicDynaClass dynaClass = createDynaClass( resultTable.getPropertyNames( ) );
        int counter = 0;
        for ( int rowCounter = 0; rowCounter < resultTable.getRowCount( ); rowCounter++ ) {
            DynaBean dynaBean = dynaClass.newInstance( );
            int functionFieldPosition = 0;

            if ( resultTable instanceof ResourceResultTable ) {
                dynaBean.set( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, ( ( ResourceResultTable ) resultTable ).getResourceKeys( )[ rowCounter ].getResourceId( ) );
            }
            dynaBean.set( QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, ++counter );

            for ( int columnCounter = 0; columnCounter < resultTable.getColumnCount( ); columnCounter++ ) {
                String propertyName = resultTable.getPropertyNames( )[ columnCounter ].getName( );
                if ( propertyName.equals( "*" ) ) {
                    propertyName = createFieldFunctionPropertyName( functionFieldPosition );
                    functionFieldPosition++;
                }
                dynaBean.set( propertyName, resultTable.getCell( columnCounter, rowCounter ) );
            }
            dynaBeans.add( dynaBean );
        }

        return new NscaleResult( dynaClass, dynaBeans, itemsTotal );
    }

    /**
     * insert paging if not exists in query and paging size in settings greater 100
     *
     * @param nqlQuery query
     */
    private String prepareQuery( String nqlQuery, int pageNumber ) {
        if ( !nqlQuery.contains( "count" ) ) {
            nqlQuery = String.format( "%s count", nqlQuery );
        }

        if ( SETTINGS.getMaxResultSize( ) > 0 ) {
            if ( !nqlQuery.contains( "paging" ) ) {
                String pagingString = String.format( " paging(number=%d, size=%d) ", pageNumber, SETTINGS.getMaxResultSize( ) );
                int lastIndexOfScope = nqlQuery.lastIndexOf( "scope" );
                StringBuilder builder = new StringBuilder( nqlQuery );
                if ( lastIndexOfScope > -1 ) {
                    builder.insert( lastIndexOfScope, pagingString );
                } else {
                    builder.append( pagingString );
                }
                nqlQuery = builder.toString( );
            }
        }
        return nqlQuery;
    }

    /**
     * create the dynamic class
     *
     * @param propertyNames property name array
     */
    private BasicDynaClass createDynaClass( PropertyName[] propertyNames ) {
        int fieldFunctionCounter = 0;
        List<DynaProperty> dynaProperties = new ArrayList<>( );
        dynaProperties.add( new DynaProperty( QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, Integer.class ) );
        dynaProperties.add( new DynaProperty( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, String.class ) );
        for ( PropertyName propertyName : propertyNames ) {
            String propName = propertyName.getName( );
            if ( propName.equals( "*" ) ) {
                propName = createFieldFunctionPropertyName( fieldFunctionCounter );
                fieldFunctionCounter++;
            }
            dynaProperties.add( new DynaProperty( propName, Object.class ) );
        }
        return new BasicDynaClass( QueryTesterConstants.DBEAN_NAME, null, dynaProperties.toArray( new DynaProperty[ 0 ] ) );
    }

    public String createFieldFunctionPropertyName( int counter ) {
        return String.format( "%s%d", QueryTesterConstants.DBEAN_PROPERTY_PRE_NAME_FOR_NQL_FUNC, counter );
    }
}
