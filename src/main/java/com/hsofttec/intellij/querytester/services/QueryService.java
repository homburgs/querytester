/*
 * The MIT License (MIT)
 *
 * Copyright © 2023 Sven Homburg, <homburgs@gmail.com>
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

import com.ceyoniq.nscale.al.core.*;
import com.ceyoniq.nscale.al.core.cfg.MasterdataPropertyName;
import com.ceyoniq.nscale.al.core.common.PropertyName;
import com.ceyoniq.nscale.al.core.common.ResultTable;
import com.ceyoniq.nscale.al.core.identity.PrincipalEntityResults;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataKey;
import com.ceyoniq.nscale.al.core.masterdata.MasterdataResults;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceResultTable;
import com.ceyoniq.nscale.al.core.repository.ResourceResults;
import com.ceyoniq.nscale.al.core.search.AggregateablePropertyName;
import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.NscaleQueryInformation;
import com.hsofttec.intellij.querytester.models.NscaleResult;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.ui.Notifier;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class QueryService {
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    private static final ConnectionService connectionService = ConnectionService.getInstance();
    private static final SettingsState SETTINGS = SettingsService.getSettings();

    public NscaleResult processQuery(NscaleQueryInformation queryInformation, int pageNumber) {
        NscaleResult nscaleResult = null;

        if (queryInformation == null || StringUtils.isBlank(queryInformation.getNqlQuery())) {
            Notifier.warning("query is blank, query execution stopped");
            return null;
        }

        try {

            switch (queryInformation.getQueryMode()) {
                case REPOSITORY -> nscaleResult = processRepositoryQuery(queryInformation, pageNumber);
                case MASTERDATA -> nscaleResult = processMasterdataQuery(queryInformation, pageNumber);
                case PRINCIPALS -> nscaleResult = processPrincipalQuery(queryInformation, pageNumber);
            }

            return nscaleResult;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * processing a masterdata query
     *
     * @param queryInformation query information
     * @param pageNumber       page number to get
     * @return result set
     */
    private NscaleResult processMasterdataQuery(NscaleQueryInformation queryInformation, int pageNumber) throws IllegalAccessException, InstantiationException {
        try {
            Session session = connectionService.getSession();
            MasterdataService masterdataService = session.getMasterdataService();
            List<DynaBean> dynaBeans = new ArrayList<>();

            MasterdataResults masterdataResults = masterdataService.search(queryInformation.getMasterdataScope(),
                    queryInformation.getDocumentAreaName(),
                    prepareQuery(queryInformation.getNqlQuery(), pageNumber));
            BasicDynaClass dynaClass = createDynaClass(masterdataResults.getResultTable().getPropertyNames(), queryInformation.getQueryMode());

            int counter = 0;
            for (MasterdataKey masterdataKey : masterdataResults.getResultTable().getMasterdataKeys()) {
                DynaBean dynaBean = dynaClass.newInstance();
                dynaBean.set(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, masterdataKey.getMasterdataId());
                dynaBean.set(QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, ++counter);
                for (PropertyName propertyName : masterdataResults.getResultTable().getPropertyNames()) {
                    if (dynaBean.getDynaClass().getDynaProperty(propertyName.getName()) != null) {
                        Object value = masterdataResults.getResultTable().getCell((MasterdataPropertyName) propertyName, masterdataKey.getValue());
                        dynaBean.set(propertyName.getName(), value);
                    }
                }
                dynaBeans.add(dynaBean);
            }

            return new NscaleResult(dynaClass, dynaBeans, masterdataResults.getResultCount());
        } catch (ServerException | IllegalAccessException | InstantiationException exception) {
            Notifier.error(String.format("%s: %s", exception.getClass().getSimpleName(), exception.getLocalizedMessage()));
            return null;
        }
    }

    /**
     * processing a repository query
     *
     * @param queryInformation query information
     * @param pageNumber       page number to get
     * @return result set
     */
    private NscaleResult processRepositoryQuery(NscaleQueryInformation queryInformation, int pageNumber) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession();
        RepositoryService repositoryService = session.getRepositoryService();
        ResourceKey rootFolder = resolveRootFolder(repositoryService, queryInformation);
        if (rootFolder == null) {
            return null;
        }

        String nqlQuery = prepareQuery(queryInformation.getNqlQuery(), pageNumber);
        ResultTable resultTable = null;
        int itemsTotal = 0;
        try {
            switch (queryInformation.getQueryType()) {
                case DEFAULT -> {
                    ResourceResults results = repositoryService.search(rootFolder, nqlQuery);
                    itemsTotal = results.getResultCount();
                    resultTable = results.getResultTable();
                }
                case AGGREGATE -> {
                    resultTable = repositoryService.searchAggregate(rootFolder, nqlQuery);
                    itemsTotal = resultTable.getRowCount();
                }
                case VERSION -> {
                    ResourceResults versionResult = repositoryService.searchVersions(rootFolder, nqlQuery);
                    resultTable = versionResult.getResultTable();
                    itemsTotal = versionResult.getResultCount();
                }
                case AGGREGATE_AND_VERSION -> {
                    resultTable = repositoryService.searchVersionsAggregate(rootFolder, nqlQuery);
                    itemsTotal = resultTable.getRowCount();
                }
            }
        } catch (ServerException serverException) {
            Notifier.error(serverException.getLocalizedMessage());
            return null;
        }

        return toNscaleResult(resultTable, queryInformation.getQueryMode(), itemsTotal);
    }

    /**
     * root folder of a repository query: the configured repository root or the root folder of the document area
     *
     * @return root folder key, null if the configured repository root is invalid
     */
    private ResourceKey resolveRootFolder(RepositoryService repositoryService, NscaleQueryInformation queryInformation) {
        String repositoryRoot = queryInformation.getRepositoryRoot();
        if (StringUtils.isBlank(repositoryRoot)) {
            return repositoryService.getRootFolder(queryInformation.getDocumentAreaName());
        }

        ResourceKey rootFolder;
        try {
            rootFolder = new ResourceKey(repositoryRoot);
        } catch (Exception e) {
            Notifier.error(String.format("parent resource id '%s': %s", repositoryRoot, e.getLocalizedMessage()));
            return null;
        }
        if (!repositoryService.exists(rootFolder)) {
            Notifier.error(String.format("parent resource id '%s' not exists", repositoryRoot));
            return null;
        }
        return rootFolder;
    }

    /**
     * processing a principal query
     *
     * @param queryInformation query information
     * @param pageNumber       page number to get
     * @return result set
     */
    private NscaleResult processPrincipalQuery(NscaleQueryInformation queryInformation, int pageNumber) throws IllegalAccessException, InstantiationException {
        Session session = connectionService.getSession();
        UserManagementService userManagementService = session.getUserManagementService();
        String nqlQuery = prepareQuery(queryInformation.getNqlQuery(), pageNumber);
        ResultTable resultTable = null;
        int itemsTotal = 0;

        try {
            switch (queryInformation.getQueryType()) {
                case DEFAULT -> {
                    PrincipalEntityResults results = userManagementService.search(nqlQuery);
                    itemsTotal = results.getResultCount();
                    resultTable = results.getResultTable();
                }
                case AGGREGATE -> {
                    resultTable = userManagementService.searchAggregate(nqlQuery);
                    itemsTotal = resultTable.getRowCount();
                }
            }
        } catch (ServerException serverException) {
            Notifier.error(serverException.getLocalizedMessage());
            return null;
        }

        return toNscaleResult(resultTable, queryInformation.getQueryMode(), itemsTotal);
    }

    /**
     * convert a nscale result table into dynabean rows
     *
     * @param resultTable result table returned by the server
     * @param queryMode   query mode
     * @param itemsTotal  total number of matching items
     * @return result set
     */
    private NscaleResult toNscaleResult(ResultTable resultTable, QueryMode queryMode, int itemsTotal) throws IllegalAccessException, InstantiationException {
        BasicDynaClass dynaClass = createDynaClass(resultTable.getPropertyNames(), queryMode);
        List<DynaBean> dynaBeans = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < resultTable.getRowCount(); rowIndex++) {
            DynaBean dynaBean = dynaClass.newInstance();

            if (resultTable instanceof ResourceResultTable resourceResultTable) {
                ResourceKey resourceKey = resourceResultTable.getResourceKeys()[rowIndex];
                dynaBean.set(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, resourceKey.getResourceId());
                dynaBean.set(QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED, isResourceLocked(resourceKey));
            }
            dynaBean.set(QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, rowIndex + 1);

            for (int columnIndex = 0; columnIndex < resultTable.getColumnCount(); columnIndex++) {
                String propertyName = getPropertyName(resultTable.getPropertyNames()[columnIndex]);
                dynaBean.set(propertyName, resultTable.getCell(columnIndex, rowIndex));
            }
            dynaBeans.add(dynaBean);
        }

        return new NscaleResult(dynaClass, dynaBeans, itemsTotal);
    }

    private String getPropertyName( PropertyName propertyName ) {
        String name = propertyName.getName();
        if (propertyName instanceof AggregateablePropertyName) {
            String functionName = ((AggregateablePropertyName) propertyName).getFunction().name();
            name = String.format("%s_%s", functionName, name);
        }
        return name;
    }

    private boolean isResourceLocked( ResourceKey resourceKey ) {
        BaseResource baseResource = connectionService.getBaseResource(resourceKey.getResourceId());
        return baseResource.isLocked();
    }

    /**
     * insert "paging" or/and "count" if not exists in query
     *
     * @param nqlQuery   query
     * @param pageNumber page number of result set
     */
    private String prepareQuery(String nqlQuery, int pageNumber) {

        if (SETTINGS.getMaxResultSize() > 0) {
            if (!nqlQuery.contains("paging")) {
                String pagingString = String.format(" paging(number=%d, size=%d) ", pageNumber, SETTINGS.getMaxResultSize());
                int lastIndexOfScope = nqlQuery.lastIndexOf("scope");
                StringBuilder builder = new StringBuilder(nqlQuery);
                if (lastIndexOfScope > -1) {
                    builder.insert(lastIndexOfScope, pagingString);
                } else {
                    builder.append(pagingString);
                }
                nqlQuery = builder.toString();
            }
        }

        if (!nqlQuery.contains("count")) {
            nqlQuery = String.format("%s count", nqlQuery);
        }

        return nqlQuery;
    }

    /**
     * create the dynamic class
     *
     * @param propertyNames property name array
     */
    private BasicDynaClass createDynaClass( PropertyName[] propertyNames, QueryMode queryMode ) {
        List<DynaProperty> dynaProperties = new ArrayList<>();
        dynaProperties.add(new DynaProperty(QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO, Integer.class));
        if (queryMode != QueryMode.PRINCIPALS) {
            dynaProperties.add(new DynaProperty(QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY, String.class));
            dynaProperties.add(new DynaProperty(QueryTesterConstants.DBEAN_PROPERTY_NAME_LOCKED, Boolean.class));
        }
        for (PropertyName propertyName : propertyNames) {
            String propName = getPropertyName(propertyName);
            dynaProperties.add(new DynaProperty(propName, Object.class));
        }
        return new BasicDynaClass(QueryTesterConstants.DBEAN_NAME, null, dynaProperties.toArray(new DynaProperty[0]));
    }
}
