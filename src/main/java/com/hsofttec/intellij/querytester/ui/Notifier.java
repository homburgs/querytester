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

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class Notifier {

    private static final Project PROJECT = ProjectManager.getInstance( ).getOpenProjects( )[ 0 ];

    public static void error( String content ) {
        NotificationGroup notificationGroup = new NotificationGroup( "QueryTester", NotificationDisplayType.STICKY_BALLOON, true );
        notify( notificationGroup.createNotification( content, NotificationType.ERROR ) );
    }

    public static void warning( String content ) {
        NotificationGroup notificationGroup = new NotificationGroup( "QueryTester", NotificationDisplayType.STICKY_BALLOON, true );
        notify( notificationGroup.createNotification( content, NotificationType.WARNING ) );
    }

    public static void information( String content ) {
        NotificationGroup notificationGroup = new NotificationGroup( "QueryTester", NotificationDisplayType.STICKY_BALLOON, true );
        notify( notificationGroup.createNotification( content, NotificationType.INFORMATION ) );
    }

    private static void notify( Notification notification ) {
        Notifications.Bus.notify( notification );
    }
}
