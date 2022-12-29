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

import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.intellij.ui.components.JBList;

import javax.swing.*;

public class ConnectionList extends JBList<ConnectionSettings> {
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );

    public ConnectionList( ) {
        super( connectionSettingsService.connectionSettingsState.connectionSettings );
        setCellRenderer( new ConnectionListCellRenderer( ) );
    }

    public ConnectionSettings getSelectedItem( ) {
        ListModel<ConnectionSettings> model = getModel( );
        return model.getElementAt( getSelectedIndex( ) );
    }

    public void addElement( ConnectionSettings connectionSettings ) {
        DefaultListModel<ConnectionSettings> model = ( DefaultListModel<ConnectionSettings> ) getModel( );
        model.addElement( connectionSettings );
    }

    public void removeElement( ConnectionSettings connectionSettings ) {
        DefaultListModel<ConnectionSettings> model = ( DefaultListModel<ConnectionSettings> ) getModel( );
        model.removeElement( connectionSettings );
    }
}
