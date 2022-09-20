package com.hsofttec.intellij.querytester.events;

import com.hsofttec.intellij.querytester.models.NscaleResult;

public class QueryExecutionSuccededEvent {
    private final NscaleResult result;

    public QueryExecutionSuccededEvent( NscaleResult result ) {
        this.result = result;
    }

    public NscaleResult getData( ) {
        return result;
    }
}
