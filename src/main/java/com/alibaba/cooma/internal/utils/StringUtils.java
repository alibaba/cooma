/*
 * Copyright 2012-2013 Cooma Team.
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

package com.alibaba.cooma.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * StringUtils
 *
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @since 0.1.0
 */
public final class StringUtils {
    /**
     * Get the String of Throwable, like the output of {@link Throwable#printStackTrace()}.
     *
     * @param throwable the input throwable.
     */
    public static String toString(Throwable throwable) {
        return toString(null, throwable);
    }

    /**
     * Get the String of Throwable, like the output of {@link Throwable#printStackTrace()}.
     *
     * @param head      the head line of message.
     * @param throwable the input throwable.
     */
    public static String toString(String head, Throwable throwable) {
        StringWriter w = new StringWriter(1024);
        if (head != null) w.write(head + "\n");
        PrintWriter p = new PrintWriter(w);
        try {
            throwable.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    /**
     * convert CamelCase string to dot-split lowercase string.
     * <p/>
     * eg: convert <code>LessIsMore</code> to <code>less.is.more</code>
     */
    public static String toDotSpiteString(String input) {
        char[] charArray = input.toCharArray();
        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < charArray.length; i++) {
            if (Character.isUpperCase(charArray[i])) {
                if (i != 0 && charArray[i - 1] != '.') {
                    sb.append('.');
                }
                sb.append(Character.toLowerCase(charArray[i]));
            } else {
                sb.append(charArray[i]);
            }
        }
        return sb.toString();
    }

    private StringUtils() {
    }
}
