package net.iponweb.disthene.reader.graph;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class FontTable {

    private static Map<String, Font> fontMap = new HashMap<>();


    private static Font HELVETICA = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
    private static Font COURIER = new Font(Font.SANS_SERIF, Font.PLAIN, 1);;
    private static Font TIMES = new Font(Font.SANS_SERIF, Font.PLAIN, 1);;
    private static Font SANS = new Font(Font.SANS_SERIF, Font.PLAIN, 1);;

    static {
        try {
            InputStream is = FontTable.class.getResourceAsStream("/fonts/Helvetica.ttf");
            HELVETICA = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception ignored) {
        }

        try {
            InputStream is = FontTable.class.getResourceAsStream("/fonts/Courier.ttf");
            COURIER = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception ignored) {
        }

        try {
            InputStream is = FontTable.class.getResourceAsStream("/fonts/Times.ttf");
            TIMES = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception ignored) {
        }

        fontMap.put("helvetica", HELVETICA);
        fontMap.put("courier", COURIER);
        fontMap.put("times", TIMES);
        fontMap.put("sans", SANS);
    }

    public static Font getFont(String name, int style, float size) {
        Font font = fontMap.get(name.toLowerCase());
        if (font == null) {
            font = SANS;
        }

        return font.deriveFont(style, size);
    }
}
