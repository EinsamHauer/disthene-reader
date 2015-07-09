package net.iponweb.disthene.reader.graph;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrei Ivanov
 */
public class ColorTable {

    private static Map<String, Color> colorMap = new HashMap<>();
    private static List<Color> colorRotationList = new ArrayList<>();

    public static Color BLACK = new Color(0, 0, 0);
    public static Color WHITE = new Color(255, 255, 255);
    public static Color BLUE = new Color(100, 100, 255);
    public static Color GREEN = new Color(0, 200, 0);
    public static Color RED = new Color(200, 0, 50);
    public static Color YELLOW = new Color(255, 255, 0);
    public static Color ORANGE = new Color(255, 165, 0);
    public static Color PURPLE = new Color(200, 100, 255);
    public static Color BROWN = new Color(150, 100, 50);
    public static Color CYAN = new Color(0, 255, 255);
    public static Color AQUA = new Color(0, 150, 150);
    public static Color GRAY = new Color(175, 175, 175);
    public static Color MAGENTA = new Color(255, 0, 255);
    public static Color PINK = new Color(255, 100, 100);
    public static Color GOLD = new Color(200, 200, 0);
    public static Color ROSE = new Color(200, 150, 200);
    public static Color DARK_BLUE = new Color(0, 0, 255);
    public static Color DARK_GREEN = new Color(0, 255, 0);
    public static Color DARK_RED = new Color(255, 0, 0);
    public static Color DARK_GRAY = new Color(111, 111, 111);

    public static Color INVISIBLE = new Color(0, 0, 0, 0);

    private static final Pattern HEX_PATTERN = Pattern.compile("^([A-Fa-f0-9]){6,8}$");

    static {
        colorMap.put("black", BLACK);
        colorMap.put("white", WHITE);
        colorMap.put("blue", BLUE);
        colorMap.put("green", GREEN);
        colorMap.put("red", RED);
        colorMap.put("yellow", YELLOW);
        colorMap.put("orange", ORANGE);
        colorMap.put("purple", PURPLE);
        colorMap.put("brown", BROWN);
        colorMap.put("cyan", CYAN);
        colorMap.put("aqua", AQUA);
        colorMap.put("gray", GRAY);
        colorMap.put("grey", GRAY);
        colorMap.put("magenta", MAGENTA);
        colorMap.put("pink", PINK);
        colorMap.put("gold", GOLD);
        colorMap.put("rose", ROSE);
        colorMap.put("darkblue", DARK_BLUE);
        colorMap.put("darkgreen", DARK_GREEN);
        colorMap.put("darkgred", DARK_RED);
        colorMap.put("darkgray", DARK_GRAY);
        colorMap.put("darkgrey", DARK_GRAY);

        colorRotationList.add(BLUE);
        colorRotationList.add(GREEN);
        colorRotationList.add(RED);
        colorRotationList.add(PURPLE);
        colorRotationList.add(BROWN);
        colorRotationList.add(YELLOW);
        colorRotationList.add(AQUA);
        colorRotationList.add(GRAY);
        colorRotationList.add(MAGENTA);
        colorRotationList.add(PINK);
        colorRotationList.add(GOLD);
        colorRotationList.add(ROSE);
    }

    public static List<Color> getColorRotationList() {
        return colorRotationList;
    }

    public static Color getColorByName(String name) {
        Matcher m = HEX_PATTERN.matcher(name);

        if (m.matches()) {
            int r = Integer.parseInt(name.substring(0, 2), 16);
            int g = Integer.parseInt(name.substring(2, 4), 16);
            int b = Integer.parseInt(name.substring(4, 6), 16);
            int a = name.length() > 6 ? Integer.parseInt(name.substring(6, 8), 16) : 255;
                return new Color(r, g, b, a);
        } else {
            return colorMap.get(name != null ? name.toLowerCase() : null);
        }
    }

}
