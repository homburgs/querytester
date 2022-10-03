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

package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.QueryMode;
import com.hsofttec.intellij.querytester.models.ConnectionSettings;

public class StartQueryExecutionEvent {
    private final QueryExecutionParameters parameters;

    public StartQueryExecutionEvent( ConnectionSettings connectionSettings,
                                     QueryMode queryMode,
                                     String documentAreaName,
                                     String masterdataScope,
                                     String rootResourceId,
                                     String nqlQuery,
                                     boolean aggregate ) {
        parameters = new QueryExecutionParameters( connectionSettings, queryMode, documentAreaName, masterdataScope, rootResourceId, nqlQuery, aggregate );
    }

    public QueryExecutionParameters getData( ) {
        return parameters;
    }

    public static class QueryExecutionParameters {
        private final ConnectionSettings connectionSettings;
        private final QueryMode queryMode;
        private final String documentAreaName;
        private final String masterdataScope;
        private final String rootResourceId;
        private final String nqlQuery;
        private final boolean aggregate;

        public QueryExecutionParameters( ConnectionSettings connectionSettings,
                                         QueryMode queryMode,
                                         String documentAreaName,
                                         String masterdataScope,
                                         String rootResourceId,
                                         String nqlQuery,
                                         boolean aggregate ) {
            this.connectionSettings = connectionSettings;
            this.queryMode = queryMode;
            this.documentAreaName = documentAreaName;
            this.masterdataScope = masterdataScope;
            this.rootResourceId = rootResourceId;
            this.nqlQuery = nqlQuery;
            this.aggregate = aggregate;
        }

        public ConnectionSettings getConnectionSettings( ) {
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

        public boolean isAggregate( ) {
            return aggregate;
        }
    }
}
