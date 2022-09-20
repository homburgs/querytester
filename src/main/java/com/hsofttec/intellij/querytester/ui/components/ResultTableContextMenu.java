package com.hsofttec.intellij.querytester.ui.components;

import com.google.common.eventbus.EventBus;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.events.OptimizeTableHeaderWidthEvent;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import org.apache.commons.beanutils.BasicDynaBean;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResultTableContextMenu extends JBPopupMenu implements PopupMenuListener {
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final SettingsState SETTINGS = SettingsService.getSettings( );
    private JBMenuItem selectParentFolderId;
    private JBMenuItem searchParentMenuItem;
    private JBMenuItem showPathMenuItem;
    private JBMenuItem lockMenuItem;
    private JBMenuItem unlockMenuItem;
    private JBMenuItem showContentMenuItem;
    private JBMenuItem addDocumentMenuItem;
    private JBMenuItem addFolderMenuItem;
    private JBMenuItem addLinkMenuItem;
    private JBMenuItem deleteResourceMenuItem;
    private JBMenuItem optimizeColumnWithMenuItem;

    private ActionListener actionListener = new AbstractAction( ) {
        @Override
        public void actionPerformed( ActionEvent e ) {

        }
    };

    public ResultTableContextMenu( ) {
        super( );
        selectParentFolderId = new JBMenuItem( "Search From Here" );
        searchParentMenuItem = new JBMenuItem( "Search Parent" );
        searchParentMenuItem.setEnabled( false );
        showPathMenuItem = new JBMenuItem( "Show Path" );
        showPathMenuItem.setEnabled( false );
        showContentMenuItem = new JBMenuItem( "Show Content" );
        showContentMenuItem.setEnabled( false );
        lockMenuItem = new JBMenuItem( "Lock" );
        lockMenuItem.setEnabled( false );
        unlockMenuItem = new JBMenuItem( "Unlock" );
        unlockMenuItem.setEnabled( false );
        if ( SETTINGS.isShowDelete( ) ) {
            deleteResourceMenuItem = new JBMenuItem( "Delete Document/Folder" );
            deleteResourceMenuItem.setEnabled( false );
            add( deleteResourceMenuItem );
            addSeparator( );
        }
        addDocumentMenuItem = new JBMenuItem( "Add Document" );
        addDocumentMenuItem.setEnabled( false );
        addFolderMenuItem = new JBMenuItem( "Add Folder" );
        addFolderMenuItem.setEnabled( false );
        addLinkMenuItem = new JBMenuItem( "Add Link" );
        addLinkMenuItem.setEnabled( false );
        optimizeColumnWithMenuItem = new JBMenuItem( "Optimize Column Width" );

        add( selectParentFolderId );
        add( searchParentMenuItem );
        add( showPathMenuItem );
        add( showContentMenuItem );
        addSeparator( );
        add( lockMenuItem );
        add( unlockMenuItem );
        addSeparator( );
        add( optimizeColumnWithMenuItem );
        addSeparator( );
        add( addDocumentMenuItem );
        add( addFolderMenuItem );
        add( addLinkMenuItem );

        addPopupMenuListener( this );

        optimizeColumnWithMenuItem.addActionListener( new AbstractAction( ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                EVENT_BUS.post( new OptimizeTableHeaderWidthEvent( ) );
            }
        } );
    }

    public void setSelectParentFolderListener( Action action ) {
        selectParentFolderId.addActionListener( action );
    }

    @Override
    public void popupMenuWillBecomeVisible( PopupMenuEvent popupMenuEvent ) {
        ResultTableContextMenu menu = ( ResultTableContextMenu ) popupMenuEvent.getSource( );
        JTable invoker = ( JTable ) menu.getInvoker( );
        BasicDynaBean basicDynaBean = ( BasicDynaBean ) invoker.getValueAt( invoker.getSelectedRow( ), invoker.getSelectedColumn( ) );
        String selectedResourceId = ( String ) basicDynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY );

    }

    @Override
    public void popupMenuWillBecomeInvisible( PopupMenuEvent popupMenuEvent ) {

    }

    @Override
    public void popupMenuCanceled( PopupMenuEvent popupMenuEvent ) {

    }
}
