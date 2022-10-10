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

package com.hsofttec.intellij.querytester.completion;

import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyDefinition;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.notifiers.DocumentAreaChangedNotifier;
import com.hsofttec.intellij.querytester.utils.NqlLiterals;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.util.TextFieldCompletionProviderDumbAware;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NqlCompletionProvider extends TextFieldCompletionProviderDumbAware {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );
    private final Project project;

    private Pattern pattern = Pattern.compile( "([.!? ,])" );

    private final List<IndexingPropertyDefinition> indexingPropertyDefinitions = new ArrayList<>( );

    public NqlCompletionProvider( Project project ) {
        super( true );
        this.project = project;
        MessageBusConnection connect = project.getMessageBus( ).connect( );
        connect.subscribe( DocumentAreaChangedNotifier.DOCUMENT_AREA_CHANGED_TOPIC, this::dcumentAreaChanged );
    }

    @Override
    protected void addCompletionVariants( @NotNull String text, int offset, @NotNull String prefix, @NotNull CompletionResultSet result ) {
        CompletionResultSet myResult;
        int lastSpace = 0;
//        PrefixMatcher prefixMatcher = result.getPrefixMatcher();

        Matcher matcher = pattern.matcher( prefix );
        while ( matcher.find( ) ) {
            lastSpace = matcher.end( 1 );
        }

        if ( lastSpace >= 0 && lastSpace <= prefix.length( ) - 1 ) {
            prefix = prefix.substring( lastSpace );
            myResult = result.withPrefixMatcher( prefix );
        } else {
            myResult = result;
        }

        for ( String literal : NqlLiterals.LITERALS ) {
            final LookupElementBuilder element = LookupElementBuilder.create( literal.toLowerCase( ) );
            myResult.addElement( element.withLookupString( element.getLookupString( ) ) );
        }

        for ( IndexingPropertyDefinition indexingPropertyDefinition : indexingPropertyDefinitions ) {
            final LookupElementBuilder element = LookupElementBuilder.create( indexingPropertyDefinition.getName( ).getName( ) );
            myResult.addElement( element.withLookupString( element.getLookupString( ) ) );
        }

        result.stopHere( );
    }

    public void dcumentAreaChanged( String selectedDocumentAreaName ) {
        Session session = CONNECTION_SERVICE.getSession( );
        indexingPropertyDefinitions.clear( );
        if ( session != null ) {
            List<IndexingPropertyDefinition> definitions = session.getConfigurationService( ).getIndexingPropertyDefinitions( selectedDocumentAreaName );
            indexingPropertyDefinitions.addAll( definitions );
        }
    }
}
