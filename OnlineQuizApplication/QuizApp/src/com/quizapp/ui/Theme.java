package com.quizapp.ui;

import java.awt.*;

/** Shared color/font constants for a consistent look across the app. */
public final class Theme {
    public static final Color PRIMARY = new Color(0x2F, 0x54, 0xD9);
    public static final Color PRIMARY_DARK = new Color(0x1F, 0x3A, 0xA3);
    public static final Color SUCCESS = new Color(0x1E, 0x8E, 0x3E);
    public static final Color ERROR = new Color(0xD3, 0x2F, 0x2F);
    public static final Color BG = new Color(0xF5, 0xF6, 0xFA);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT = new Color(0x21, 0x25, 0x2B);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD_BODY = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    private Theme() { }
}
