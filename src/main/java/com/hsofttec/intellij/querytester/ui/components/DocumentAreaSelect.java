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

import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.DocumentArea;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.states.ConnectionSettings;
import com.hsofttec.intellij.querytester.ui.notifiers.DocumentAreaChangedNotifier;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentAreaSelect extends ComboBox<String> implements ItemListener {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );

    private final Project project;
    private final QueryTab owner;
    private final List<String> docAreaNames = new ArrayList<>( );

    public DocumentAreaSelect( QueryTab owner ) {
        this.project = owner.getQueryTester( ).getProject( );
        this.owner = owner;
        setModel( new CollectionComboBoxModel<>( docAreaNames ) );
        addItemListener( this );
    }

    @Override
    public void setSelectedIndex( int index ) {
        super.setSelectedIndex( index );
        DocumentAreaChangedNotifier notifier = project.getMessageBus( ).syncPublisher( DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC );
        notifier.doAction( ( String ) getSelectedItem( ) );
    }

    public void reloadDocumentAreas( ConnectionSettings settings ) {
        docAreaNames.clear( );
        setSelectedIndex( -1 );

        if ( settings != null ) {
            Session session = CONNECTION_SERVICE.getSession( );
            if ( session != null ) {
                List<DocumentArea> documentAreas = CONNECTION_SERVICE.getSession( ).getConfigurationService( ).getDocumentAreas( );
                docAreaNames.addAll( documentAreas.stream( ).map( DocumentArea::getAreaName ).collect( Collectors.toList( ) ) );
                if ( !documentAreas.isEmpty( ) ) {
                    setSelectedIndex( 0 );
                }
            }
        }
    }

    @Override
    public void itemStateChanged( ItemEvent itemEvent ) {
        if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
            DocumentAreaChangedNotifier notifier = project.getMessageBus( ).syncPublisher( DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC );
            notifier.doAction( ( String ) itemEvent.getItem( ) );
        }
    }
}

