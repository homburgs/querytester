package com.hsofttec.intellij.querytester.utils;

import java.util.ArrayList;
import java.util.List;

public class NqlLiterals {
    public static final List<String> LITERALS = new ArrayList<>();

    static {
        LITERALS.add( "select" );
        LITERALS.add( "where" );
        LITERALS.add( "=" );
        LITERALS.add( "!=" );
        LITERALS.add( ">" );
        LITERALS.add( ">=" );
        LITERALS.add( "<" );
        LITERALS.add( "<=" );
        LITERALS.add( "in" );
        LITERALS.add( "between" );
        LITERALS.add( "like" );
        LITERALS.add( "is null" );
        LITERALS.add( "is not null" );
        LITERALS.add( "@=" );
        LITERALS.add( "~=" );
        LITERALS.add( "%=" );
        LITERALS.add( "%~" );
        LITERALS.add( "*=" );
        LITERALS.add( "order by" );
        LITERALS.add( "orderby" );
        LITERALS.add( "desc" );
        LITERALS.add( "asc" );
        LITERALS.add( "scope onelevel" );
        LITERALS.add( "scope subtree" );
        LITERALS.add( "hidden" );
        LITERALS.add( "%currentUserPrincipalId" );
        LITERALS.add( "%currentDefaultPositionPrincipalId" );
        LITERALS.add( "%currentPositionPrincipalIds" );
        LITERALS.add( "%currentGroupPrincipalIds" );
        LITERALS.add( "%currentProxiedOrgEntityPrincipalIds" );
        LITERALS.add( "%currentHeadedOrgEntityPrincipalIds" );
        LITERALS.add( "%currentManagedOrgEntityPrincipalIds" );
        LITERALS.add( "%currentOwnedOrgEntityPrincipal" );
        LITERALS.add( "%actAsPositionIds" );
        LITERALS.add( "%agentPrincipalId" );
        LITERALS.add( "%currentWorkPositionPrincipalId" );
        LITERALS.add( "%today" );
        LITERALS.add( "%now" );
        LITERALS.add( "%todayAtMidnight" );
        LITERALS.add( "%tomorrowAtMidnight" );
        LITERALS.add( "%clientHost" );
        LITERALS.add( "%clientIPAddress" );
        LITERALS.add( "%clientApplication" );
        LITERALS.add( "%clientLocale" );
        LITERALS.add( "%clientToken" );
        LITERALS.add( "%similarityThreshold" );
        LITERALS.add( "%systemId" );
//        LITERALS.add("update");
//        LITERALS.add("delete");
    }
}
