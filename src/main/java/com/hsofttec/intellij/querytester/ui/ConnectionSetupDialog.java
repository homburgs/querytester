package com.hsofttec.intellij.querytester.ui;

import com.hsofttec.intellij.querytester.services.ConnectionSettingsService;
import com.hsofttec.intellij.querytester.ui.components.ConnectionSetupComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConnectionSetupDialog extends DialogWrapper {
    private final Project project;
    ConnectionSetupComponent connectionSetupComponent = null;

    public ConnectionSetupDialog( Project project ) {
        super( project, true );
        this.project = project;
        setModal( true );
        init( );
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel( ) {
        connectionSetupComponent = new ConnectionSetupComponent( );
        connectionSetupComponent.getInputConnectioName( ).requestFocus( );
        return connectionSetupComponent.getMainPanel( );
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate( ) {
        String connectioName = connectionSetupComponent.getInputConnectioName( ).getText( );
        String server = connectionSetupComponent.getInputServer( ).getText( );
        String port = connectionSetupComponent.getInputPort( ).getText( );
        String timeout = connectionSetupComponent.getInputPort( ).getText( );
        String username = connectionSetupComponent.getInputUsername( ).getText( );
        String instance = connectionSetupComponent.getInputInstance( ).getText( );
        char[] password = connectionSetupComponent.getInputPassword( ).getPassword( );

        if ( StringUtils.isBlank( connectioName ) ) {
            return new ValidationInfo( "Connection name is mandatory", connectionSetupComponent.getInputConnectioName( ) );
        }

        if ( StringUtils.isBlank( server ) ) {
            return new ValidationInfo( "Server is mandatory", connectionSetupComponent.getInputServer( ) );
        }

        if ( !StringUtils.isNumeric( port ) ) {
            return new ValidationInfo( "Port is mandatory and must be an integer value", connectionSetupComponent.getInputPort( ) );
        }

        if ( !StringUtils.isNumeric( port ) ) {
            return new ValidationInfo( "Port is mandatory and must be an integer value", connectionSetupComponent.getInputPort( ) );
        }

        if ( !StringUtils.isNumeric( timeout ) ) {
            return new ValidationInfo( "Timeout is mandatory and must be an integer value", connectionSetupComponent.getInputTimeout( ) );
        }

        if ( StringUtils.isBlank( instance ) ) {
            return new ValidationInfo( "Instance is mandatory and must be an integer value", connectionSetupComponent.getInputInstance( ) );
        }

        if ( StringUtils.isBlank( username ) ) {
            return new ValidationInfo( "Username is mandatory", connectionSetupComponent.getInputUsername( ) );
        }

        if ( password.length == 0 ) {
            return new ValidationInfo( "Password is mandatory", connectionSetupComponent.getInputPassword( ) );
        }

        return null;
    }

    public void getData( ConnectionSettingsService.ConnectionSettings data ) {
        connectionSetupComponent.getData( data );
    }

    public void setData( ConnectionSettingsService.ConnectionSettings data ) {

        if ( StringUtils.isBlank( data.getConnectionName( ) ) ) {
            setTitle( "Add New Connection Settings" );
            data.setServer( "127.0.0.1" );
            data.setPort( 8080 );
            data.setSsl( false );
            data.setInstance( "nscalealinst1" );
            data.setUsername( "admin@nscale" );
            data.setTimeout( 0 );
            setOKButtonText( "Add" );
        } else {
            setTitle( String.format( "Change Settings For '%s'", data.getConnectionName( ) ) );
            setOKButtonText( "Save Changes" );
        }

        connectionSetupComponent.setData( data );
    }
}
