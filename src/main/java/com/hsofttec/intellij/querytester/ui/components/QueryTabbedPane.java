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

import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.utils.QueryTabMap;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class QueryTabbedPane extends JBTabbedPane {
    private final QueryTabMap queryTabMap = new QueryTabMap();
    private final QueryTester queryTester;

    public QueryTabbedPane( QueryTester queryTester ) {
        this.queryTester = queryTester;

        AnAction action1 = new AnAction() {
            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                QueryTab queryTab = getActiveQueryTab();
                if (queryTab != null) {
                    NscaleTable queryResultTable = queryTab.getQueryResultTable();
                    queryResultTable.incrementHeaderWidth();
                }
            }
        };

        AnAction action2 = new AnAction() {
            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                QueryTab queryTab = getActiveQueryTab();
                if (queryTab != null) {
                    NscaleTable queryResultTable = queryTab.getQueryResultTable();
                    queryResultTable.calcHeaderWidth();
                }
            }
        };

        action1.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK)), this);
        action2.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)), this);
    }

    public void createQueryTab() {
        int tabId = getTabCount();
        String tabTitle = String.format("Query %d", tabId + 1);
        QueryTab queryTab = new QueryTab(queryTester, this, tabTitle);
        queryTabMap.put(tabId, queryTab);

        if (getTabCount() > 1) {
            for (Integer integer : queryTabMap.keySet()) {
                QueryTab queryTab1 = queryTabMap.get(integer);
                queryTab1.addCloseButton();
            }
        }

        ApplicationManager.getApplication().invokeLater(() -> queryTab.getQueryTextbox().requestFocus());
    }

    public QueryTab getActiveQueryTab() {
        return queryTabMap.get(getSelectedIndex());
    }

    @Override
    public void setEnabled( boolean enabled ) {
        QueryTab activeQueryTab = getActiveQueryTab();
        if (activeQueryTab != null) {
            activeQueryTab.getQueryResultTable().setEnabled(enabled);
            activeQueryTab.getQueryTextbox().setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @Override
    public void removeTabAt( int index ) {
        queryTabMap.remove(index);
        super.removeTabAt(index);

        if (getTabCount() == 1) {
            for (Integer integer : queryTabMap.keySet()) {
                QueryTab queryTab1 = queryTabMap.get(integer);
                queryTab1.removeCloseButton();
            }
        }
    }
}
