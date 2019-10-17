package com.components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TerminalPane {

    private static JTextArea textArea;

    public final static String INPUT_COMMAND = "input_command";

    public static void Write(String text) {
        textArea.append(text);
    }

    public static void Clear() {
        textArea.setText("");
    }

    public static List<ActionListener> listeners = new ArrayList<>();

    public TerminalPane(JTextArea editor) {
        textArea = editor;

        textArea.setEditable(true);
        ((AbstractDocument)textArea.getDocument()).setDocumentFilter(new DocFilter());

        disableUpDownArrowKeys(textArea.getInputMap());
    }

    private void notifyListeners(String input) {
        ActionEvent ev = new ActionEvent(input, 0, INPUT_COMMAND);

        for (ActionListener l :
                listeners) {
            l.actionPerformed(ev);
        }
    }

    private void disableUpDownArrowKeys(InputMap map) {
        String[] keys = new String[]{"UP", "DOWN"};

        for (String k :
                keys) {
            map.put(KeyStroke.getKeyStroke(k), "non");
        }
    }

    private class DocFilter extends DocumentFilter {
        public final static String NEW_LINE = "\n";

        public StringBuffer feed = new StringBuffer();

        @Override
        public void insertString(FilterBypass fb, int offset, String content, AttributeSet attr) throws BadLocationException {
            super.insertString(fb, offset, content, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            super.replace(fb, offset, length, text, attrs);
            feed.append(text);

            if (isNewlineFeed(offset)) {
                // notify observers
                notifyListeners(feed.substring(0, feed.length()));
                feed.delete(0, feed.length());
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (isNewlineFeed(offset)) return;

            super.remove(fb, offset, length);
            System.out.println(String.format("offset: %s length: %s ", offset, length));
        }

        private boolean isNewlineFeed(int offset) throws BadLocationException {
            Document doc = textArea.getDocument();
            String c = doc.getText(offset, 1);

            return c.equals(NEW_LINE);
        }
    }
}
