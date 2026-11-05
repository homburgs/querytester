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

import com.ceyoniq.nscale.al.core.authority.ResourcePermissions;
import com.ceyoniq.nscale.al.core.common.ObjectclassName;
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.ceyoniq.nscale.al.core.repository.ResourceType;
import com.hsofttec.intellij.querytester.models.BaseResource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * objectclass change of repository resources, restricted by configuration and session permissions
 */
public final class ObjectclassChangeService {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance();

    private ObjectclassChangeService() {
    }

    /**
     * objectclasses the resource can be changed to: allowed by the parent folder objectclass, ChangeObjectclass
     * permission on the current objectclass, WriteProperties (and WriteContent for documents) on the new objectclass
     *
     * @param resourceId resource id
     * @return possible objectclasses without the current one, empty if no change is allowed
     */
    public static List<ObjectclassName> getPossibleObjectclasses(String resourceId) {
        List<ObjectclassName> result = new ArrayList<>();
        BaseResource resource = CONNECTION_SERVICE.getBaseResource(resourceId);
        if (resource.getObjectclass() == null || resource.getResourcetype() == null || resource.getParentresourceid() == null) {
            return result;
        }

        if (!PermissionService.hasPermission(resource.getObjectclass(), ResourcePermissions::isChangeObjectclass)) {
            return result;
        }

        BaseResource parent = CONNECTION_SERVICE.getBaseResource(resource.getParentresourceid());
        ResourceType resourceType = ResourceType.get(resource.getResourcetype());
        List<ObjectclassName> allowed;
        Predicate<ObjectclassName> writePermission;
        if (resourceType.isFolder()) {
            allowed = CONNECTION_SERVICE.getPossibleObjectclassesForFolderCreation(parent);
            writePermission = name -> PermissionService.hasPermission(name, ResourcePermissions::isWriteProperties);
        } else if (resourceType.isDocument()) {
            allowed = CONNECTION_SERVICE.getPossibleObjectclassesForDocumentCreation(parent);
            writePermission = PermissionService::canWriteDocument;
        } else {
            return result;
        }

        for (ObjectclassName objectclassName : allowed) {
            if (!objectclassName.equals(resource.getObjectclass()) && writePermission.test(objectclassName)) {
                result.add(objectclassName);
            }
        }
        return result;
    }

    /**
     * change the objectclass of a repository resource
     *
     * @param resourceId      resource id
     * @param objectclassName new objectclass
     */
    public static void change(String resourceId, ObjectclassName objectclassName) {
        CONNECTION_SERVICE.getSession().getRepositoryService().updateObjectclass(new ResourceKey(resourceId), objectclassName);
    }
}
