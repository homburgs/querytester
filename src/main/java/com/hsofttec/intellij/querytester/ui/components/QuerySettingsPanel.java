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
import com.intellij.ui.components.JBLabel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import lombok.Getter;

import javax.swing.*;

public class QuerySettingsPanel extends JPanel {
    private final QueryTester queryTester;
    private final QueryTab owner;

    @Getter
    private ConnectionSelect inputSelectedConnection;

    @Getter
    private JComboBox<String> inputHistory;

    @Getter
    private ReconnectIcon iconReconnect;

    @Getter
    private DocumentAreaSelect inputDocumentArea;

    public QuerySettingsPanel( QueryTab owner ) {
        this.owner = owner;
        this.queryTester = owner.getQueryTester( );

        int formRow;
        int[] formColNums = { 2, 4, 6 };
        CellConstraints cc = new CellConstraints( );
        FormLayout formLayout = new FormLayout(
                "5px, left:pref, 4dlu, pref:grow, 4dlu, pref,5px",
                "pref, 3dlu, pref, 3dlu, pref"
        );

        setLayout( formLayout );
        setBorder( BorderFactory.createEtchedBorder( ) );

        formRow = 1;
        inputSelectedConnection = new ConnectionSelect( owner );
        iconReconnect = new ReconnectIcon( inputSelectedConnection );

        add( new JBLabel( "Connection" ), cc.xy( formColNums[ 0 ], formRow ) );
        add( inputSelectedConnection, cc.xy( formColNums[ 1 ], formRow ) );
        add( iconReconnect, cc.xy( formColNums[ 2 ], formRow ) );

        formRow += 2;
        inputDocumentArea = new DocumentAreaSelect( owner );
        add( new JBLabel( "Document area" ), cc.xy( formColNums[ 0 ], formRow ) );
        add( inputDocumentArea, cc.xyw( formColNums[ 1 ], formRow, 3 ) );

        formRow += 2;
        inputHistory = new HistorySelect( owner );
        inputHistory.setPrototypeDisplayValue( "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" );
        add( new JBLabel( "History" ), cc.xy( formColNums[ 0 ], formRow ) );
        add( inputHistory, cc.xyw( formColNums[ 1 ], formRow, 3 ) );
    }


    @Override
    public void setEnabled( boolean enabled ) {
        super.setEnabled( enabled );
        getIconReconnect( ).setEnabled( enabled );
        getInputHistory( ).setEnabled( enabled );
        getInputDocumentArea( ).setEnabled( enabled );
        getInputSelectedConnection( ).setEnabled( enabled );
    }
}
