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

package com.hsofttec.intellij.querytester.ui.components;

import com.hsofttec.intellij.querytester.ui.notifiers.DocumentAreaChangedNotifier;
import com.hsofttec.intellij.querytester.utils.QueryTab;
import com.hsofttec.intellij.querytester.utils.QueryTabMap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;

import javax.annotation.Nullable;

public class QueryTabbedPane extends JBTabbedPane {
    private final QueryTabMap queryTabMap = new QueryTabMap( );
    private final Project project;

    public QueryTabbedPane( Project project ) {
        this.project = project;
    }

    public void createQueryTab( @Nullable String documentAreaName ) {
        int tabId = getTabCount( ) + 1;
        String tabTitle = String.format( "Query %d", tabId );
        QueryTab queryTab = new QueryTab( project, this, tabId, tabTitle );
        queryTabMap.put( tabId, queryTab );

        if ( documentAreaName != null ) {
            DocumentAreaChangedNotifier notifier = project.getMessageBus( ).syncPublisher( DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC );
            notifier.doAction( documentAreaName );
        }

        ApplicationManager.getApplication( ).invokeLater( ( ) -> queryTab.getQueryTextbox( ).requestFocus( ) );
    }

    public QueryTab getActiveQueryTab( ) {
        return queryTabMap.get( getSelectedIndex( ) + 1 );
    }

    @Override
    public void setEnabled( boolean enabled ) {
        QueryTab activeQueryTab = getActiveQueryTab( );
        if ( activeQueryTab != null ) {
            activeQueryTab.getQueryResultTable( ).setEnabled( enabled );
            activeQueryTab.getQueryTextbox( ).setEnabled( enabled );
        }
        super.setEnabled( enabled );
    }

    @Override
    public void removeTabAt( int index ) {
        queryTabMap.remove( index );
        super.removeTabAt( index );
    }
}
