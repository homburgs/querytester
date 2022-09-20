package com.hsofttec.intellij.querytester;

import com.hsofttec.intellij.querytester.ui.QueryTester;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class QueryTesterToolWindowFactory implements ToolWindowFactory {
    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent( @NotNull Project project, @NotNull ToolWindow toolWindow ) {
        Content content = ContentFactory.SERVICE.getInstance( ).createContent( new QueryTester( ), "", false );
        toolWindow.getContentManager( ).addContent( content );
    }
}
