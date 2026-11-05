/*
 * The MIT License (MIT)
 *
 * Copyright © 2026 Sven Homburg, <homburgs@gmail.com>
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

package com.hsofttec.intellij.querytester.services;

import com.ceyoniq.nscale.al.core.authority.DocumentPermissions;
import com.ceyoniq.nscale.al.core.authority.FolderPermissions;
import com.ceyoniq.nscale.al.core.authority.ResourcePermissions;
import com.ceyoniq.nscale.al.core.common.ObjectclassName;

import java.util.function.Predicate;

/**
 * objectclass permissions of the current session principal
 */
public final class PermissionService {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();

    private PermissionService() {
    }

    /**
     * any resource permission of the session for the objectclass (null objectclass applies to all) grants the check
     */
    public static boolean hasPermission(ObjectclassName objectclassName, Predicate<ResourcePermissions> check) {
        return CONNECTION_SERVICE.getSession().getSessionPermissions().stream()
                .filter(ResourcePermissions.class::isInstance)
                .map(ResourcePermissions.class::cast)
                .filter(p -> p.getObjectclassName() == null || p.getObjectclassName().equals(objectclassName))
                .anyMatch(check);
    }

    /**
     * CreateDocument permission on the folder objectclass
     */
    public static boolean canCreateDocumentIn(ObjectclassName folderObjectclass) {
        return hasPermission(folderObjectclass, p -> p instanceof FolderPermissions folderPermissions && folderPermissions.isCreateDocument());
    }

    /**
     * WriteProperties and WriteContent permission on the document objectclass
     */
    public static boolean canWriteDocument(ObjectclassName documentObjectclass) {
        return hasPermission(documentObjectclass, p -> p instanceof DocumentPermissions documentPermissions
                && documentPermissions.isWriteProperties() && documentPermissions.isWriteContent());
    }
}
