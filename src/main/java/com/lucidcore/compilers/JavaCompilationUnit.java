package com.lucidcore.compilers;

import com.lucidcore.ICodeCompilationUnit;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JavaCompilationUnit implements ICodeCompilationUnit {
    // expect a main.java file that should contain the main method
    public String compile(File codeDirRoot) {
        String command = getCodeCompilationCommand(codeDirRoot);

        return executeCommand(command);
    }

    private String executeCommand(String command) {
        try {
            Runtime currRuntime = Runtime.getRuntime();
            Process p = currRuntime.exec(command);

            // wait until process ends TODO: add error handling and timeout
            while (p.isAlive()) {};

            if (p.exitValue() != 0) throw new Exception("Failed comiplation");

            return readProcessOutput(p.getInputStream());
        } catch (IOException e) {
            System.err.println(String.format("Error executing command: %s\nTrace: %s", command, e.getStackTrace()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private String readProcessOutput(InputStream stream) {
        StringBuilder output = new StringBuilder();
        byte[] buffer = new byte[1024];

        try {
            while (stream.read(buffer, 0, buffer.length) > 0) {
                output.append(buffer);
            }
        } catch (IOException e) {
            System.err.println(String.format("Error reading compilation output: %s\nTrace: %s", e.getMessage(), e.getStackTrace()));
        }

        return output.toString();
    }

    // Ex: javac -cp "dep1.jar;dep2.jar;dep3.jar" source
    private String getCodeCompilationCommand(File root) {
        StringBuilder builder = new StringBuilder("javac");

        String[] dependencies = getDependencies(root);
        // String[] files = getJavaFiles(root);

        builder.append(" -cp ");
        String depFiles = Arrays.stream(dependencies).collect(Collectors.joining(";"));
        builder.append(String.join(" \"%s\" ", depFiles));
        builder.append("main.java");

        return builder.toString();
    }

    private String[] getJavaFiles(File root) {
        // get all java files from the lib directory
        String[] javaFiles = Arrays.stream(root.listFiles())
                .filter(file -> !file.isDirectory() && file.getName().endsWith(".java"))
                .map(f -> f.getName())
                .toArray(String[]::new);

        return javaFiles;
    }

    private String[] getDependencies(File root) {
        // get all jar files from the lib directory
        String[] jarFiles = Arrays.stream(root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".jar") && file.getParent().equals("libs");
            }
        })).map(f -> f.getName()).toArray(String[]::new);

        return jarFiles;
    }
}
