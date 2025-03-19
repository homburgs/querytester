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

package com.hsofttec.intellij.querytester.ui;

import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.models.ResourceDialogModel;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.components.CreateResourceComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.CollectionComboBoxModel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateResourceDialog extends DialogWrapper {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );
    private final Project project;
    private final CreationMode creationMode;
    private BaseResource parentFolder;
    private CreateResourceComponent dlgComponent = null;

    public CreateResourceDialog( Project project, CreationMode creationMode ) {
        super( project, true );
        this.project = project;
        this.creationMode = creationMode;
        setModal( true );
        setHorizontalStretch( 2 );
        init( );
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel( ) {
        dlgComponent = new CreateResourceComponent( );
        dlgComponent.getInputObjectclass( ).requestFocus( );
        dlgComponent.getInputContent().addBrowseFolderListener(project, new FileChooserDescriptor(true, false, true, true, false, false));
        return dlgComponent.getMainPanel( );
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate( ) {
        ObjectclassName objectclassName = ( ObjectclassName ) dlgComponent.getInputObjectclass( ).getSelectedItem( );
        String displayName = dlgComponent.getInputDisplayname( ).getText( );

        if ( objectclassName == null ) {
            return new ValidationInfo( "No objectclass selected", dlgComponent.getInputObjectclass( ) );
        }
        if ( StringUtils.isBlank( displayName ) ) {
            return new ValidationInfo( "DisplayName is mandatory", dlgComponent.getInputDisplayname( ) );
        }

        if ( creationMode == CreationMode.DOCUMENT ) {
            String fileName = dlgComponent.getInputContent( ).getText( );
            if ( StringUtils.isBlank( fileName ) ) {
                return new ValidationInfo( "Content is mandatory", dlgComponent.getInputDisplayname( ) );
            }
        }

        return null;
    }

    public ResourceDialogModel getData( ) {
        ResourceDialogModel resourceDialogModel = new ResourceDialogModel( );
        resourceDialogModel.setCreationMode( creationMode );
        resourceDialogModel.setParentResource( parentFolder );
        resourceDialogModel.setDisplayname( dlgComponent.getInputDisplayname( ).getText( ) );
        resourceDialogModel.setObjectclassName( ( ObjectclassName ) dlgComponent.getInputObjectclass( ).getSelectedItem( ) );
        if ( creationMode == CreationMode.DOCUMENT ) {
            resourceDialogModel.setSelectedFileName( dlgComponent.getInputContent( ).getText( ) );
        }
        return resourceDialogModel;
    }

    public void setData( BaseResource parentFolder ) {
        List<ObjectclassName> possibleObjectclasses = new ArrayList<>( );
        String titlePatten = "";

        this.parentFolder = parentFolder;

        switch ( creationMode ) {
            case DOCUMENT:
                titlePatten = "Create document in %s";
                possibleObjectclasses = CONNECTION_SERVICE.getPossibleObjectclassesForDocumentCreation( parentFolder );
                break;
            case LINK:
                titlePatten = "Create link in %s";
                break;
            case FOLDER:
                titlePatten = "Create folder in %s";
                possibleObjectclasses = CONNECTION_SERVICE.getPossibleObjectclassesForFolderCreation( parentFolder );
                break;
        }
        setTitle( String.format( titlePatten, parentFolder.getDisplayname( ) ) );

        dlgComponent.getInputObjectclass( ).setModel( new CollectionComboBoxModel<>( possibleObjectclasses ) );
        dlgComponent.getInputObjectclass( ).setRenderer( new BasicComboBoxRenderer( ) {
            @Override
            public Component getListCellRendererComponent( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                ObjectclassName value1 = ( ObjectclassName ) value;
                return super.getListCellRendererComponent( list, value1.getName( ), index, isSelected, cellHasFocus );
            }
        } );

        dlgComponent.getInputParentFolder( ).setText( parentFolder.getDisplayname( ) );

        if ( creationMode == CreationMode.DOCUMENT ) {
            dlgComponent.getLabelContent( ).setVisible( true );
            dlgComponent.getInputContent( ).setVisible( true );
        }
    }

    public enum CreationMode {
        FOLDER,
        DOCUMENT,
        LINK
    }
}
