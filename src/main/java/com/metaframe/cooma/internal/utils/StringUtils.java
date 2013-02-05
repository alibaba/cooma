package com.metaframe.cooma.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * StringUtils
 * 
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
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
