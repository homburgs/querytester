package com.hsofttec.intellij.querytester.services;

import com.hsofttec.intellij.querytester.models.SettingsState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
        name = "querytester-settings",
        storages = @Storage( "querytester-settings.xml" )
)
public class SettingsService implements PersistentStateComponent<SettingsState> {

    public SettingsState settingsState = new SettingsState( );

    public SettingsService( ) {
    }

    public static SettingsState getSettings( ) {
        return ApplicationManager.getApplication( ).getService( SettingsService.class ).settingsState;
    }


    @Override
    public SettingsState getState( ) {
        return settingsState;
    }

    @Override
    public void loadState( @NotNull SettingsState state ) {
        XmlSerializerUtil.copyBean( state, this.settingsState );
    }
}
