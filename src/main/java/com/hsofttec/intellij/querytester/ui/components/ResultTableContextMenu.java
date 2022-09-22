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

import com.ceyoniq.nscale.al.core.repository.ResourceType;
import com.google.common.eventbus.EventBus;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.events.OptimizeTableHeaderWidthEvent;
import com.hsofttec.intellij.querytester.events.RootResourceIdChangedEvent;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.SettingsState;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.hsofttec.intellij.querytester.ui.ResourcePathDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResultTableContextMenu extends JBPopupMenu implements PopupMenuListener {
    private static final ProjectManager projectManager = ProjectManager.getInstance( );
    private static final Project project = projectManager.getOpenProjects( )[ 0 ];
    private static final Logger logger = LoggerFactory.getLogger( ResultTableContextMenu.class );
    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );
    private static final SettingsState SETTINGS = SettingsService.getSettings( );
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );
    private String selectedResourceId;
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
        selectParentFolderId.setEnabled( false );

        searchParentMenuItem = new JBMenuItem( "Search Parent" );
        searchParentMenuItem.setEnabled( true );

        showPathMenuItem = new JBMenuItem( "Show Path" );
        showPathMenuItem.addActionListener( actionEvent -> {
            ResourcePathDialog dialog = new ResourcePathDialog( project );
            dialog.setData( selectedResourceId );
            if ( dialog.showAndGet( ) ) {
                BaseResource baseResource = dialog.getData( );
                EVENT_BUS.post( new RootResourceIdChangedEvent( baseResource.getResourceid( ) ) );
            }
        } );
        showPathMenuItem.setEnabled( false );

        showContentMenuItem = new JBMenuItem( "Show Content" );
        showContentMenuItem.setEnabled( false );

        // lock item
        lockMenuItem = new JBMenuItem( "Lock" );
        lockMenuItem.addActionListener( actionEvent -> {
            CONNECTION_SERVICE.lockResource( selectedResourceId );
        } );
        lockMenuItem.setEnabled( false );

        // unlock item
        unlockMenuItem = new JBMenuItem( "Unlock" );
        unlockMenuItem.addActionListener( actionEvent -> {
            CONNECTION_SERVICE.unlockResource( selectedResourceId );
        } );
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

    public void setSearchFromParentFolderListener( Action action ) {
        searchParentMenuItem.addActionListener( action );
    }

    /**
     * disable all menu items
     */
    private void disableAllMenuItems( ) {
        selectParentFolderId.setEnabled( false );
        searchParentMenuItem.setEnabled( true );
        showPathMenuItem.setEnabled( false );
        showContentMenuItem.setEnabled( false );
        lockMenuItem.setEnabled( false );
        unlockMenuItem.setEnabled( false );
        addDocumentMenuItem.setEnabled( false );
        addFolderMenuItem.setEnabled( false );
        addLinkMenuItem.setEnabled( false );

        if ( SETTINGS.isShowDelete( ) ) {
            deleteResourceMenuItem.setEnabled( false );
        }
    }

    @Override
    public void popupMenuWillBecomeVisible( PopupMenuEvent popupMenuEvent ) {
        BasicDynaBean basicDynaBean = null;
        ResultTableContextMenu menu = ( ResultTableContextMenu ) popupMenuEvent.getSource( );
        JTable invoker = ( JTable ) menu.getInvoker( );

        // disable all menu items
        disableAllMenuItems( );

        // get selected table row and search for selected resource id
        try {
            int selectedRow = invoker.getSelectedRow( );
            int selectedColumn = invoker.getSelectedColumn( );
            if ( selectedRow > -1 ) {
                basicDynaBean = ( BasicDynaBean ) invoker.getValueAt( selectedRow, selectedColumn );
            }
        } catch ( Exception e ) {
            logger.warn( ExceptionUtils.getRootCause( e ).getLocalizedMessage( ) );
            return;
        }

        // if resource id found, enable some menu items
        if ( basicDynaBean != null ) {
            selectedResourceId = ( String ) basicDynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY );
            BaseResource baseResource = CONNECTION_SERVICE.getBaseResource( selectedResourceId );
            if ( !baseResource.isLocked( ) ) {
                lockMenuItem.setEnabled( true );
                unlockMenuItem.setEnabled( false );
            } else {
                lockMenuItem.setEnabled( false );
                unlockMenuItem.setEnabled( true );
            }

            showPathMenuItem.setEnabled( true );
            selectParentFolderId.setEnabled( baseResource.getResourcetype( ) == ResourceType.FOLDER.getId( ) );
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible( PopupMenuEvent popupMenuEvent ) {
    }

    @Override
    public void popupMenuCanceled( PopupMenuEvent popupMenuEvent ) {
    }
}
