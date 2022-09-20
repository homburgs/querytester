package com.hsofttec.intellij.querytester.ui.components;

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
import java.util.ArrayList;
import java.util.List;

public class MasterdataScopeSelect extends JComboBox<String> {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ConnectionService connectionService = ConnectionService.getInstance( );
    private final List<String> scopes = new ArrayList<>( );

    public MasterdataScopeSelect( ) {
        EVENT_BUS.register( this );
        this.addItemListener( itemEvent -> {
            if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
                EVENT_BUS.post( new DocumentAreaChangedEvent( ( String ) itemEvent.getItem( ) ) );
            }
        } );

//        setRenderer( new ConnectionListCellRenderer( ) );
        setModel( new CollectionComboBoxModel<>( scopes ) );
        setSelectedIndex( 0 );
    }

    @Override
    public void setSelectedIndex( int index ) {
        if ( !scopes.isEmpty( ) && scopes.size( ) >= index ) {
            super.setSelectedIndex( index );
            EVENT_BUS.post( new MasterdataScopeChangedEvent( ( String ) getSelectedItem( ) ) );
        }
    }

    @Subscribe
    public void documentAreaChangedEvent( DocumentAreaChangedEvent event ) {
        List<MasterdataScope> masterdataScopes = connectionService.getSession( ).getConfigurationService( ).getMasterdataScopes( event.getDocumentAreaName( ) );
        scopes.clear( );
        for ( MasterdataScope masterdataScope : masterdataScopes ) {
            scopes.add( masterdataScope.getDisplayNameId( ) );
        }

        setSelectedIndex( 0 );
    }
}

