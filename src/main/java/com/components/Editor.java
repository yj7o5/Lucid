package com.components;

import com.Lucid;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Editor implements Disposable {
    private JTextPane editorPane;
    private String fileName;
    private File file;
    private Disposable fileChangesSubscription;
    private EditorDocumentListener edl;
    private boolean isDisposed = false;

    // Indicates tells whether modifications to current document hasn't been saved yet
    private BehaviorSubject<Boolean> prestine$ = BehaviorSubject.create();

    public final static Pattern tokenPattern = Pattern.compile("(([\"'])(?:(?=(\\\\?))\\3.)*?\\2)|(if)|(else)|(for)|(while)|(\\+)|(\\-)|(/)|(\\|\\|)|(&&)");

    public Editor(File selectedFile, JTextPane jEditorPane) throws IOException {
        file = selectedFile;
        fileName = file.getName();
        editorPane = jEditorPane;

        String fileContent = Files.readAllLines(Paths.get(selectedFile.getAbsolutePath())).stream().collect(Collectors.joining("\n"));
        editorPane.setText(fileContent);

        Lucid.statsFacade.updateStats(fileContent);

        edl = new EditorDocumentListener();
        editorPane.getDocument().addDocumentListener(edl);

        fileChangesSubscription = prestine$
            .distinctUntilChanged()
            .subscribe();

        setTabs(4);
    }

    public String getFileName() {
        return fileName;
    }

    // adapted from: https://stackoverflow.com/questions/33544621/java-setting-indent-size-on-jtextpane/33557782#33557782
    public void setTabs(int charactersPerTab)
    {
        FontMetrics fm = editorPane.getFontMetrics( editorPane.getFont() );
        int charWidth = fm.charWidth( ' ' );
        int tabWidth = charWidth * charactersPerTab;

        TabStop[] tabs = new TabStop[5];

        for (int j = 0; j < tabs.length; j++)
        {
            int tab = j + 1;
            tabs[j] = new TabStop( tab * tabWidth );
        }

        TabSet tabSet = new TabSet(tabs);
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setTabSet(attributes, tabSet);
        int length = editorPane.getDocument().getLength();
        editorPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
    }

    public void saveChanges() throws IOException  {
        // indicate editor file is not longer dirty
        prestine$.onNext(false);

        String content = editorPane.getText();
        byte[] bytes = content.getBytes();

        Path path = Paths.get(file.getAbsolutePath());
        Files.write(path, bytes);
    }

    @Override
    public void dispose() {
        if (!isDisposed())
        {
            fileChangesSubscription.dispose();
            edl.unsubscribeSubject();
        }
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    private class EditorDocumentListener implements DocumentListener {
        final PublishSubject<DocumentEvent> documentEventSubject = PublishSubject.create();

        Disposable subscription;

        public EditorDocumentListener() {
            subscription = documentEventSubject
                    .debounce(250,  TimeUnit.MILLISECONDS)
                    .doOnEach(c -> prestine$.onNext(true))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new EditorStyleConsumer());
        }

        public void unsubscribeSubject() {
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) { documentEventSubject.onNext(documentEvent); }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) { documentEventSubject.onNext(documentEvent); }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) { documentEventSubject.onNext(documentEvent); }
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
            accept(null);
        }

        private void reset() {
            int length = editorPane.getDocument().getLength();
            highlight(Keywords.Default, 0, length, false);
        }

        @Override
        public void accept(DocumentEvent documentEvent) {
            reset();

            String updatedText = editorPane.getText();

            updateStats(updatedText);

            Matcher matcher = tokenPattern.matcher(updatedText);

            while (matcher.find()) {
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

        private void updateStats(String code) {
            SwingUtilities.invokeLater(() -> Lucid.statsFacade.updateStats(code));
        }
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
