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
import com.ceyoniq.nscale.al.core.common.AggregateResults;
import com.ceyoniq.nscale.al.core.common.PropertyName;
import com.ceyoniq.nscale.al.core.common.ResultTable;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataKey;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataResults;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceResultTable;
import com.ceyoniq.nscale.al.core.repository.ResourceResults;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;
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

    public NscaleResult proccessQuery( ConnectionSettings configuration,
                                       QueryMode queryMode,
                                       String documentAreaName,
                                       String masterdataScope,
                                       String repositoryRoot,
                                       String nqlQuery,
                                       boolean aggregate ) {
        NscaleResult nscaleResult = null;

        if ( StringUtils.isBlank( nqlQuery ) ) {
            Notifier.warning( "query is blank, query execution stopped" );
            return null;
        }

        try {

            switch ( queryMode ) {
                case REPOSITORY:
                    nscaleResult = proccessRepositoryQuery( documentAreaName, repositoryRoot, nqlQuery, aggregate );
                    break;
                case MASTERDATA:
                    nscaleResult = proccessMasterdatQuery( documentAreaName, masterdataScope, nqlQuery, aggregate );
                    break;
            }

            return nscaleResult;
        } catch ( IllegalAccessException | InstantiationException e ) {
            throw new RuntimeException( e );
        }
    }

    private NscaleResult proccessMasterdatQuery( String documentAreaName, String masterdataScope, String nqlQuery, boolean aggregate ) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession( );
        MasterdataService masterdataService = session.getMasterdataService( );
        List<DynaBean> dynaBeans = new ArrayList<>( );

        MasterdataResults masterdataResults = masterdataService.search( masterdataScope, documentAreaName, prepareQuery( nqlQuery ) );
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

        return new NscaleResult( dynaClass, dynaBeans );
    }

    private NscaleResult proccessRepositoryQuery( String documentAreaName, String repositoryRoot, String nqlQuery, boolean aggregate ) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession( );
        RepositoryService repositoryService = session.getRepositoryService( );
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

        ResultTable resultTable = null;
        try {
            if ( !aggregate ) {
                ResourceResults results = repositoryService.search( rootFolder, prepareQuery( nqlQuery ) );
                resultTable = results.getResultTable( );
            } else {
                AggregateResults results = repositoryService.searchAggregate( rootFolder, prepareQuery( nqlQuery ) );
                resultTable = results;
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

        return new NscaleResult( dynaClass, dynaBeans );
    }

    /**
     * insert paging if not exists in query and paging size in settings greater 100
     *
     * @param nqlQuery query
     */
    private String prepareQuery( String nqlQuery ) {
        if ( SETTINGS.getMaxResultSize( ) > 0 ) {
            if ( !nqlQuery.contains( "paging" ) ) {
                String pagingString = String.format( " paging(number=1, size=%d) ", SETTINGS.getMaxResultSize( ) );
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
