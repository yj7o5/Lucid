package com.core.compilers;

import com.components.TerminalPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaCodeExecutionUnit extends Thread implements ICodeExecutionUnit {
    File targetDirectory;
    String targetClass;

    ProcessBuilder pb;

    public final static String INPUT_COMMAND = "input_command";

    public JavaCodeExecutionUnit(File dir, String target) {
        targetDirectory = dir;
        targetClass = target;
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

            // setup streams
            ArrayList<InputStream> streams = new ArrayList<>();
            streams.add(process.getInputStream());
            streams.add(process.getErrorStream());

            RedirectStreams rs = new RedirectStreams(streams, ev -> {
                TerminalPane.Write((String)ev.getSource());
            });

            addOutputListener(process.getOutputStream(), TerminalPane.listeners);

            // run reader/writer routines
            rs.run();

            // wait for program to finish
            int returnCode = process.waitFor();

            // clear off listeners
            TerminalPane.listeners.clear();

            // ending note with exit code
            TerminalPane.Write("Program finished with exit code " + returnCode);
        }
        catch (Exception ex) {
            TerminalPane.Write(ex.getMessage());
        }
    }

    private void addOutputListener(OutputStream stream, List<ActionListener> listeners) {
        listeners.add((l) -> {
            byte[] data = l.getSource().toString().getBytes();

            try {
                stream.write(data, 0, data.length);
                stream.flush();
            }
            catch (IOException ex) {
                // swallow exception for now
            }
        });
    }

    private class RedirectStreams {
        ActionListener writeTo;
        ArrayList<InputStream> streams;
        ActionEvent av;

        public RedirectStreams(ArrayList<InputStream> _streams, ActionListener _writeTo) {
            if (_streams == null || _streams.isEmpty()) {
                throw new IllegalArgumentException("empty or null streams not allowed");
            }

            streams = _streams;
            writeTo = _writeTo;
            av = new ActionEvent("", 0, "input");
        }

        public void run() {
            streams.forEach(s -> {
                Thread th = new Thread(() -> {
                    int read;
                    byte[] buffer = new byte[1024];

                    try {
                        while((read = s.read(buffer, 0, buffer.length)) != -1) {
                            if (read < buffer.length) {
                                buffer = Arrays.copyOf(buffer, read);
                            }
                            av.setSource(new String(buffer, StandardCharsets.UTF_8));
                            writeTo.actionPerformed(av);
                        }
                    }
                    catch (IOException e) {
                        // swallow exception for now
                    }
                });
                th.start();
            });
        }
    }
}
