package com.hsofttec.intellij.querytester.listeners;

public interface HistoryModifiedEventListener {
    void notifyAdd( String query );

    void notifyRemove( String query );
}
