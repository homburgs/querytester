package com.hsofttec.intellij.querytester.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsState {
    private boolean showIdColumn;
    private boolean showKeyColumn;
    private int maxResultSize;
    private boolean showDelete;
    private int maxHistorySize;
    private String fontFace;
    private int fontSize;

    public SettingsState( ) {
        showKeyColumn = true;
        showIdColumn = true;
        showDelete = false;
        maxHistorySize = 25;
        maxResultSize = 100;
        fontFace = "JetBrains Mono";
        fontSize = 14;
    }
}
