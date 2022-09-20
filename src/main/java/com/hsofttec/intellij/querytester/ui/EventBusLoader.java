package com.hsofttec.intellij.querytester.ui;

import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

public class EventBusLoader extends PreloadingActivity {
    @Override
    public void preload( @NotNull ProgressIndicator indicator ) {
        indicator.setText( "Loading eventBus for querytester ..." );
//        EventBusFactory.load( );
    }
}
