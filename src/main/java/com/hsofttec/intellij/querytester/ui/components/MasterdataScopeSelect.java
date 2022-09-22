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
import com.ceyoniq.nscale.al.core.cfg.MasterdataScope;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.DocumentAreaChangedEvent;
import com.hsofttec.intellij.querytester.events.MasterdataScopeChangedEvent;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.ui.CollectionComboBoxModel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MasterdataScopeSelect extends JComboBox<String> implements ItemListener {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ConnectionService connectionService = ConnectionService.getInstance( );
    private final List<String> scopes = new ArrayList<>( );

    public MasterdataScopeSelect( ) {
        EVENT_BUS.register( this );
        setModel( new CollectionComboBoxModel<>( scopes ) );
        addItemListener( this );
    }

    @Override
    public void setSelectedIndex( int index ) {
        super.setSelectedIndex( index );
        EVENT_BUS.post( new MasterdataScopeChangedEvent( ( String ) getSelectedItem( ) ) );
    }

    @Subscribe
    public void documentAreaChangedEvent( DocumentAreaChangedEvent event ) {
        Session session = connectionService.getSession( );
        scopes.clear( );
        setSelectedIndex( -1 );
        if ( session != null ) {
            List<MasterdataScope> masterdataScopes = session.getConfigurationService( ).getMasterdataScopes( event.getDocumentAreaName( ) );
            scopes.addAll( masterdataScopes.stream( ).map( MasterdataScope::getDisplayNameId ).collect( Collectors.toList( ) ) );
            if ( !scopes.isEmpty( ) ) {
                setSelectedIndex( 0 );
            }
        }
    }

    @Override
    public void itemStateChanged( ItemEvent itemEvent ) {
        if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
            EVENT_BUS.post( new MasterdataScopeChangedEvent( ( String ) itemEvent.getItem( ) ) );
        }
    }
}

