package com.core;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class StatsFacade {
    private JTextField textField;

    public static List<String> keywords =
            Arrays.asList("abstract","assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double",
                    "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
                    "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
                    "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null");


    public void setField(JTextField field) {
        textField = field;
    }

    public synchronized void updateStats(String code) {
        String stats = String.format("Keywords: %s Lines: %s", getKeywordsCount(code), getNumberOfLines(code));
        textField.setText(stats);
    }

    // such bad code, this is terrible performance ... I am sorry :( -- will optimize
    public int getNumberOfLines(String code) {
        return code.split("\n").length;
    }

    // terrible performance yet another time ... :/ -- will optimize later
    public long getKeywordsCount(String code) {
        return Arrays.stream(code.split(" "))
                .filter(kw -> keywords.contains(kw))
                .count();
    }
}
