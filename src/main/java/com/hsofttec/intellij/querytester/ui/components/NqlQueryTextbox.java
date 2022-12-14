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

import com.hsofttec.intellij.querytester.completion.NqlCompletionProvider;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.hsofttec.intellij.querytester.ui.notifiers.PrepareQueryExecutionNotifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.textCompletion.TextFieldWithCompletion;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class NqlQueryTextbox extends TextFieldWithCompletion {
    private static final SettingsState SETTINGS_STATE = SettingsService.getSettings( );

    private final QueryTester queryTester;

    public NqlQueryTextbox( QueryTab owner ) {
        super( owner.getQueryTester( ).getProject( ), new NqlCompletionProvider( owner.getQueryTester( ).getProject( ) ), "", false, true, true );
        this.queryTester = owner.getQueryTester( );
        setBorder( BorderFactory.createEmptyBorder( ) );
        setFont( new Font( SETTINGS_STATE.getFontFace( ), Font.PLAIN, SETTINGS_STATE.getFontSize( ) ) );
        Dimension preferredSize = getPreferredSize( );
        preferredSize.height = 200;
        setMaximumSize( preferredSize );
        setPlaceholder( "... type your query and press Ctrl-Enter to execute, dude!" );
        setShowPlaceholderWhenFocused( true );

        AnAction action = new AnAction( ) {
            @Override
            public void actionPerformed( @NotNull AnActionEvent anActionEvent ) {
                MessageBus messageBus = queryTester.getProject( ).getMessageBus( );
                PrepareQueryExecutionNotifier notifier = messageBus.syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                notifier.doAction( );
            }
        };

        action.registerCustomShortcutSet( new CustomShortcutSet( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK ) ), this );
    }

    public void fontSettingsChanged( ) {
        setFont( new Font( SETTINGS_STATE.getFontFace( ), Font.PLAIN, SETTINGS_STATE.getFontSize( ) ) );
    }
}
