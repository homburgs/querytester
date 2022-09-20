package com.hsofttec.intellij.querytester.ui.components;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hsofttec.intellij.querytester.events.FontSettingsChangedEvent;
import com.hsofttec.intellij.querytester.events.OptimizeTableHeaderWidthEvent;
import com.hsofttec.intellij.querytester.events.StartQueryExecutionEvent;
import com.hsofttec.intellij.querytester.models.DynaClassTableModel;
import com.hsofttec.intellij.querytester.models.NscaleResult;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.renderer.DynaPropertyTableCellRenderer;
import com.hsofttec.intellij.querytester.services.HistorySettingsService;
import com.hsofttec.intellij.querytester.services.QueryService;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.table.JBTable;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NscaleTable extends JBTable {

    private static final Project project = ProjectManager.getInstance( ).getOpenProjects( )[ 0 ];

    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );

    private static final QueryService QUERY_SERVICE = project.getService( QueryService.class );

    private static final HistorySettingsService HISTORY_SETTINGS_SERVICE = HistorySettingsService.getSettings( project );

    private static final SettingsState SETTINGS = SettingsService.getSettings( );

    public NscaleTable( ) {
        EVENT_BUS.register( this );
        setFont( new Font( SETTINGS.getFontFace( ), Font.PLAIN, SETTINGS.getFontSize( ) ) );
        setAutoResizeMode( JBTable.AUTO_RESIZE_OFF );
        setDefaultRenderer( Object.class, new DynaPropertyTableCellRenderer( ) );
        addMouseListener( new MouseAdapter( ) {
            @Override
            public void mousePressed( MouseEvent mouseEvent ) {
                // selects the row at which point the mouse is clicked
                Point point = mouseEvent.getPoint( );
                int currentRow = rowAtPoint( point );
                setRowSelectionInterval( currentRow, currentRow );
            }
        } );
    }

    @Subscribe
    public void fontSettingsChanged( FontSettingsChangedEvent event ) {
        setFont( new Font( SETTINGS.getFontFace( ), Font.PLAIN, SETTINGS.getFontSize( ) ) );
    }

    @Subscribe
    public void startQueryExecution( StartQueryExecutionEvent event ) {
        StartQueryExecutionEvent.QueryExecutionParameters queryExecutionParameters = event.getData( );
        NscaleResult nscaleResult = QUERY_SERVICE.proccessQuery( queryExecutionParameters.getConnectionSettings( ),
                queryExecutionParameters.getQueryMode( ),
                queryExecutionParameters.getDocumentAreaName( ),
                queryExecutionParameters.getMasterdataScope( ),
                queryExecutionParameters.getRootResourceId( ),
                queryExecutionParameters.getNqlQuery( ) );

        if ( nscaleResult != null ) {
            setModel( new DynaClassTableModel( nscaleResult ) );
            if ( !SETTINGS.isShowIdColumn( ) ) {
                getColumnModel( ).getColumn( 0 ).setMinWidth( 0 );
                getColumnModel( ).getColumn( 0 ).setMaxWidth( 0 );
            }
            if ( !SETTINGS.isShowKeyColumn( ) ) {
                getColumnModel( ).getColumn( 1 ).setMinWidth( 0 );
                getColumnModel( ).getColumn( 1 ).setMaxWidth( 0 );
            }
            HISTORY_SETTINGS_SERVICE.addQuery( event.getData( ).getNqlQuery( ) );
        }
    }

    /**
     * Calculates the optimal width for the header of the given table. The
     * calculation is based on the preferred width of the header renderer.
     */
    @Subscribe
    public void calcHeaderWidth( OptimizeTableHeaderWidthEvent event ) {
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
}
