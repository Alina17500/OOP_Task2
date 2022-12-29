package ru.vsu.cs.volobueva.engine;

import java.awt.Color;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

public final class GameParameters {

    public enum End {
        NORMAL, FISH
    }

    public enum Corner {
        C0(0), C90(90), C180(180), C270(270);

        @Getter
        private final int corner;

        Corner(int corner) {
            this.corner = corner;
        }
    }

    public static final Color color = new Color(217, 165, 185, 255);

    public static final byte MAXDOTS = 6;
    public static final byte MAXBONES = MAXDOTS + 1;
    public static final int TOTALBONES = (MAXDOTS + 1) * (MAXDOTS + 2) / 2;

    public static final int SIZEX = 1300; // размеры окна
    public static final int SIZEY = 720;

    public static final int BONEX = 46; // размеры камней
    public static final int BONEY = 20;
    public static final int MOVEJBX = 1060;
    public static final int MOVEJBY = 40;

    public static final int OFFSET = 4;
    public static final int PLAYERSHIFT = 10;
    public static final int SHIFT = 5;
    public static final int SPACELIMIT = 150;

    public static final boolean TORIGHT = true;
    public static final boolean TOLEFT = !TORIGHT;

    public static final boolean FRAME = true;
    public static final boolean NOFRAME = !FRAME;

    public static final int XSHIFT = 25; // Начальное смещение домино на панели
    public static final int YSHIFT = 25;

    static final boolean SHOW = true;
    static final boolean HIDE = !SHOW;
    static final boolean DUPLET = true;
    static final boolean NOTDUPLET = !DUPLET;
    public static final boolean HUMAN = true;
    public static final boolean ROBOT = !HUMAN;
    public static final boolean SELECTED = true;
    public static final boolean UNSELECTED = !SELECTED;

    static final Map<String, String> ENEMY = Stream
            .of(new SimpleEntry<>("Windows", "windows32.png"),
                    new SimpleEntry<>("Linux", "linux32.png"))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
}
