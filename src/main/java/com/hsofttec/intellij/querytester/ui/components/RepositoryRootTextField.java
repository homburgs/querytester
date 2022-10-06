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

import com.hsofttec.intellij.querytester.ui.notifiers.PrepareQueryExecutionNotifier;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RepositoryRootTextField extends ExtendableTextField implements KeyListener {
    private final Project project;

    public RepositoryRootTextField( Project project ) {
        this.project = project;
        addKeyListener( this );
        ExtendableTextComponent.Extension browseExtension =
                ExtendableTextComponent.Extension.create( AllIcons.Actions.Close, "Clear input", ( ) -> {
                    setText( "" );
                    PrepareQueryExecutionNotifier notifier = project.getMessageBus( ).syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
                    notifier.doAction( );
                } );
        addExtension( browseExtension );
    }

    @Override
    public void keyTyped( KeyEvent e ) {

    }

    @Override
    public void keyPressed( KeyEvent keyEvent ) {
        if ( keyEvent.getKeyCode( ) == KeyEvent.VK_ENTER ) {
            PrepareQueryExecutionNotifier notifier = project.getMessageBus( ).syncPublisher( PrepareQueryExecutionNotifier.PREPARE_QUERY_EXECUTION_TOPIC );
            notifier.doAction( );
        }
    }

    @Override
    public void keyReleased( KeyEvent e ) {

    }
}
