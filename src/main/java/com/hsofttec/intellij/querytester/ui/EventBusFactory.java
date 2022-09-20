package com.hsofttec.intellij.querytester.ui;

import com.google.common.eventbus.EventBus;

public class EventBusFactory {
    private static EventBusFactory instance = null;

    private final EventBus eventBus;

    private EventBusFactory( ) {
        eventBus = new EventBus( );
    }

    public static EventBusFactory getInstance( ) {
        if ( instance == null ) {
            synchronized ( EventBusFactory.class ) {
                instance = new EventBusFactory( );
            }
        }
        return instance;
    }

    public EventBus get( ) {
        return eventBus;
    }
}
