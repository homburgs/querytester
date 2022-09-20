package com.hsofttec.intellij.querytester.services;

import com.hsofttec.intellij.querytester.listeners.HistoryModifiedEventListener;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.utils.DimensionConverter;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@State(
        name = "QueryHistory",
        storages = { @Storage( "querytester.xml" ) }
)
public class HistorySettingsService implements PersistentStateComponent<HistorySettingsService.HistorySettingsState> {

    private static final SettingsState SETTINGS_STATE = SettingsService.getSettings( );

    private final Project project;
    private HistorySettingsState historySettingsState = new HistorySettingsState( );

    public HistorySettingsService( Project project ) {
        this.project = project;
    }

    private static final List<HistoryModifiedEventListener> listeners = new ArrayList<>( );

    public static HistorySettingsService getSettings( Project project ) {
        return ServiceManager.getService( project, HistorySettingsService.class );
    }

    @Override
    public HistorySettingsService.HistorySettingsState getState( ) {
        return historySettingsState;
    }

    @Override
    public void loadState( @NotNull HistorySettingsService.HistorySettingsState state ) {
        XmlSerializerUtil.copyBean( state, this.historySettingsState );
    }

    public Dimension getDialogDimension( ) {
        return historySettingsState.dialogDimension;
    }

    public void setDialogDimension( Dimension dialogDimension ) {
        historySettingsState.dialogDimension = dialogDimension;
    }

    public List<String> getQueryList( ) {
        return historySettingsState.historyList;
    }

    public void addListener( HistoryModifiedEventListener listener ) {
        listeners.add( listener );
    }

    public void addQuery( @NotNull String nqlQuery ) {
        if ( nqlQuery.trim( ).isEmpty( ) ) {
            return;
        }

        nqlQuery = nqlQuery.replaceAll( "\\t\\n\\r", " " );

        for ( String query : historySettingsState.historyList ) {
            if ( query.equals( nqlQuery ) ) {
                return;
            }
        }

        if ( historySettingsState.historyList.size( ) >= SETTINGS_STATE.getMaxHistorySize( ) ) {
            historySettingsState.historyList.remove( 0 );
        }
        historySettingsState.historyList.add( nqlQuery );

        for ( HistoryModifiedEventListener listener : listeners ) {
            listener.notifyAdd( nqlQuery );
        }
    }

    private long getCRC32Checksum( byte[] bytes ) {
        Checksum crc32 = new CRC32( );
        crc32.update( bytes, 0, bytes.length );
        return crc32.getValue( );
    }

    public static class HistorySettingsState {
        public List<String> historyList = new ArrayList<>( );
        @OptionTag( converter = DimensionConverter.class )
        public Dimension dialogDimension = new Dimension( 200, 200 );
    }
}
