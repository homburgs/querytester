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

package com.hsofttec.intellij.querytester.ui.components;

import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.renderer.DynaPropertyTableCellRenderer;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.intellij.ui.table.JBTable;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NscaleTable extends JBTable {
    private static final Logger logger = LoggerFactory.getLogger( NscaleTable.class );
    private static final SettingsState SETTINGS = SettingsService.getSettings( );
    private final ConnectionService connectionService;
    private final QueryTester queryTester;
    private final ResultTableContextMenu contextMenu;

    public NscaleTable( QueryTester queryTester ) {
        this.queryTester = queryTester;
        connectionService = ConnectionService.getInstance( );
        setFont( new Font( SETTINGS.getFontFace( ), Font.PLAIN, SETTINGS.getFontSize( ) ) );
        setAutoResizeMode( JBTable.AUTO_RESIZE_OFF );
        setDefaultRenderer( Object.class, new DynaPropertyTableCellRenderer( ) );

        contextMenu = new ResultTableContextMenu( queryTester );
        setComponentPopupMenu( contextMenu );

        addMouseListener( new MouseAdapter( ) {
            @Override
            public void mousePressed( MouseEvent mouseEvent ) {
                // selects the row at which point the mouse is clicked
                Point point = mouseEvent.getPoint( );
                int currentCol = columnAtPoint( point );
                int currentRow = rowAtPoint( point );
                if ( currentRow != -1 ) {
                    if ( mouseEvent.getClickCount( ) == 1 ) {
                        try {
                            setRowSelectionInterval( currentRow, currentRow );
                        } catch ( Exception e ) {
                            logger.warn( ExceptionUtils.getRootCause( e ).getLocalizedMessage( ) );
                        }
                    }
                }
                if ( currentCol != -1 ) {
                    if ( mouseEvent.getClickCount( ) == 2 ) {
                        DynaBean selectedRowValue = ( DynaBean ) getValueAt( currentRow, currentCol );
                        Object headerValue = getColumnModel( ).getColumn( currentCol ).getHeaderValue( );
//                        ModifyResourceDialog modifyResourceDialog = new ModifyResourceDialog( queryTester );
//                        modifyResourceDialog.setData( selectedRowValue, ( String ) headerValue );
//                        if ( modifyResourceDialog.showAndGet( ) ) {
//
//                        }
                    }
                }
            }
        } );
    }

    public void fontSettingsChanged( ) {
        setFont( new Font( SETTINGS.getFontFace( ), Font.PLAIN, SETTINGS.getFontSize( ) ) );
    }

    /**
     * Calculates the optimal width for the header of the given table. The
     * calculation is based on the preferred width of the header renderer.
     */
    public void calcHeaderWidth( ) {
        JTableHeader header = this.getTableHeader( );
        TableCellRenderer defaultHeaderRenderer = null;
        if ( header != null ) {
            defaultHeaderRenderer = header.getDefaultRenderer( );
        }
        TableColumnModel columns = this.getColumnModel( );
        for ( int col = 0; col < columns.getColumnCount( ); col++ ) {
            TableColumn column = columns.getColumn( col );
            int width = -1;
            TableCellRenderer h = column.getHeaderRenderer( );
            if ( h == null ) {
                h = defaultHeaderRenderer;
            }
            if ( h != null ) {
                // Not explicitly impossible
                Component c = h.getTableCellRendererComponent( this, column.getHeaderValue( ), false, false, -1, col );
                width = c.getPreferredSize( ).width + 10;
                column.setPreferredWidth( width );
            }
        }
    }

    /**
     * increments every table column width except line no column.
     */
    public void incrementHeaderWidth( ) {
        TableColumnModel columns = this.getColumnModel( );
        for ( int col = 0; col < columns.getColumnCount( ); col++ ) {
            if ( col == 0 ) {
                if ( SETTINGS.isShowIdColumn( ) ) {
                    continue;
                }
            }
            TableColumn column = columns.getColumn( col );
            int width = column.getPreferredWidth( );
            column.setPreferredWidth( width + 100 );
        }
    }
}
