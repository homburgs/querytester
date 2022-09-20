package com.hsofttec.intellij.querytester.ui.components;

import com.ceyoniq.nscale.al.core.cfg.DocumentArea;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.ConnectionSelectionChangedEvent;
import com.hsofttec.intellij.querytester.events.DocumentAreaChangedEvent;
import com.hsofttec.intellij.querytester.renderer.ConnectionListCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.ui.CollectionComboBoxModel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class DocumentAreaSelect extends JComboBox<String> {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ConnectionService connectionService = ConnectionService.getInstance( );
    private final List<String> documentAreaNames = new ArrayList<>( );

    public DocumentAreaSelect( ) {
        EVENT_BUS.register( this );
        this.addItemListener( itemEvent -> {
            if ( itemEvent.getStateChange( ) == ItemEvent.SELECTED ) {
                EVENT_BUS.post( new DocumentAreaChangedEvent( ( String ) itemEvent.getItem( ) ) );
            }
        } );

        setRenderer( new ConnectionListCellRenderer( ) );
        setModel( new CollectionComboBoxModel<>( documentAreaNames ) );
        setSelectedIndex( 0 );
    }

    @Override
    public void setSelectedIndex( int index ) {
        if ( !documentAreaNames.isEmpty( ) && documentAreaNames.size( ) >= index ) {
            super.setSelectedIndex( index );
            EVENT_BUS.post( new DocumentAreaChangedEvent( ( String ) getSelectedItem( ) ) );
        }
    }

    @Subscribe
    public void connectionSelectionChanged( ConnectionSelectionChangedEvent event ) {
        List<DocumentArea> documentAreas = connectionService.getSession( ).getConfigurationService( ).getDocumentAreas( );
        documentAreaNames.clear( );
        for ( DocumentArea documentArea : documentAreas ) {
            documentAreaNames.add( documentArea.getAreaName( ) );
        }

        setSelectedIndex( 0 );
    }
}

