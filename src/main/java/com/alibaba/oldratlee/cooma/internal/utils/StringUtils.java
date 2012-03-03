/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.oldratlee.cooma.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * StringUtils
 * 
 * @author oldratlee
 * @since 0.1.0
 */
public final class StringUtils {
    public static String toString(Throwable e) {
        StringWriter w = new StringWriter(1024);
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    /**
     * @param msg
     * @param e
     * @return string
     */
    public static String toString(String msg, Throwable e) {
        StringWriter w = new StringWriter(1024);
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    private StringUtils() {
    }
}
