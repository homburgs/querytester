package com.hsofttec.intellij.querytester.ui.components;

import com.google.common.eventbus.EventBus;
import com.hsofttec.intellij.querytester.events.PrepareQueryExecutionEvent;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.ui.components.JBTextField;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RepositoryRootTextField extends JBTextField implements KeyListener {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );

    public RepositoryRootTextField( ) {
        EVENT_BUS.register( this );
        addKeyListener( this );
    }

    @Override
    public void keyTyped( KeyEvent e ) {

    }

    @Override
    public void keyPressed( KeyEvent keyEvent ) {
        if ( keyEvent.getKeyCode( ) == KeyEvent.VK_ENTER ) {
            EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
        }
    }

    @Override
    public void keyReleased( KeyEvent e ) {

    }
}
