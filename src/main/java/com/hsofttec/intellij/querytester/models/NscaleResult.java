package com.hsofttec.intellij.querytester.models;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NscaleResult {
    private final DynaClass dynaClass;
    private final List<DynaBean> dynaBeans;

    private List<String> propertyNames;

    public NscaleResult( DynaClass dynaClass, List<DynaBean> dynaBeans ) {
        this.dynaClass = dynaClass;
        this.dynaBeans = dynaBeans;
        propertyNames = Arrays.stream( dynaClass.getDynaProperties( ) ).map( DynaProperty::getName ).collect( Collectors.toList( ) );
    }

    public List<DynaBean> getDynaBeans( ) {
        return dynaBeans;
    }

    public List<String> getPropertyNames( ) {
        return propertyNames;
    }
}
