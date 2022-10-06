/*
 * The MIT License (MIT)
 *
 * Copyright © 2022 Sven Homburg, <homburgs@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hsofttec.intellij.querytester.renderer;

import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.lang.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            value = convertValueToString( dynaBean.get( dynaProperty.getName( ) ) );
        }

        return super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
    }

    private String convertValueToString( Object rawValue ) {
        String stringValue = "";

        if ( rawValue == null ) {
            stringValue = "null";
        } else {
            if ( rawValue instanceof ArrayList ) {
                List arrayList = ( ( ArrayList ) rawValue );
                if ( arrayList.isEmpty( ) ) {
                    stringValue = "<Empty>";
                } else {
                    StringBuilder valueBuilder = new StringBuilder( "[" );
                    for ( Object element : arrayList ) {
                        if ( element == null ) {
                            valueBuilder.append( "<null>" );
                        } else {
                            valueBuilder.append( convertValueToString( element ) ).append( ";" );
                        }
                    }

                    if ( !arrayList.isEmpty( ) ) {
                        valueBuilder.deleteCharAt( valueBuilder.length( ) - 1 );
                    }

                    valueBuilder.append( "]" );
                    stringValue = valueBuilder.toString( );
                }
            } else if ( rawValue instanceof ObjectclassName ) {
                stringValue = ( ( ObjectclassName ) rawValue ).getName( );
            } else if ( rawValue instanceof DateTime ) {
                stringValue = ( ( DateTime ) rawValue ).toString( "yyyy-MM-dd hh:mm:ss" );
            } else if ( rawValue instanceof Date ) {
                stringValue = DateFormatUtils.format( ( Date ) rawValue, "yyyy-MM-dd" );
            } else if ( rawValue instanceof YearMonthDay ) {
                stringValue = ( ( YearMonthDay ) rawValue ).toString( "yyyy-MM-dd" );
            } else if ( rawValue instanceof Boolean ) {
                stringValue = ( ( Boolean ) rawValue ).toString( );
            } else if ( rawValue instanceof Integer ) {
                stringValue = ( ( Integer ) rawValue ).toString( );
            } else if ( rawValue instanceof Double ) {
                stringValue = ( ( Double ) rawValue ).toString( );
            } else if ( rawValue instanceof Long ) {
                stringValue = ( ( Long ) rawValue ).toString( );
            } else {
                stringValue = String.valueOf( rawValue );
            }
        }

        return stringValue;
    }

}
