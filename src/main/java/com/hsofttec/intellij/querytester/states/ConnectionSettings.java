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


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
public class ConnectionSettings {
    @Setter
    private String id;
    @Setter
    @Getter
    private boolean active;
    @Setter
    @Getter
    private String connectionName;
    @Setter
    @Getter
    private boolean ssl;
    @Setter
    @Getter
    private String server;
    @Setter
    @Getter
    private int port;
    @Setter
    @Getter
    private int timeout;
    @Setter
    @Getter
    private int connectTimeout;
    @Setter
    @Getter
    private String instance;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private String password;

    public String getId() {
        if (StringUtils.isBlank(id)) {
            id = RandomStringUtils.secure().nextAlphabetic(20);
        }
        return id;
    }
}
