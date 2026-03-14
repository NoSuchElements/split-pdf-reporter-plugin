package com.qtest.pdf;

import java.awt.*;

/**
 * Color palette for PDF elements (for use with PDFBox, which relies on
 * {@link java.awt.Color}).
 */
public class ColorScheme {

    public static final Color PASSED  = new Color(34, 177, 76);
    public static final Color FAILED  = new Color(237, 28, 36);
    public static final Color SKIPPED = new Color(255, 192, 0);

    public static final Color TEXT_PRIMARY   = new Color(33, 37, 41);
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    public static final Color TEXT_WHITE     = Color.WHITE;

    public static final Color BG_HEADER  = new Color(52, 58, 64);
    public static final Color BG_ROW_ALT = new Color(248, 249, 250);
    public static final Color BORDER     = new Color(222, 226, 230);

    public static Color getStatusColorAwt(String status) {
        if (status == null) {
            return TEXT_SECONDARY;
        }
        String s = status.toUpperCase();
        if (s.contains("PASS")) return PASSED;
        if (s.contains("FAIL")) return FAILED;
        if (s.contains("SKIP")) return SKIPPED;
        return TEXT_SECONDARY;
    }
}
