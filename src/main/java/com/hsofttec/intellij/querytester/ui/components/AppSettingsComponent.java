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

import com.google.common.eventbus.EventBus;
import com.hsofttec.intellij.querytester.events.ConnectionAddedEvent;
import com.hsofttec.intellij.querytester.events.ConnectionChangedEvent;
import com.hsofttec.intellij.querytester.events.ConnectionRemovedEvent;
import com.hsofttec.intellij.querytester.models.FontFaceComboBoxModel;
import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.ConnectionSetupDialog;
import com.hsofttec.intellij.querytester.ui.EventBusFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;

public class AppSettingsComponent {
    private static final ConnectionSettingsService connectionSettingsService = ConnectionSettingsService.getSettings( );

    private static final ProjectManager projectManager = ProjectManager.getInstance( );

    private static final EventBus EVENT_BUS = EventBusFactory.getInstance( ).get( );

    private JCheckBox inputShowIdColumn;
    private JCheckBox inputShowKeyColumn;
    private JPanel settingsPanel;
    private JCheckBox inputShowDelete;
    private JTextField inputMaxResultSize;
    private JLabel labelMaxResultSize;
    private JLabel labelMaxHistorySize;
    private JTextField inputMaxHistorySize;
    private JComboBox<String> inputFontFace;
    private JLabel labelFontFace;
    private JTextField inputFontSize;
    private JLabel labelFontSize;
    private ConnectionList listConnections;
    private JPanel panelConnections;

    public AppSettingsComponent( ) {
        inputFontFace.setModel( new FontFaceComboBoxModel( ) );
        createConnectionsToolbar( );
    }

    public JPanel getPanel( ) {
        return settingsPanel;
    }

    public JComponent getPreferredFocusedComponent( ) {
        return inputShowIdColumn;
    }

    public boolean getShowIdColumnValue( ) {
        return inputShowIdColumn.isSelected( );
    }

    public void setShowIdColumnValue( boolean value ) {
        inputShowIdColumn.setSelected( value );
    }

    public boolean getShowKeyColumnValue( ) {
        return inputShowKeyColumn.isSelected( );
    }

    public void setShowKeyColumnValue( boolean value ) {
        inputShowKeyColumn.setSelected( value );
    }

    public String getFontFaceValue( ) {
        return ( String ) inputFontFace.getSelectedItem( );
    }

    public void setFontFaceValue( String value ) {
        ComboBoxModel<String> model = inputFontFace.getModel( );
        for ( int i = 0; i < model.getSize( ); i++ ) {
            if ( model.getElementAt( i ).equals( value ) ) {
                inputFontFace.setSelectedIndex( i );
                return;
            }
        }
        inputFontFace.setSelectedIndex( 0 );
    }

    public int getFontSizeValue( ) {
        String text = inputFontSize.getText( );
        if ( StringUtils.isBlank( text ) ) {
            text = "10";
        }
        return Integer.parseInt( text );
    }

    public void setFontSizeValue( int value ) {
        inputFontSize.setText( Integer.toString( value ) );
    }

    public int getMaxHistorySizeValue( ) {
        String text = inputMaxHistorySize.getText( );
        if ( StringUtils.isBlank( text ) ) {
            text = "0";
        }
        return Integer.parseInt( text );
    }

    public void setMaxHistorySizeValue( int value ) {
        inputMaxHistorySize.setText( Integer.toString( value ) );
    }

    public int getMaxResultSizeValue( ) {
        String text = inputMaxResultSize.getText( );
        if ( StringUtils.isBlank( text ) ) {
            text = "0";
        }
        return Integer.parseInt( text );
    }

    public void setMaxResultSizeValue( int value ) {
        inputMaxResultSize.setText( Integer.toString( value ) );
    }

    private void createConnectionsToolbar( ) {
        ToolbarDecorator decorationToolbar = ToolbarDecorator.createDecorator( listConnections );
        decorationToolbar.setAddAction( anActionButton -> {
            Project currentProject = projectManager.getOpenProjects( )[ 0 ];
            ConnectionSettingsService.ConnectionSettings connectionSettings = new ConnectionSettingsService.ConnectionSettings( );
            ConnectionSetupDialog connectionSetupDialog = new ConnectionSetupDialog( currentProject );
            connectionSetupDialog.setData( connectionSettings );
            if ( connectionSetupDialog.showAndGet( ) ) {
                connectionSettings = connectionSetupDialog.getData( );
                listConnections.addElement( connectionSettings );
                connectionSettingsService.connectionSettingsState.connectionSettings.add( connectionSettings );
                EVENT_BUS.post( new ConnectionAddedEvent( connectionSettings ) );
            }
        } );
        decorationToolbar.setRemoveAction( anActionButton -> {
            ConnectionSettingsService.ConnectionSettings selectedValue = listConnections.getSelectedValue( );
            listConnections.removeElement( selectedValue );
            connectionSettingsService.removeConnection( selectedValue.getId( ) );
            EVENT_BUS.post( new ConnectionRemovedEvent( selectedValue ) );
        } );
        decorationToolbar.setEditAction( anActionButton -> {
            ConnectionSettingsService.ConnectionSettings selectedValue = listConnections.getSelectedValue( );
            Project currentProject = projectManager.getOpenProjects( )[ 0 ];
            ConnectionSetupDialog connectionSetupDialog = new ConnectionSetupDialog( currentProject );
            connectionSetupDialog.setData( selectedValue );
            if ( connectionSetupDialog.showAndGet( ) ) {
                selectedValue = connectionSetupDialog.getData( );
                listConnections.repaint( );
                if ( selectedValue.isActive( ) ) {
                    EVENT_BUS.post( new ConnectionChangedEvent( selectedValue ) );
                } else {
                    EVENT_BUS.post( new ConnectionRemovedEvent( selectedValue ) );
                }
            }
        } );
        panelConnections.add( decorationToolbar.disableUpDownActions( ).createPanel( ), BorderLayout.NORTH );
        panelConnections.setBorder( IdeBorderFactory.createTitledBorder( "Connections", false ) );
    }
}