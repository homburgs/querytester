package com.hsofttec.intellij.querytester.events;

public class DocumentAreaChangedEvent {
    private final String documentAreaName;

    public DocumentAreaChangedEvent( String documentAreaName ) {
        this.documentAreaName = documentAreaName;
    }

    public String getDocumentAreaName( ) {
        return documentAreaName;
    }
}
