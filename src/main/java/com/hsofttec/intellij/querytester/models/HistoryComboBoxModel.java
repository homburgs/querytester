package com.hsofttec.intellij.querytester.models;

import javax.swing.*;
import java.util.List;

public class HistoryComboBoxModel extends DefaultComboBoxModel<String> {
    public HistoryComboBoxModel( List<String> items ) {
        super( );
        addElement( "" );
        addAll( items );
    }

    @Override
    public String getSelectedItem( ) {
        return ( String ) super.getSelectedItem( );
    }
}
