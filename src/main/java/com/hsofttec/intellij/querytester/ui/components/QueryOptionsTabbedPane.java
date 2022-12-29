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
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import lombok.Getter;

import javax.swing.*;

public class QueryOptionsTabbedPane extends JBTabbedPane {
    private final QueryTester queryTester;
    private final QueryTab owner;

    @Getter
    private RepositoryRootTextField inputRepositoryRoot;

    @Getter
    private JCheckBox inputAggregate;

    @Getter
    private JCheckBox inputVersion;

    @Getter
    private MasterdataScopeSelect inputMasterdataScope;

    public QueryOptionsTabbedPane( QueryTab owner ) {
        this.queryTester = owner.getQueryTester();
        this.owner = owner;
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        addTab("Repository", createRepositoryPanel());
        addTab("Masterdata", createMasterdataPanel());
        setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * create panel for all repository query options
     *
     * @return repository options panel
     */
    private JPanel createRepositoryPanel() {
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "5px, left:pref, 4dlu, pref:grow",
                "pref, 3dlu, pref, 3dlu, pref"
        );
        JPanel repositoryPanel = new JPanel(formLayout);
        inputRepositoryRoot = new RepositoryRootTextField(queryTester.getProject());
        repositoryPanel.add(new JBLabel("Root resource"), cc.xy(2, 1));
        repositoryPanel.add(inputRepositoryRoot, cc.xy(4, 1));

        inputAggregate = new JBCheckBox("Aggregate");
        repositoryPanel.add(inputAggregate, cc.xy(4, 3));

        inputVersion = new JBCheckBox("Version");
        repositoryPanel.add(inputVersion, cc.xy(4, 5));

        return repositoryPanel;
    }

    /**
     * create panel for all masterdata query options
     *
     * @return masterdata options panel
     */
    private JPanel createMasterdataPanel() {
        CellConstraints cc = new CellConstraints();
        FormLayout formLayout = new FormLayout(
                "5px, left:pref, 4dlu, pref:grow",
                "pref, 3dlu, pref, 3dlu, pref"
        );
        JPanel masterdataPanel = new JPanel(formLayout);

        inputMasterdataScope = new MasterdataScopeSelect(queryTester.getProject());
        masterdataPanel.add(new JBLabel("Scope"), cc.xy(2, 1));
        masterdataPanel.add(inputMasterdataScope, cc.xy(4, 1));

        return masterdataPanel;
    }

    @Override
    public void setEnabled( boolean enabled ) {
        super.setEnabled(enabled);
        inputRepositoryRoot.setEnabled(enabled);
        inputAggregate.setEnabled(enabled);
        inputMasterdataScope.setEnabled(enabled);
        inputVersion.setEnabled(enabled);
    }
}
