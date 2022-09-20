package com.hsofttec.intellij.querytester.models;

import com.hsofttec.intellij.querytester.QueryTesterConstants;

import javax.swing.table.AbstractTableModel;

public class DynaClassTableModel extends AbstractTableModel {
    private final NscaleResult result;

    public DynaClassTableModel( NscaleResult result ) {
        this.result = result;
    }

    @Override
    public String getColumnName( int column ) {
        String colName = result.getPropertyNames( ).get( column );
        if ( colName.equals( QueryTesterConstants.DBEAN_PROPERTY_NAME_LINENO ) ) {
            colName = "";
        }
        return colName;
    }

    @Override
    public int getRowCount( ) {
        return result.getDynaBeans( ).size( );
    }

    @Override
    public int getColumnCount( ) {
        return result.getPropertyNames( ).size( );
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex ) {
        return result.getDynaBeans( ).get( rowIndex );
    }
}
