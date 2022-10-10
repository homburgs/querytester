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

package com.hsofttec.intellij.querytester.utils;

import com.hsofttec.intellij.querytester.ui.components.NqlQueryTextbox;
import com.hsofttec.intellij.querytester.ui.components.NscaleTable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.roots.IconActionComponent;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class QueryTab {
    private final Project project;
    private final JBTabbedPane parent;
    private final int tabId;
    private String tabTitle;
    private JBSplitter splitter;
    private NqlQueryTextbox queryTextbox;
    private NscaleTable queryResultTable;

    public QueryTab( Project project, JBTabbedPane parent, int tabId, String tabTitle ) {
        this.project = project;
        this.parent = parent;
        this.tabId = tabId;
        this.tabTitle = tabTitle;

        splitter = new OnePixelSplitter( true, 0.3f );
        splitter.setHonorComponentsMinimumSize( true );
        splitter.setSplitterProportionKey( "query.splitter.key" );

        JPanel queryTextboxPanel = JBUI.Panels.simplePanel( );
        queryTextbox = new NqlQueryTextbox( project );
        queryTextboxPanel.add( new JBScrollPane( queryTextbox ) );

        JPanel queryResultTablePanel = JBUI.Panels.simplePanel( );
        queryResultTable = new NscaleTable( project );
        queryResultTablePanel.add( new JBScrollPane( queryResultTable ) );

        splitter.setFirstComponent( queryTextboxPanel );
        splitter.setSecondComponent( queryResultTablePanel );

        parent.addTab( tabTitle, AllIcons.Actions.Close, splitter );

        int index = parent.indexOfTab( tabTitle );
        JPanel pnlTab = new JPanel( new GridBagLayout( ) );
        pnlTab.setOpaque( false );
        JLabel lblTitle = new JBLabel( tabTitle );
        IconActionComponent btnClose = new IconActionComponent( AllIcons.Actions.Close, null, "Close query tab", ( ) -> {
            int index1 = parent.indexOfTab( tabTitle );
            if ( index1 >= 0 ) {
                parent.removeTabAt( index1 );
            }
        } );

        GridBagConstraints gbc = new GridBagConstraints( );
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        pnlTab.add( lblTitle, gbc );

        gbc.gridx++;
        gbc.weightx = 0;
        pnlTab.add( btnClose, gbc );

        parent.setTabComponentAt( index, pnlTab );
        parent.setSelectedIndex( index );
    }

    public JBSplitter getSplitter( ) {
        return splitter;
    }

    public NqlQueryTextbox getQueryTextbox( ) {
        return queryTextbox;
    }

    public NscaleTable getQueryResultTable( ) {
        return queryResultTable;
    }
}
