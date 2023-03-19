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

package com.hsofttec.intellij.querytester.ui.components;

import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.MasterdataScope;
import com.hsofttec.intellij.querytester.renderer.MasterdataListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class MasterdataScopeSelect extends ComboBox<MasterdataScope> {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();

    private final List<MasterdataScope> scopes = new ArrayList<>();
    private final Project project;

    public MasterdataScopeSelect( Project project ) {
        this.project = project;
        setModel(new CollectionComboBoxModel<>(scopes));
        setRenderer(new MasterdataListCellRenderer());
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged( ItemEvent event ) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    MasterdataScope item = (MasterdataScope) event.getItem();
                    System.err.println(item);
                }
            }
        });
    }


    public void reloadMasterdataScopes( String documentArea ) {
        Session session = CONNECTION_SERVICE.getSession();
        scopes.clear();
        setSelectedIndex(-1);
        if (session != null) {
            List<MasterdataScope> masterdataScopes = session.getConfigurationService().getMasterdataScopes(documentArea);
            scopes.addAll(masterdataScopes);
            if (!scopes.isEmpty()) {
                setSelectedIndex(0);
            }
        }
    }
}

