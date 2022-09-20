package com.hsofttec.intellij.querytester.utils;

import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DimensionConverter extends Converter<Dimension> {
    @Override
    public @Nullable Dimension fromString( @NotNull String value ) {
        String[] splittedValues = value.split( "\\|" );
        return new Dimension( Integer.parseInt( splittedValues[ 1 ] ), Integer.parseInt( splittedValues[ 0 ] ) );
    }

    @Override
    public @Nullable String toString( @NotNull Dimension value ) {
        return String.format( "%d|%d", value.height, value.width );
    }
}
