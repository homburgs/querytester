package com.hsofttec.intellij.querytester.ui;

import com.google.common.eventbus.EventBus;
import com.hsofttec.intellij.querytester.events.FontSettingsChangedEvent;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.components.AppSettingsComponent;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent appSettingsComponent;

    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    ;

    @Nls( capitalization = Nls.Capitalization.Title )
    @Override
    public String getDisplayName( ) {
        return "nscale QueryTester Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent( ) {
        return appSettingsComponent.getPreferredFocusedComponent( );
    }

    @Nullable
    @Override
    public JComponent createComponent( ) {
        appSettingsComponent = new AppSettingsComponent( );
        return appSettingsComponent.getPanel( );
    }

    @Override
    public boolean isModified( ) {
        SettingsState settings = SettingsService.getSettings( );
        boolean modified = appSettingsComponent.getShowIdColumnValue( ) != settings.isShowIdColumn( );
        modified |= appSettingsComponent.getShowKeyColumnValue( ) != settings.isShowKeyColumn( );
        modified |= appSettingsComponent.getMaxHistorySizeValue( ) != settings.getMaxHistorySize( );
        modified |= appSettingsComponent.getMaxResultSizeValue( ) != settings.getMaxResultSize( );
        modified |= appSettingsComponent.getFontSizeValue( ) != settings.getFontSize( );
        modified |= !appSettingsComponent.getFontFaceValue( ).equals( settings.getFontFace( ) );
        return modified;
    }

    @Override
    public void apply( ) {
        SettingsState settings = SettingsService.getSettings( );
        settings.setShowIdColumn( appSettingsComponent.getShowIdColumnValue( ) );
        settings.setShowKeyColumn( appSettingsComponent.getShowKeyColumnValue( ) );
        settings.setMaxHistorySize( appSettingsComponent.getMaxHistorySizeValue( ) );
        settings.setMaxResultSize( appSettingsComponent.getMaxResultSizeValue( ) );
        settings.setFontFace( appSettingsComponent.getFontFaceValue( ) );
        settings.setFontSize( appSettingsComponent.getFontSizeValue( ) );
        EVENT_BUS.post( new FontSettingsChangedEvent( ) );
    }

    @Override
    public void reset( ) {
        SettingsState settings = SettingsService.getSettings( );
        appSettingsComponent.setShowIdColumnValue( settings.isShowIdColumn( ) );
        appSettingsComponent.setShowKeyColumnValue( settings.isShowKeyColumn( ) );
        appSettingsComponent.setMaxHistorySizeValue( settings.getMaxHistorySize( ) );
        appSettingsComponent.setMaxResultSizeValue( settings.getMaxResultSize( ) );
        appSettingsComponent.setFontSizeValue( settings.getFontSize( ) );
        appSettingsComponent.setFontFaceValue( settings.getFontFace( ) );
    }

    @Override
    public void disposeUIResources( ) {
        appSettingsComponent = null;
    }
}
