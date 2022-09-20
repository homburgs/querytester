package com.hsofttec.intellij.querytester.utils;

import java.util.ArrayList;
import java.util.List;

public class NqlLiterals {
    public static final List<String> LITERALS = new ArrayList<>( );

    static {
        LITERALS.add( "SELECT" );
        LITERALS.add( "WHERE" );
        LITERALS.add( "=" );
        LITERALS.add( "!=" );
        LITERALS.add( ">" );
        LITERALS.add( ">=" );
        LITERALS.add( "<" );
        LITERALS.add( "<=" );
        LITERALS.add( "IN" );
        LITERALS.add( "BETWEEN" );
        LITERALS.add( "LIKE" );
        LITERALS.add( "IS NULL" );
        LITERALS.add( "IS NOT NULL" );
        LITERALS.add( "@=" );
        LITERALS.add( "~=" );
        LITERALS.add( "%=" );
        LITERALS.add( "%~" );
        LITERALS.add( "*=" );
        LITERALS.add( "ORDER BY" );
        LITERALS.add( "ORDERBY" );
        LITERALS.add( "DESC" );
        LITERALS.add( "ASC" );
        LITERALS.add( "ASC" );
        LITERALS.add( "SCOPE" );
        LITERALS.add( "SCOPE ONELEVEL" );
        LITERALS.add( "SCOPE SUBTREE" );
        LITERALS.add( "HIDDEN" );
    }
}
