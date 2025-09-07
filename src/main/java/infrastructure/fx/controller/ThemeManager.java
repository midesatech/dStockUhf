package infrastructure.fx.controller;

import java.util.prefs.Preferences;

public final class ThemeManager {
    public enum UiTheme { OCEAN, GREEN, DARK, OBSIDIAN }

    private static final String PREF_NODE = "dstockuhf/ui";
    private static final String KEY_THEME = "theme";
    private static final UiTheme DEFAULT = UiTheme.OCEAN;

    private ThemeManager() {}

    public static UiTheme getTheme() {
        String v = prefs().get(KEY_THEME, DEFAULT.name());
        try { return UiTheme.valueOf(v); } catch (IllegalArgumentException e) { return DEFAULT; }
    }

    public static void setTheme(UiTheme t) {
        prefs().put(KEY_THEME, t.name());
    }

    public static String cssClassFor(UiTheme t) {
        return switch (t) {
            case OCEAN -> "ocean-soft";
            case GREEN -> "green-soft";
            case DARK -> "dark-menu";
            case OBSIDIAN -> "obsidian-menu";
        };
    }

    private static Preferences prefs() {
        return Preferences.userRoot().node(PREF_NODE);
    }
}

