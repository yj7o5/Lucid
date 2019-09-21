package com.components;

import javax.swing.*;

public class Editor {
    private JEditorPane editorPane;
    private String fileName;
    // tells whether modifications to current document hasn't been saved yet
    private boolean isPrestine;

    public Editor(String name, JEditorPane jEditorPane, String content) {
        fileName = name;
        editorPane = jEditorPane;
        editorPane.setText(content);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean areChangesSaved() { return !isPrestine; }
}
