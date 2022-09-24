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

import com.ceyoniq.nscale.al.core.repository.ResourceType;
import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.ui.components.ResourcePathComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ResourcePathDialog extends DialogWrapper {
    private final Project project;
    private ResourcePathComponent resourcePathComponent = null;
    private BaseResource selectedBaseResource = null;

    public ResourcePathDialog( Project project ) {
        super( project, true );
        this.project = project;
        setModal( true );
        setSize( getSize( ).width + 100, getSize( ).height );
        init( );
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel( ) {
        resourcePathComponent = new ResourcePathComponent( );
        resourcePathComponent.getResourceLIst( ).requestFocus( );
        return resourcePathComponent.getMainPanel( );
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate( ) {
        selectedBaseResource = resourcePathComponent.getData( );

        if ( selectedBaseResource == null || selectedBaseResource.getResourcetype( ) != ResourceType.FOLDER.getId( ) ) {
            return new ValidationInfo( "No folder resource selected" );
        }

        return null;
    }

    public BaseResource getData( ) {
        return selectedBaseResource;
    }

    public void setData( String selectedResourceId ) {
        setTitle( String.format( "Path of Resource %s", selectedResourceId ) );
        resourcePathComponent.setData( selectedResourceId );
    }
}
