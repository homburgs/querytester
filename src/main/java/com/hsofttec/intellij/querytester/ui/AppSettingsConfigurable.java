/*
 * The MIT License (MIT)
 *
 * Copyright © 2023 Sven Homburg, <homburgs@gmail.com>
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

import com.hsofttec.intellij.querytester.services.SettingsService;
import com.hsofttec.intellij.querytester.states.SettingsState;
import com.hsofttec.intellij.querytester.ui.components.AppSettingsComponent;
import com.hsofttec.intellij.querytester.ui.notifiers.FontSettingsChangedNotifier;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent appSettingsComponent;
    private MessageBus messageBus;

    @Nls( capitalization = Nls.Capitalization.Title )
    @Override
    public String getDisplayName( ) {
        return "nscale QueryTester Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent( ) {
        return appSettingsComponent.getPreferredFocusedComponent( );
    }

    @Nullable
    @Override
    public JComponent createComponent( ) {
        Project project = ProjectManager.getInstance( ).getOpenProjects( )[ 0 ];
        messageBus = project.getMessageBus( );
        appSettingsComponent = new AppSettingsComponent( project );
        return appSettingsComponent.getPanel( );
    }

    @Override
    public boolean isModified( ) {
        SettingsState settings = SettingsService.getSettings( );
        boolean modified = appSettingsComponent.getShowIdColumnValue( ) != settings.isShowIdColumn( );
        modified |= appSettingsComponent.getShowKeyColumnValue() != settings.isShowKeyColumn();
        modified |= appSettingsComponent.getDisplayLockIcon() != settings.isDisplayLockItem();
        modified |= appSettingsComponent.getShowDeleteValue() != settings.isShowDelete();
        modified |= appSettingsComponent.getMaxHistorySizeValue( ) != settings.getMaxHistorySize( );
        modified |= appSettingsComponent.getMaxResultSizeValue( ) != settings.getMaxResultSize( );
        modified |= appSettingsComponent.getFontSizeValue( ) != settings.getFontSize( );
        modified |= !appSettingsComponent.getFontFaceValue().equals(settings.getFontFace());
        modified |= !appSettingsComponent.getFieldSeparator().equals(settings.getFieldSeparator());
        return modified;
    }

    @Override
    public void apply( ) {
        SettingsState settings = SettingsService.getSettings( );
        settings.setShowIdColumn( appSettingsComponent.getShowIdColumnValue( ) );
        settings.setShowKeyColumn(appSettingsComponent.getShowKeyColumnValue());
        settings.setDisplayLockItem(appSettingsComponent.getDisplayLockIcon());
        settings.setShowDelete(appSettingsComponent.getShowDeleteValue());
        settings.setMaxHistorySize( appSettingsComponent.getMaxHistorySizeValue( ) );
        settings.setMaxResultSize( appSettingsComponent.getMaxResultSizeValue( ) );
        settings.setFontFace( appSettingsComponent.getFontFaceValue( ) );
        settings.setFontSize(appSettingsComponent.getFontSizeValue());
        settings.setFieldSeparator(appSettingsComponent.getFieldSeparator());
        FontSettingsChangedNotifier notifier = messageBus.syncPublisher( FontSettingsChangedNotifier.FONT_SETTINGS_CHANGED_TOPIC );
        notifier.doAction( );
    }

    @Override
    public void reset( ) {
        SettingsState settings = SettingsService.getSettings( );
        appSettingsComponent.setShowIdColumnValue( settings.isShowIdColumn( ) );
        appSettingsComponent.setShowKeyColumnValue( settings.isShowKeyColumn( ) );
        appSettingsComponent.setShowDeleteValue(settings.isShowDelete());
        appSettingsComponent.setDisplayLockIcon(settings.isDisplayLockItem());
        appSettingsComponent.setMaxHistorySizeValue(settings.getMaxHistorySize());
        appSettingsComponent.setMaxResultSizeValue( settings.getMaxResultSize( ) );
        appSettingsComponent.setFontSizeValue( settings.getFontSize( ) );
        appSettingsComponent.setFontFaceValue(settings.getFontFace());
        appSettingsComponent.setFieldSeparator(settings.getFieldSeparator());
    }

    @Override
    public void disposeUIResources( ) {
        appSettingsComponent = null;
    }
}
