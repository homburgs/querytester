package com.hsofttec.intellij.querytester.renderer;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;

import javax.swing.*;
import java.awt.*;

public class ConnectionListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent( JList<?> list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus ) {

        if ( value instanceof ConnectionSettingsService.ConnectionSettings ) {
            value = ( ( ConnectionSettingsService.ConnectionSettings ) value ).getConnectionName( );
        }

        return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
    }
}
