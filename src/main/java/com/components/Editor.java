package com.components;

import javax.swing.*;

public class Editor {
    private JEditorPane editorPane;
    private String fileName;

    public Editor(String name, JEditorPane jEditorPane, String content) {
        fileName = name;
        editorPane = jEditorPane;
        editorPane.setText(content);
    }

    public String getFileName() {
        return fileName;
    }
}
