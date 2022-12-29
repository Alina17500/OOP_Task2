package ru.vsu.cs.volobueva;

import java.awt.EventQueue;

import ru.vsu.cs.volobueva.engine.DominoGame;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> (new DominoGame()).play());
    }
}
