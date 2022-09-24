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

import com.hsofttec.intellij.querytester.models.BaseResource;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.intellij.icons.AllIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ResourcePathComponent {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );

    private JPanel mainPanel;
    private com.intellij.ui.treeStructure.Tree resourceTree;

    public ResourcePathComponent( ) {
        resourceTree.setCellRenderer( new DefaultTreeCellRenderer( ) {

            @Override
            public Color getBackgroundNonSelectionColor( ) {
                return null;
            }

            @Override
            public Color getBackground( ) {
                return null;
            }

            @Override
            public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
                DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) value;
                BaseResource baseResource = ( BaseResource ) node.getUserObject( );
                setIcon( AllIcons.Nodes.Folder );
                return super.getTreeCellRendererComponent( tree, baseResource.getDisplayname( ), sel, expanded, leaf, row, hasFocus );
            }
        } );
    }

    public JPanel getMainPanel( ) {
        return mainPanel;
    }

    public com.intellij.ui.treeStructure.Tree getResourceLIst( ) {
        return resourceTree;
    }

    public void setData( String data ) {
        if ( data != null ) {
            List<BaseResource> list = CONNECTION_SERVICE.getParentsUntilRootFolder( data );

            Collections.reverse( list );

            DefaultMutableTreeNode top = new DefaultMutableTreeNode( list.get( 0 ) );
            DefaultMutableTreeNode lastNode = top;
            for ( int i = 1; i < list.size( ); i++ ) {
                BaseResource baseResource = list.get( i );
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode( baseResource );
                lastNode.add( newNode );
                lastNode = newNode;
            }

            resourceTree.setModel( new DefaultTreeModel( top ) );
            for ( int i = 0; i < resourceTree.getRowCount( ); i++ ) {
                resourceTree.expandRow( i );
            }
        }
    }

    public BaseResource getData( ) {
        DefaultMutableTreeNode[] selectedNodes = getResourceLIst( ).getSelectedNodes( DefaultMutableTreeNode.class, null );
        Optional<DefaultMutableTreeNode> first = Arrays.stream( selectedNodes ).findFirst( );
        BaseResource selectedBaseResource = null;

        if ( first.isPresent( ) ) {
            selectedBaseResource = ( BaseResource ) first.get( ).getUserObject( );
        }

        return selectedBaseResource;
    }

    public boolean isModified( String data ) {
        return true;
    }
}
