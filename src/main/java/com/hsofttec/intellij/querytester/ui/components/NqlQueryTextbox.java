package com.hsofttec.intellij.querytester.ui.components;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.FontSettingsChangedEvent;
import com.hsofttec.intellij.querytester.events.PrepareQueryExecutionEvent;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.openapi.project.Project;
import com.intellij.util.textCompletion.TextCompletionProvider;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class NqlQueryTextbox extends TextFieldWithCompletion {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );

    private static final SettingsState SETTINGS_STATE = SettingsService.getSettings( );

    public NqlQueryTextbox( @Nullable Project project, @NotNull TextCompletionProvider provider, @NotNull String value ) {
        super( project, provider, value, false, true, true );
        EVENT_BUS.register( this );
//        setupTextFieldEditor(  );
        setFont( new Font( SETTINGS_STATE.getFontFace( ), Font.PLAIN, SETTINGS_STATE.getFontSize( ) ) );
        getInputMap( ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK ), "CTRL_ENTER" );
        getActionMap( ).put( "CTRL_ENTER", new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent actionEvent ) {
                EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
            }
        } );
        Dimension preferredSize = getPreferredSize( );
        preferredSize.height = 200;
        setMaximumSize( preferredSize );
        setPlaceholder( "type your query, dude!" );
    }

    @Subscribe
    public void fontSettingsChanged( FontSettingsChangedEvent event ) {
        setFont( new Font( SETTINGS_STATE.getFontFace( ), Font.PLAIN, SETTINGS_STATE.getFontSize( ) ) );
    }

//    public NqlQueryTextbox( ) {
//        getInputMap( ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK ), "CTRL_ENTER" );
//        getActionMap( ).put( "CTRL_ENTER", new AbstractAction( ) {
//            @Override
//            public void actionPerformed( ActionEvent actionEvent ) {
//                eventBus.post( new PrepareQueryExecutionEvent( ) );
//            }
//        } );
//    }
}
