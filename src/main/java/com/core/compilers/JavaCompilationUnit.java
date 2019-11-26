package com.core.compilers;

import com.utilities.Helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class JavaCompilationUnit implements ICodeCompilationUnit {
    private File workingDirectory;

    public JavaCompilationUnit(File directory) {
        workingDirectory = directory;
    }

    // expect a main.java file that should contain the main method
    public CompilationResult compile() {
        if(workingDirectory == null || !workingDirectory.exists()) {
            throw new IllegalArgumentException(String.format("Invalid project directory provided for compilation: %s", JavaCompilationUnit.class.getName()));
        }

        String command = getCodeExecutionCommand();

        return executeCommand(command);
    }

    private CompilationResult executeCommand(String command) {
        StringBuilder executionResult = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(workingDirectory);
            pb.command(command.split(" "));

            Process p = pb.start();

            // wait for program to finish
            p.waitFor();

            // anything other than 0 means error of some kind
            if (p.getErrorStream().read() != -1) {
                executionResult.append(Helper.readProcessOutput(p.getErrorStream()));

                return new CompilationResult(executionResult.toString(), false);
            }

            String result = executionResult.toString();
            return new CompilationResult(result.trim().isEmpty() ? "Compiled Successfully" : result, p.exitValue() == 0);
        } catch (IOException e) {
            executionResult.append(String.format("Error executing command: %s\nDetail: %s", command, e.getMessage()));
        } catch (Exception e) {
            executionResult.append(String.format("Exception: %s\nStack: %s", e.getMessage(), e.getStackTrace()));
        }

        return new CompilationResult("No output", true);
    }

    // Ex: javac -cp "dep1.jar;dep2.jar;dep3.jar" source
    private String getCodeExecutionCommand() {
        return Arrays.stream(new String[]{
            "-cp",
            "libs/*:",
            "Main"
        }).collect(Collectors.joining(" "));
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
