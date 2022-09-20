package com.hsofttec.intellij.querytester.completion;

import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyDefinition;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.DocumentAreaChangedEvent;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.hsofttec.intellij.querytester.utils.NqlLiterals;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.TextFieldCompletionProviderDumbAware;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NqlCompletionProvider extends TextFieldCompletionProviderDumbAware {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );

    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    ;

    private Pattern pattern = Pattern.compile( "([.!? ,])" );

    private final List<IndexingPropertyDefinition> indexingPropertyDefinitions = new ArrayList<>( );

    public NqlCompletionProvider( ) {
        super( true );
        EVENT_BUS.register( this );
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

    @Subscribe
    public void dcumentAreaChanged( DocumentAreaChangedEvent event ) {
        indexingPropertyDefinitions.clear( );
        indexingPropertyDefinitions.addAll( CONNECTION_SERVICE.getSession( ).getConfigurationService( ).getIndexingPropertyDefinitions( event.getDocumentAreaName( ) ) );
    }
}
