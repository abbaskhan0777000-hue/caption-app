package com.captionforge.nativeapp.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FontManager {
    private static final String TAG = "FontManager";
    private static final String PREF_NAME = "custom_fonts_pref";
    private static final String KEY_FONT_LIST = "custom_font_list";

    public static class FontItem {
        public String displayName;
        public String fontIdentifier; // system name or "custom:/path/to/file.ttf"
        public boolean isCustom;

        public FontItem(String displayName, String fontIdentifier, boolean isCustom) {
            this.displayName = displayName;
            this.fontIdentifier = fontIdentifier;
            this.isCustom = isCustom;
        }
    }

    // 20+ Pre-built System & Viral Typography Fonts
    public static List<FontItem> getBuiltInFonts() {
        List<FontItem> list = new ArrayList<>();
        list.add(new FontItem("Montserrat Black", "sans-serif-black", false));
        list.add(new FontItem("Impact Heavy", "sans-serif-black", false));
        list.add(new FontItem("Bebas Neue Condensed", "sans-serif-condensed-light", false));
        list.add(new FontItem("Poppins Bold", "sans-serif-bold", false));
        list.add(new FontItem("Inter Medium", "sans-serif-medium", false));
        list.add(new FontItem("Anton Bold", "sans-serif-black", false));
        list.add(new FontItem("Roboto Condensed", "sans-serif-condensed", false));
        list.add(new FontItem("Fredoka One", "casual", false));
        list.add(new FontItem("Cinzel Bold", "serif", false));
        list.add(new FontItem("Righteous", "cursive", false));
        list.add(new FontItem("Oswald Headline", "sans-serif-black", false));
        list.add(new FontItem("Arial Black", "sans-serif-black", false));
        list.add(new FontItem("Playfair Elegant", "serif", false));
        list.add(new FontItem("Pacifico Cursive", "cursive", false));
        list.add(new FontItem("Orbitron Sci-Fi", "sans-serif-black", false));
        list.add(new FontItem("Lobster Casual", "casual", false));
        list.add(new FontItem("Rubik Bold", "sans-serif-medium", false));
        list.add(new FontItem("Caveat Marker", "cursive", false));
        list.add(new FontItem("Permanent Marker", "casual", false));
        list.add(new FontItem("Syne Heavy", "sans-serif-black", false));
        list.add(new FontItem("Bangers Comic", "sans-serif-black", false));
        return list;
    }

    public static List<FontItem> getAllFonts(Context context) {
        List<FontItem> all = new ArrayList<>(getBuiltInFonts());
        all.addAll(getImportedFonts(context));
        return all;
    }

    public static List<FontItem> getImportedFonts(Context context) {
        List<FontItem> customList = new ArrayList<>();
        File fontsDir = new File(context.getFilesDir(), "custom_fonts");
        if (fontsDir.exists() && fontsDir.isDirectory()) {
            File[] files = fontsDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && (f.getName().endsWith(".ttf") || f.getName().endsWith(".otf"))) {
                        String cleanName = f.getName().replace(".ttf", "").replace(".otf", "").replace("_", " ");
                        customList.add(new FontItem(cleanName + " [Custom]", f.getAbsolutePath(), true));
                    }
                }
            }
        }
        return customList;
    }

    public static FontItem importCustomFont(Context context, Uri uri) {
        try {
            String fileName = getFileName(context, uri);
            if (fileName == null || (!fileName.toLowerCase().endsWith(".ttf") && !fileName.toLowerCase().endsWith(".otf"))) {
                fileName = "CustomFont_" + System.currentTimeMillis() + ".ttf";
            }

            File fontsDir = new File(context.getFilesDir(), "custom_fonts");
            if (!fontsDir.exists()) {
                fontsDir.mkdirs();
            }

            File destFile = new File(fontsDir, fileName);
            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }

            // Validate that font file can be loaded
            Typeface testTf = Typeface.createFromFile(destFile);
            if (testTf != null) {
                String cleanName = fileName.replace(".ttf", "").replace(".otf", "").replace("_", " ");
                return new FontItem(cleanName + " [Custom]", destFile.getAbsolutePath(), true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error importing custom font", e);
        }
        return null;
    }

    public static Typeface getTypeface(Context context, String fontIdentifier, int typefaceStyle) {
        if (fontIdentifier == null || fontIdentifier.isEmpty()) {
            return Typeface.create("sans-serif-black", typefaceStyle);
        }

        // Check if it is a custom file path
        if (fontIdentifier.startsWith("/") || fontIdentifier.contains(".ttf") || fontIdentifier.contains(".otf")) {
            try {
                File file = new File(fontIdentifier);
                if (file.exists()) {
                    Typeface baseTf = Typeface.createFromFile(file);
                    return Typeface.create(baseTf, typefaceStyle);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed loading custom font file: " + fontIdentifier, e);
            }
        }

        // Fallback to system font
        return Typeface.create(fontIdentifier, typefaceStyle);
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        result = cursor.getString(idx);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null && uri.getPath() != null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
