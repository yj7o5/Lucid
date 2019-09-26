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

    public final static Pattern tokenPattern = Pattern.compile("(([\"'])(?:(?=(\\\\?))\\3.)*?\\2)|(if)|(else)|(for)|(while)|(\\+)|(\\-)|(/)|(\\|\\|)|(&&)");

    public Editor(String name, JTextPane jEditorPane, String content) {
        fileName = name;
        editorPane = jEditorPane;
        editorPane.setText(content);

        editorPane.getDocument().addDocumentListener(new EditorDocumentListener());
    }

    public String getFileName() {
        return fileName;
    }

    public boolean hasChanged() { return !isPrestine; }

    private class EditorDocumentListener implements DocumentListener {
        final PublishSubject<DocumentEvent> documentEventSubject = PublishSubject.create();

        public EditorDocumentListener() {
            documentEventSubject
                    .debounce(500,  TimeUnit.MILLISECONDS)
                    .doOnEach(c -> isPrestine = true)
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

        public void highlight(Keywords keyword, int startIndex, int endIndex, boolean b) {
            MutableAttributeSet set = new SimpleAttributeSet();

            StyleConstants.setForeground(set, keywordColorMap.get(keyword));
            editorPane.getStyledDocument().setCharacterAttributes(startIndex, endIndex, set, b);
        }

        private void matchAndApplyStyleIfGroup(int group, Matcher matcher, Keywords style) {
            if (matcher.group(group) != null) {
                highlight(style, matcher.start(group), matcher.end(group) - matcher.start(group), false);
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
            keywordColorMap.put(Keywords.LogicalAnd, Color.RED);
            keywordColorMap.put(Keywords.StringText, Color.GREEN);
            keywordColorMap.put(Keywords.Default, Color.BLACK);

            reset();
        }

        private void reset() {
            int length = editorPane.getDocument().getLength();
            highlight(Keywords.Default, 0, length, false);
        }

        @Override
        public void accept(DocumentEvent documentEvent) {
            reset();

            Matcher matcher = tokenPattern.matcher(editorPane.getText());

            while (matcher.find()) {
                System.out.println(String.format("group: %s start: %s end: %s", matcher.group(), matcher.start(2), matcher.end(2)));

                matchAndApplyStyleIfGroup(1, matcher, Keywords.StringText);
                matchAndApplyStyleIfGroup(4, matcher, Keywords.If);
                matchAndApplyStyleIfGroup(5, matcher, Keywords.Else);
                matchAndApplyStyleIfGroup(6, matcher, Keywords.For);
                matchAndApplyStyleIfGroup(7, matcher, Keywords.While);
                matchAndApplyStyleIfGroup(8, matcher, Keywords.Addition);
                matchAndApplyStyleIfGroup(9, matcher, Keywords.Subtraction);
                matchAndApplyStyleIfGroup(10, matcher, Keywords.Division);
                matchAndApplyStyleIfGroup(11, matcher, Keywords.LogicalOr);
                matchAndApplyStyleIfGroup(12, matcher, Keywords.LogicalAnd);
            }
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
