package com.core.compilers;

/*
This is just a work in progress, probably not functional yet.
-Quentin
 */

import com.components.TerminalPane;

import javax.print.DocFlavor;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class JavaCodeExecutionUnit extends Thread implements ICodeExecutionUnit {
    File targetDirectory;
    String targetClass;

    ProcessBuilder pb;

    public JavaCodeExecutionUnit(File dir, String target) {
        targetDirectory = dir;
        targetClass = target;

        TerminalPane.Write("Init Java Code Execution");
    }

    @Override
    public void run()
    {
        pb = new ProcessBuilder(new String[]{"java", "-cp", targetDirectory.getAbsolutePath(), "Main"});
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        execute();
    }

    public void execute() {
        TerminalPane.Clear();

        try {
            Process process = pb.start();

            ProgramReader reader = new ProgramReader(process.getInputStream(), (ev) -> {
               TerminalPane.Write((String)ev.getSource());
            }, (ev) -> {
                // Handle error
                TerminalPane.Write(((Exception)ev.getSource()).getMessage());
            });

            ProgramWriter writer = new ProgramWriter(process.getOutputStream(), (ArrayList<ActionListener>) TerminalPane.listeners);

            reader.start();
            writer.start();

            int returnCode = process.waitFor();

            TerminalPane.Write("Program finished with exit code " + returnCode);
        }
        catch (Exception ex) {
            TerminalPane.Write(ex.getMessage());
        }
    }

    private class ProgramReader extends Thread {
        private InputStream is;
        private ActionListener onRead;
        private ActionListener onError;
        private ActionEvent ev;

        public ProgramReader(InputStream input, ActionListener onSuccess, ActionListener onErr) {
            is = input;
            onRead = onSuccess;
            onError = onErr;
            ev = new ActionEvent("", 0, "input");
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                while(true) {
                    int read = is.read(buffer, 0, buffer.length);

                    if (read == -1) break;

                    if (read < buffer.length)
                        buffer = Arrays.copyOf(buffer, read);

                    ev.setSource(new String(buffer, StandardCharsets.UTF_8));
                    onRead.actionPerformed(ev);
                }
            }
            catch (IOException e) {
                ev.setSource(e);
                onError.actionPerformed(ev);
            }
        }
    }

    private class ProgramWriter extends Thread {
        private BufferedWriter writer;

        public ProgramWriter(OutputStream out, ArrayList<ActionListener> listeners) {
            writer = new BufferedWriter(new OutputStreamWriter(out));
            listeners.add((ev) -> {
                try {
                    writer.write((String)ev.getSource());
                    writer.flush();
                }
                catch (Exception e) {
                    System.err.print(e.getMessage());
                }
            });
        }
    }
}
