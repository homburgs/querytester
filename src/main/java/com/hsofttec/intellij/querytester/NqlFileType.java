package com.hsofttec.intellij.querytester;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NqlFileType extends LanguageFileType {
    @NotNull
    public static final NqlFileType INSTANCE = new NqlFileType( );

    private NqlFileType( ) {
        this( NqlLanguage.INSTANCE );
    }

    protected NqlFileType( @NotNull NqlLanguage language ) {
        super( language );
    }

    /**
     * Returns the name of the file type. The name must be unique among all file types registered in the system.
     *
     * @return The file type name.
     */
    @NotNull
    @Override
    public String getName( ) {
        return getLanguage( ).getID( ) + " file";
    }

    /**
     * Returns the user-readable description of the file type.
     *
     * @return The file type description.
     */
    @NotNull
    @Override
    public String getDescription( ) {
        return getLanguage( ).getDisplayName( );
    }

    /**
     * Returns the default extension for files of the type.
     *
     * @return The extension, not including the leading '.'.
     */
    @NotNull
    @Override
    public String getDefaultExtension( ) {
        return getNqlLanguage( ).getExtension( );
    }

    /**
     * Returns the icon used for showing files of the type.
     *
     * @return The icon instance, or null if no icon should be shown.
     */
    @Nullable
    @Override
    public Icon getIcon( ) {
        return getNqlLanguage( ).getIcon( );
    }

    /**
     * Returns {@see IgnoreLanguage} instance.
     *
     * @return associated language.
     */
    @NotNull
    public NqlLanguage getNqlLanguage( ) {
        return ( NqlLanguage ) getLanguage( );
    }

}
