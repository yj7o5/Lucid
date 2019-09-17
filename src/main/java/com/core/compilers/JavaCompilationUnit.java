package com.core.compilers;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JavaCompilationUnit implements ICodeCompilationUnit {
    private File workingDirectory;

    public JavaCompilationUnit(File directory) {
        workingDirectory = directory;
    }

    // expect a main.java file that should contain the main method
    public String compile() {
        if(workingDirectory == null || !workingDirectory.exists()) {
            throw new IllegalArgumentException(String.format("Invalid project directory provided for compilation: %s", JavaCompilationUnit.class.getName()));
        }

        String command = getCodeCompilationCommand(workingDirectory);

        return executeCommand(command);
    }

    private String executeCommand(String command) {
        StringBuilder executionResult = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(workingDirectory);

            pb.command(command);
            Process p = pb.start();

            InputStream is = p.getInputStream();
            InputStream es = p.getErrorStream();

            // wait for program to finish
            p.waitFor();

            // anything other than 0 means error of some kind
            if (p.exitValue() != 0) {
                executionResult.append(String.format("Process Error - Exit Code : %s", p.exitValue()));
                executionResult.append(readProcessOutput(p.getErrorStream()));

                return executionResult.toString();
            }

            executionResult.append(p.getInputStream());
        } catch (IOException e) {
            executionResult.append(String.format("Error executing command: %s\nTrace: %s", command, e.getStackTrace()));
        } catch (Exception e) {
            executionResult.append(String.format("Exception: %s\nStack: %s", e.getMessage(), e.getStackTrace()));
        }

        return executionResult.toString();
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
        String depFiles = String.join(";", dependencies);
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
        String[] jarFiles = Arrays.stream(root.listFiles())
                .filter(file ->
                    !file.isDirectory() &&
                     file.getName().endsWith(".jar") &&
                     file.getParent().equalsIgnoreCase("libs")
                )
                .map(file -> file.getName())
                .toArray(String[]::new);

        return jarFiles;
    }
}
