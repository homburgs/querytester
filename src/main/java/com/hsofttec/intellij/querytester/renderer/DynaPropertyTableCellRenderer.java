package com.hsofttec.intellij.querytester.renderer;

import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class DynaPropertyTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent( JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row,
                                                    int column ) {

        if ( value instanceof DynaBean ) {
            DynaBean dynaBean = ( DynaBean ) value;
            DynaProperty dynaProperty = dynaBean.getDynaClass( ).getDynaProperties( )[ column ];
            Object dynaBeanValue = dynaBean.get( dynaProperty.getName( ) );
            Class<?> type = dynaProperty.getType( );
            if ( dynaBeanValue == null ) {
                if ( ArrayList.class.equals( type ) ) {
                    value = "<Empty>";
                } else {
                    value = "<null>";
                }
            } else if ( String.class.equals( type ) ) {
                value = dynaBeanValue;
            } else if ( ObjectclassName.class.equals( type ) ) {
                value = ( ( ObjectclassName ) dynaBeanValue ).getName( );
            } else if ( Integer.class.equals( type ) ) {
                value = dynaBeanValue;
            } else if ( Long.class.equals( type ) ) {
                value = dynaBeanValue;
            } else if ( DateTime.class.equals( type ) ) {
                value = ( ( DateTime ) dynaBeanValue ).toString( "yyyy-MM-dd hh:mm:ss" );
            } else if ( Boolean.class.equals( type ) ) {
                value = ( ( Boolean ) dynaBeanValue ).toString( );
            } else if ( ArrayList.class.equals( type ) ) {
                ArrayList arrayList = ( ( ArrayList ) dynaBeanValue );
                if ( arrayList.isEmpty( ) ) {
                    value = "<Empty>";
                } else {
                    StringBuilder valueBuilder = new StringBuilder( );
                    for ( Object element : arrayList ) {
                        if ( element == null ) {
                            element = "<null>";
                        }
                        valueBuilder.append( element ).append( ";" );
                    }
                    value = valueBuilder.toString( );
                    if ( ( ( String ) value ).length( ) > 0 ) {
                        value = StringUtils.stripEnd( ( String ) value, ";" );
                    }
                }
            } else {
                value = dynaBeanValue.toString( );
            }
        }

        return super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
    }
}
