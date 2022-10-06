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
import com.ceyoniq.nscale.al.core.repository.ResourceKey;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.hsofttec.intellij.querytester.QueryTesterConstants;
import com.hsofttec.intellij.querytester.services.ConnectionService;
import com.hsofttec.intellij.querytester.ui.components.ModifyResourceComponent;
import com.hsofttec.intellij.querytester.ui.components.ObjectclassSelect;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.beanutils.DynaBean;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import javax.swing.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Date;

public class ModifyResourceDialog extends DialogWrapper {
    private static final ConnectionService CONNECTION_SERVICE = ConnectionService.getInstance( );

    private final Project project;
    private ModifyResourceComponent dlgComponent = null;

    public ModifyResourceDialog( Project project ) {
        super( project, true );
        this.project = project;
        setModal( true );
        setHorizontalStretch( 2 );
        init( );
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel( ) {
        dlgComponent = new ModifyResourceComponent( );
        return dlgComponent.getMainPanel( );
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate( ) {
        return null;
    }

    public void getData( ) {
        return;
    }

    public void setData( DynaBean dynaBean, String propertyName ) {
        JComponent inputField = null;
        Object value = dynaBean.get( propertyName );
        dlgComponent.getFieldLabel( ).setText( propertyName );
        if ( value instanceof DateTime ) {
            DatePickerSettings datePickerSettings = new DatePickerSettings( );
            datePickerSettings.setFirstDayOfWeek( DayOfWeek.MONDAY );
            TimePickerSettings timePickerSettings = new TimePickerSettings( );
            inputField = new DateTimePicker( datePickerSettings, timePickerSettings );
            ( ( DateTimePicker ) inputField ).setDateTimePermissive( LocalDateTime.now( ) );
        } else if ( value instanceof Date ) {
            DatePickerSettings datePickerSettings = new DatePickerSettings( );
            datePickerSettings.setFirstDayOfWeek( DayOfWeek.MONDAY );
            inputField = new DatePicker( datePickerSettings );
            ( ( DatePicker ) inputField ).setDateToToday( );
        } else if ( value instanceof String || value instanceof Long || value instanceof Integer ) {
            inputField = new JBTextField( String.valueOf( value ) );
        } else if ( value instanceof ObjectclassName ) {
            inputField = new ObjectclassSelect( project, ( ResourceKey ) dynaBean.get( QueryTesterConstants.DBEAN_PROPERTY_NAME_KEY ) );
            ( ( ObjectclassSelect ) inputField ).setSelectedItem( value );
        } else if ( value instanceof Boolean ) {
            inputField = new JCheckBox( "true/false", ( Boolean ) value );
        }
        dlgComponent.setInputField( inputField );
    }
}
