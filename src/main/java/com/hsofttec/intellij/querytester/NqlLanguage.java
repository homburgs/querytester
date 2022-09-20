package com.hsofttec.intellij.querytester;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NqlLanguage extends Language implements TemplateLanguage, InjectableLanguage {
    public static final NqlLanguage INSTANCE = new NqlLanguage( );

    @SuppressWarnings( "SameReturnValue" )
    // ideally this would be public static, but the static inits in the tests get cranky when we do that
    public static LanguageFileType getDefaultTemplateLang( ) {
        return NqlFileType.INSTANCE;
    }

    public NqlLanguage( ) {
        super( "NQL", "text/nql" );
    }

    public NqlLanguage( @Nullable Language baseLanguage, @NotNull @NonNls final String ID, @NonNls final String @NotNull ... mimeTypes ) {
        super( baseLanguage, ID, mimeTypes );
    }

    public Icon getIcon( ) {
        return null;
    }

    public String getExtension( ) {
        return "nql";
    }
}
