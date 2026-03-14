package com.qtest.pdf;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;

/**
 * Color scheme for PDF reports - immutable, thread-safe.
 * No static fields, can be instantiated per PDF generation.
 */
public class ColorScheme {
    // Status colors
    public static final Color PASSED = new DeviceRgb(52, 168, 83);    // Green
    public static final Color FAILED = new DeviceRgb(229, 57, 53);     // Red
    public static final Color SKIPPED = new DeviceRgb(255, 152, 0);    // Amber
    public static final Color PENDING = new DeviceRgb(158, 158, 158);  // Gray

    // Text colors
    public static final Color TEXT_PRIMARY = new DeviceRgb(33, 33, 33);      // Dark gray
    public static final Color TEXT_SECONDARY = new DeviceRgb(117, 117, 117); // Light gray
    public static final Color TEXT_WHITE = ColorConstants.WHITE;

    // Background colors
    public static final Color BG_PASSED = new DeviceRgb(232, 245, 233);      // Light green
    public static final Color BG_FAILED = new DeviceRgb(253, 232, 231);      // Light red
    public static final Color BG_SKIPPED = new DeviceRgb(255, 243, 224);     // Light amber
    public static final Color BG_PENDING = new DeviceRgb(245, 245, 245);     // Light gray
    public static final Color BG_HEADER = new DeviceRgb(33, 33, 33);         // Dark background
    public static final Color BG_ROW_ALT = new DeviceRgb(248, 248, 248);     // Alternate row

    // Border colors
    public static final Color BORDER = new DeviceRgb(224, 224, 224);

    /**
     * Get status color based on status string
     */
    public static Color getStatusColor(String status) {
        if (status == null) return PENDING;
        switch (status.toUpperCase()) {
            case "PASSED":
                return PASSED;
            case "FAILED":
                return FAILED;
            case "SKIPPED":
            case "PENDING":
                return SKIPPED;
            default:
                return PENDING;
        }
    }

    /**
     * Get background color for status
     */
    public static Color getStatusBackgroundColor(String status) {
        if (status == null) return BG_PENDING;
        switch (status.toUpperCase()) {
            case "PASSED":
                return BG_PASSED;
            case "FAILED":
                return BG_FAILED;
            case "SKIPPED":
            case "PENDING":
                return BG_SKIPPED;
            default:
                return BG_PENDING;
        }
    }
}
