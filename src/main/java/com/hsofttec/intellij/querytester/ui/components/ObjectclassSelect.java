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

import com.ceyoniq.nscale.al.core.Session;
import com.ceyoniq.nscale.al.core.cfg.FolderObjectclass;
import com.ceyoniq.nscale.al.core.cfg.IndexingPropertyName;
import com.ceyoniq.nscale.al.core.cfg.Objectclass;
import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import com.ceyoniq.nscale.al.core.common.Property;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceKeyInfo;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectclassSelect extends ComboBox<ObjectclassName> {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );

    private final Project project;
    private final ResourceKey resourceKey;

    public ObjectclassSelect( Project project, ResourceKey resourceKey ) {
        this.project = project;
        this.resourceKey = resourceKey;

        if ( resourceKey != null ) {
            getPossibleObjectClasses( resourceKey );
        }

        setRenderer( new DefaultListCellRenderer( ) {
            @Override
            public Component getListCellRendererComponent( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                if ( value != null ) {
                    ObjectclassName objectclassName = ( ObjectclassName ) value;
                    value = objectclassName.getName( );
                }
                return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            }
        } );
    }

    private List<ObjectclassName> getPossibleObjectClasses( ResourceKey resourceKey ) {
        List<ObjectclassName> objectclassNames = new ArrayList<>( );
        Session session = CONNECTION_SERVICE.getSession( );
        if ( session != null ) {
            ResourceKey parentFolder = session.getRepositoryService( ).getParentFolder( resourceKey );
            ResourceKeyInfo resourceKeyInfo = new ResourceKeyInfo( parentFolder );
            List<Property> properties = session.getRepositoryService( ).getProperties( parentFolder, Collections.singletonList( new IndexingPropertyName( "objectclassname", resourceKeyInfo.getDocumentAreaName( ) ) ) );
            Objectclass objectclass = session.getConfigurationService( ).getObjectclass( properties.get( 0 ).value( ) );
            if ( objectclass instanceof FolderObjectclass ) {
//                resourceKeyInfo.getReso
//                ((FolderObjectclass)objectclass).getAllowedFolderObjectclassNames()
            }
        }
        return objectclassNames;
    }
}
