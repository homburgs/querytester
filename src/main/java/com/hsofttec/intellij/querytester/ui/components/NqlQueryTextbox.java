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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.completion.NqlCompletionProvider;
import com.hsofttec.intellij.querytester.events.FontSettingsChangedEvent;
import com.hsofttec.intellij.querytester.events.PrepareQueryExecutionEvent;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class NqlQueryTextbox extends TextFieldWithCompletion {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final ProjectManager PROJECT_MANAGER = ProjectManager.getInstance( );
    private static final Project PROJECT = PROJECT_MANAGER.getOpenProjects( )[ 0 ];
    private static final SettingsState SETTINGS_STATE = SettingsService.getSettings( );

    public NqlQueryTextbox( ) {
        super( PROJECT, new NqlCompletionProvider( ), "", false, true, true );
        EVENT_BUS.register( this );
        setFont( new Font( SETTINGS_STATE.getFontFace( ), Font.PLAIN, SETTINGS_STATE.getFontSize( ) ) );
        Dimension preferredSize = getPreferredSize( );
        preferredSize.height = 200;
        setMaximumSize( preferredSize );
        setPlaceholder( "type your query, dude!" );
        setShowPlaceholderWhenFocused( true );

        AnAction action = new AnAction( ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                EVENT_BUS.post( new PrepareQueryExecutionEvent( ) );
            }
        };

        action.registerCustomShortcutSet( new CustomShortcutSet( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK ) ), this );
    }

    @Subscribe
    public void fontSettingsChanged( FontSettingsChangedEvent event ) {
        setFont( new Font( SETTINGS_STATE.getFontFace( ), Font.PLAIN, SETTINGS_STATE.getFontSize( ) ) );
    }
}
