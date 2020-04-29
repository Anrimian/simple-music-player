package com.github.anrimian.musicplayer.domain.utils;


public class TextUtils {

    public static String getLastPathSegment(String text) {
        String result = text;
        int lastSlashIndex = text.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            result = text.substring(++lastSlashIndex);
        }
        return result;
    }

    public static String removeLastPathSegment(String text) {
        String result = text;
        int lastSlashIndex = text.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            result = text.substring(0, ++lastSlashIndex);
        }
        return result;
    }

    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    /**
     * copied from android package for using in non-device specific layers
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * copied from android package for using in non-device specific layers
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static String between(String source, String start, String end) {
        int startIndex = indexOfEnd(source, start);
        return source.substring(startIndex, source.indexOf(end, startIndex));
    }

    public static int indexOfEnd(String text, String source) {
        return text.indexOf(source) + source.length();
    }

    public static String nullIfEmpty(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        return text;
    }

    public static String toNullableString(StringBuilder sb) {
        return sb.length() == 0 ? null : sb.toString();
    }

    public static String replaceLast(String string, String from, String to) {
        int lastIndex = string.lastIndexOf(from);
        if (lastIndex < 0) {
            return string;
        }
        String tail = string.substring(lastIndex).replace(from, to);
        return string.substring(0, lastIndex) + tail;
    }
}
