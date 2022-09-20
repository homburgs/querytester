package com.hsofttec.intellij.querytester.events;

public class MasterdataScopeChangedEvent {
    private final String masterdataScopeName;

    public MasterdataScopeChangedEvent( String masterdataScopeName ) {
        this.masterdataScopeName = masterdataScopeName;
    }

    public String getMasterdataScopeName( ) {
        return masterdataScopeName;
    }
}
