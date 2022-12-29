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

import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ToolbarLabelAction;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.components.BorderLayoutPanel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class QueryResultTablePanel extends BorderLayoutPanel {
    private static final SettingsState SETTINGS = SettingsService.getSettings();
    private final QueryTab queryTab;

    @Getter
    private NscaleTable queryResultTable;

    @Getter
    private Component actionToolbarComponent;

    public QueryResultTablePanel( QueryTab queryTab ) {
        this.queryTab = queryTab;
        queryResultTable = new NscaleTable(queryTab.getQueryTester());

        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, createActionGroup(), true);
        actionToolbar.setTargetComponent(queryResultTable);
        actionToolbarComponent = actionToolbar.getComponent();
        actionToolbarComponent.setEnabled(false);

        add(actionToolbarComponent, BorderLayout.NORTH);
        add(new JBScrollPane(queryResultTable), BorderLayout.CENTER);
    }

    private ActionGroup createActionGroup() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new ToolbarLabelAction() {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                int totalSelectedItems = 0;
                if (queryTab.getQueryInformation() != null) {
                    totalSelectedItems = queryTab.getQueryInformation().getTotalSelectedItems();
                }
                anActionEvent.getPresentation().setText(String.format("overall %d items found", totalSelectedItems));
            }
        });
        actionGroup.addSeparator();

        AnAction gotoFirstPageAction = new AnAction("First Page", null, AllIcons.Actions.Play_first) {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                if (queryTab.getResultPageNumber() == 1 || queryTab.getQueryInformation().getTotalSelectedItems() < 0) {
                    anActionEvent.getPresentation().setEnabled(false);
                } else {
                    anActionEvent.getPresentation().setEnabled(true);
                }
            }

            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                queryTab.setResultPageNumber(1);
                queryTab.startQueryExecution();
            }
        };

        actionGroup.add(gotoFirstPageAction);
        actionGroup.add(new AnAction("Prev. Page", null, AllIcons.Actions.Play_back) {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                if (queryTab.getResultPageNumber() == 1 || queryTab.getQueryInformation().getTotalSelectedItems() < 0) {
                    anActionEvent.getPresentation().setEnabled(false);
                } else {
                    anActionEvent.getPresentation().setEnabled(true);
                }
            }

            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                queryTab.setResultPageNumber(queryTab.getResultPageNumber() - 1);
                queryTab.startQueryExecution();
            }
        });
        actionGroup.add(new ToolbarLabelAction() {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                if (queryTab.getQueryInformation() != null) {
                    long pages = (queryTab.getQueryInformation().getTotalSelectedItems() % SETTINGS.getMaxResultSize()) == 0 ? (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) : (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) + 1;
                    anActionEvent.getPresentation().setText(String.format("%d of %d page/s", queryTab.getResultPageNumber(), pages));
                }
            }
        });
        actionGroup.add(new AnAction("Next Page", null, AllIcons.Actions.Play_forward) {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                if (queryTab.getQueryInformation() == null || queryTab.getQueryInformation().getTotalSelectedItems() < 1) {
                    anActionEvent.getPresentation().setEnabled(false);
                } else {
                    long pages = (queryTab.getQueryInformation().getTotalSelectedItems() % SETTINGS.getMaxResultSize()) == 0 ? (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) : (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) + 1;
                    if (queryTab.getResultPageNumber() < pages) {
                        anActionEvent.getPresentation().setEnabled(true);
                    } else {
                        anActionEvent.getPresentation().setEnabled(false);
                    }
                }
            }

            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                queryTab.setResultPageNumber(queryTab.getResultPageNumber() + 1);
                queryTab.startQueryExecution();
            }
        });
        actionGroup.add(new AnAction("Last Page", null, AllIcons.Actions.Play_last) {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                if (queryTab.getQueryInformation() == null || queryTab.getQueryInformation().getTotalSelectedItems() < 1) {
                    anActionEvent.getPresentation().setEnabled(false);
                } else {
                    long pages = (queryTab.getQueryInformation().getTotalSelectedItems() % SETTINGS.getMaxResultSize()) == 0 ? (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) : (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) + 1;
                    if (queryTab.getResultPageNumber() < pages) {
                        anActionEvent.getPresentation().setEnabled(true);
                    } else {
                        anActionEvent.getPresentation().setEnabled(false);
                    }
                }
            }

            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                int resultPageNumber = (queryTab.getQueryInformation().getTotalSelectedItems() % SETTINGS.getMaxResultSize()) == 0 ? (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) : (queryTab.getQueryInformation().getTotalSelectedItems() / SETTINGS.getMaxResultSize()) + 1;
                queryTab.setResultPageNumber(resultPageNumber);
                queryTab.startQueryExecution();
            }
        });
        actionGroup.addSeparator();
        actionGroup.add(new AnAction("Reload", null, AllIcons.Actions.Refresh) {
            @Override
            public void update( @NotNull AnActionEvent anActionEvent ) {
                if (queryTab.getQueryInformation() == null || queryTab.getQueryInformation().getTotalSelectedItems() < 1) {
                    anActionEvent.getPresentation().setEnabled(false);
                } else {
                    anActionEvent.getPresentation().setEnabled(true);
                }
            }

            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                queryTab.startQueryExecution();
            }
        });

        return actionGroup;
    }

    @Override
    public void setEnabled( boolean enabled ) {
        super.setEnabled(enabled);
        queryResultTable.setEnabled(enabled);
    }
}
