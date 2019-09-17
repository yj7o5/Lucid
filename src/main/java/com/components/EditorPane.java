package com.components;

import com.resources.Resources;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class EditorPane {
    private ArrayList<Editor> editors;
    private JPanel tabContainers;

    private JTabbedPane tabs;

    public EditorPane(JPanel frame, JTabbedPane jTabs) {
        editors = new ArrayList<Editor>();
        tabs = jTabs;
        tabContainers = frame;
    }

    public void openEditor(String name, String content) {
        if (editors.stream().anyMatch(e -> e.getFileName().equalsIgnoreCase(name))) {
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        JEditorPane editorPane = new JEditorPane();

        panel.add(editorPane, BorderLayout.CENTER);

        ImageIcon icon = Resources.getIcon("close.png");

        tabs.
        tabs.addTab(name, icon, panel);

        Editor editor = new Editor(name, editorPane, content);
        editors.add(editor);
    }

    private void addTab(String content) {

    }
}
