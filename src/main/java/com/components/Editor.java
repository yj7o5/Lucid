package com.components;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Editor {
    private JTextPane editorPane;
    private String fileName;

    // Indicates tells whether modifications to current document hasn't been saved yet
    private boolean isPrestine;

    public final static Pattern tokenPattern = Pattern.compile("(([\"'])(?:(?=(\\\\?))\\3.)*?\\2)|(if)|(else)|(for)|(while)|(\\+)|(\\-)|(/)|(\\|\\|)");

    public Editor(String name, JTextPane jEditorPane, String content) {
        fileName = name;
        editorPane = jEditorPane;
        editorPane.setText(content);

        editorPane.getDocument().addDocumentListener(new EditorDocumentListener());
    }

    public String getFileName() {
        return fileName;
    }

    public boolean areChangesSaved() { return !isPrestine; }

    private class EditorDocumentListener implements DocumentListener {
        final PublishSubject<DocumentEvent> documentEventSubject = PublishSubject.create();

        public EditorDocumentListener() {
            documentEventSubject
                    .debounce(500,  TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new EditorStyleConsumer());
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) { documentEventSubject.onNext(documentEvent); }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) { documentEventSubject.onNext(documentEvent); }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) { }
    }

    private class EditorStyleConsumer implements Consumer<DocumentEvent> {
        public Map<Keywords, Color> keywordColorMap = new HashMap<Keywords, Color>();

        public void setStyledTextSet(Keywords keyword, int startIndex, int endIndex) {
            MutableAttributeSet set = new SimpleAttributeSet();

            StyleConstants.setForeground(set, keywordColorMap.get(keyword));
            editorPane.getStyledDocument().setCharacterAttributes(startIndex, endIndex, set, true);
        }

        private void matchAndApplyStyleIfGroup(int group, Matcher matcher, Keywords style) {
            if (matcher.group(group) != null) {
                setStyledTextSet(style, matcher.start(group), matcher.end(group));
            }
        }

        public EditorStyleConsumer() {
            keywordColorMap.put(Keywords.If, Color.BLUE);
            keywordColorMap.put(Keywords.Else, Color.BLUE);
            keywordColorMap.put(Keywords.For, Color.BLUE);
            keywordColorMap.put(Keywords.While, Color.BLUE);
            keywordColorMap.put(Keywords.Addition, Color.RED);
            keywordColorMap.put(Keywords.Subtraction, Color.RED);
            keywordColorMap.put(Keywords.Division, Color.RED);
            keywordColorMap.put(Keywords.LogicalOr, Color.RED);
            keywordColorMap.put(Keywords.StringText, Color.GREEN);
            keywordColorMap.put(Keywords.Default, Color.BLACK);
        }

        @Override
        public void accept(DocumentEvent documentEvent) throws Exception {
            try {
                Matcher matcher = tokenPattern.matcher(documentEvent.getDocument().getText(0, documentEvent.getDocument().getLength()));

                while (matcher.find()) {
                    System.out.println(String.format("group: %s", matcher.group(matcher.group())));

                    matchAndApplyStyleIfGroup(4, matcher, Keywords.StringText);
                    matchAndApplyStyleIfGroup(5, matcher, Keywords.If);
                    matchAndApplyStyleIfGroup(6, matcher, Keywords.Else);
                    matchAndApplyStyleIfGroup(7, matcher, Keywords.For);
                    matchAndApplyStyleIfGroup(8, matcher, Keywords.While);
                    matchAndApplyStyleIfGroup(9, matcher, Keywords.Addition);
                    matchAndApplyStyleIfGroup(10, matcher, Keywords.Subtraction);
                    matchAndApplyStyleIfGroup(11, matcher, Keywords.Division);
                    matchAndApplyStyleIfGroup(12, matcher, Keywords.LogicalOr);
                }
            }
            catch (BadLocationException e) { /* remain silent */}
        }
    }

    private class TokenSetStyle {
        public String color;
        public ArrayList<Keywords> keywords;
    }

    private class Token {
        public int offset;
        public int length;
        public Keywords keyword;
    }

    private enum Keywords {
        If,
        Else,
        For,
        While,
        Addition,
        Subtraction,
        Division,
        LogicalOr,
        LogicalAnd,
        StringText,
        Default
    }
}
