package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

public class StartQueryExecutionEvent {
    private final QueryExecutionParameters parameters;

    public StartQueryExecutionEvent( ConnectionSettingsService.ConnectionSettings connectionSettings,
                                     QueryMode queryMode,
                                     String documentAreaName,
                                     String masterdataScope,
                                     String rootResourceId,
                                     String nqlQuery ) {
        parameters = new QueryExecutionParameters( connectionSettings, queryMode, documentAreaName, masterdataScope, rootResourceId, nqlQuery );
    }

    public QueryExecutionParameters getData( ) {
        return parameters;
    }

    public static class QueryExecutionParameters {
        private final ConnectionSettingsService.ConnectionSettings connectionSettings;
        private final QueryMode queryMode;
        private final String documentAreaName;
        private final String masterdataScope;
        private final String rootResourceId;
        private final String nqlQuery;

        public QueryExecutionParameters( ConnectionSettingsService.ConnectionSettings connectionSettings,
                                         QueryMode queryMode,
                                         String documentAreaName,
                                         String masterdataScope,
                                         String rootResourceId,
                                         String nqlQuery ) {
            this.connectionSettings = connectionSettings;
            this.queryMode = queryMode;
            this.documentAreaName = documentAreaName;
            this.masterdataScope = masterdataScope;
            this.rootResourceId = rootResourceId;
            this.nqlQuery = nqlQuery;
        }

        public ConnectionSettingsService.ConnectionSettings getConnectionSettings( ) {
            return connectionSettings;
        }

        public String getRootResourceId( ) {
            return rootResourceId;
        }

        public String getNqlQuery( ) {
            return nqlQuery;
        }

        public String getDocumentAreaName( ) {
            return documentAreaName;
        }

        public QueryMode getQueryMode( ) {
            return queryMode;
        }

        public String getMasterdataScope( ) {
            return masterdataScope;
        }
    }
}
