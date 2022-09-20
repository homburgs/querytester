package com.hsofttec.intellij.querytester.models;

import javax.swing.*;
import java.awt.*;

public class FontFaceComboBoxModel extends DefaultComboBoxModel<String> {
    public FontFaceComboBoxModel( ) {
        super( GraphicsEnvironment.getLocalGraphicsEnvironment( ).getAvailableFontFamilyNames( ) );

    }
}
