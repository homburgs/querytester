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

    private static void notify( Notification notification ) {
        Notifications.Bus.notify( notification );
    }
}
