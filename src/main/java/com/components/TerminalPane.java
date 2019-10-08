package com.components;

import javax.swing.*;
import java.util.Scanner;

public class TerminalPane {

    private static JTextArea textArea;

    public static Scanner ReadStream;

    public static void Write(String text) {
        textArea.append(text);
    }

    public static void Clear() {
        textArea.setText("");
    }

    public TerminalPane(JTextArea editor) {
        textArea = editor;
    }
}
