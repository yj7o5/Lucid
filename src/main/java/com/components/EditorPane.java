package com.components;

import com.external.CloseTabIcon;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditorPane implements MouseListener {
    private ArrayList<Editor> editors;
    private JPanel tabContainers;

    private JTabbedPane tabs;

    public EditorPane(JPanel frame, JTabbedPane jTabs) {
        editors = new ArrayList<Editor>();
        tabs = jTabs;
        tabContainers = frame;

        tabs.addMouseListener(this);
    }

    public void openEditor(String name, File file) throws IOException  {
        if (editors.stream().anyMatch(e -> e.getFileName().equalsIgnoreCase(name))) {
            return;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 0));

        JTextPane editorPane = new JTextPane();
        JScrollPane sp = new JScrollPane(editorPane);

        editorPane.setBackground(Color.lightGray);
        Font font = new Font("SANS_Serif", Font.PLAIN, 12);
        editorPane.setFont(font);

        panel.add(sp, BorderLayout.CENTER);

        tabs.addTab(name, new CloseTabIcon(null), panel);

        Editor editor = new Editor(file, editorPane);

        //editorPane.setBackground(Color.lightGray);

        editors.add(editor);
    }

    public Editor getActiveEditor() {
        if (editors.isEmpty()) return null;

        return editors.get(tabs.getSelectedIndex());
    }

    public void mouseClicked(MouseEvent e) {
        handleTabClose(e);
        handleTabSwitch(e);
    }

    public boolean saveChanges() {
        boolean successful = false;
        for (Editor editor:
             editors) {
            try {
                editor.saveChanges();
                successful = true;
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(tabContainers, "Failed saving project" );
            }
        }
        return successful;
    }

    public void closeEditor(String filename) {
        Editor editor = getActiveEditor();
        if (editor != null && editor.getFileName().equals(filename)) {
            removeEditorAtIndex(tabs.getSelectedIndex());
        }
    }

    private void handleTabClose(MouseEvent e) {
        int tabIndex = tabs.getUI().tabForCoordinate(tabs, e.getX(), e.getY());
        if (tabIndex < 0) return;

        Rectangle r = ((CloseTabIcon)tabs.getIconAt(tabIndex)).getBounds();

        if (r.contains(e.getX(), e.getY())) {
            removeEditorAtIndex(tabIndex);
        }
    }

    private void removeEditorAtIndex(int tabIndex) {
        String fileName = tabs.getTitleAt(tabIndex);
        editors.removeIf(f -> {
            boolean remove = f.getFileName().equalsIgnoreCase(fileName);
            if (remove) f.dispose();

            return remove;
        });

        tabs.removeTabAt(tabIndex);
    }

    private void handleTabSwitch(MouseEvent e) {

    }

    public void mousePressed(MouseEvent mouseEvent) { }
    public void mouseReleased(MouseEvent mouseEvent) { }
    public void mouseEntered(MouseEvent mouseEvent) { }
    public void mouseExited(MouseEvent mouseEvent) { }
}
