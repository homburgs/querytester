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

package com.hsofttec.intellij.querytester.states;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

public class ConnectionSettings {
    private String id;
    private boolean active;
    private String connectionName;
    private boolean ssl;
    private String server;
    private int port;
    private int timeout;
    private int connectTimeout;
    private String instance;
    private String username;
    private String password;

    public ConnectionSettings( ) {
    }

    public String getId( ) {
        if ( StringUtils.isBlank( id ) ) {
            id = RandomStringUtils.randomAlphanumeric( 20 );
        }
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getConnectionName( ) {
        return connectionName;
    }

    public void setConnectionName( final String connectionName ) {
        this.connectionName = connectionName;
    }

    public boolean isSsl( ) {
        return ssl;
    }

    public void setSsl( final boolean ssl ) {
        this.ssl = ssl;
    }

    public String getServer( ) {
        return server;
    }

    public void setServer( final String server ) {
        this.server = server;
    }

    public int getPort( ) {
        return port;
    }

    public void setPort( final int port ) {
        this.port = port;
    }

    public String getInstance( ) {
        return instance;
    }

    public void setInstance( final String instance ) {
        this.instance = instance;
    }

    public String getUsername( ) {
        return username;
    }

    public void setUsername( final String username ) {
        this.username = username;
    }

    public String getPassword( ) {
        return password;
    }

    public void setPassword( final String password ) {
        this.password = password;
    }

    public int getTimeout( ) {
        return timeout;
    }

    public void setTimeout( int timeout ) {
        this.timeout = timeout;
    }

    public boolean isActive( ) {
        return active;
    }

    public void setActive( boolean active ) {
        this.active = active;
    }

    public int getConnectTimeout( ) {
        return connectTimeout;
    }

    public void setConnectTimeout( int connectTimeout ) {
        this.connectTimeout = connectTimeout;
    }
}
