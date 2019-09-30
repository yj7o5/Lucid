package com.core.compilers;

/*
This is just a work in progress, probably not functional yet.
-Quentin
 */

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JavaExecutionUnit
{
    private File workingDirectory;

    public JavaExecutionUnit(File directory)
    {
        workingDirectory = directory;
    }

    public String execute() throws Exception
    {
        if(workingDirectory == null || !workingDirectory.exists())
        {
            throw new IllegalArgumentException(String.format("Invalid project directory provided for compilation: %s", JavaCompilationUnit.class.getName()));
        }

        //As long as all the files needed are compiled, the only one you actually have to execute it Main.java
        String command = "java -cp " + workingDirectory.getPath() + " Main";

        return executeCommand(command);
    }

    //Not sure if the toString thing does what I think it does or not...
    public String executeCommand(String command) throws Exception
    {
        Process proc = Runtime.getRuntime().exec(command);

        String result = proc.getInputStream().toString();

        return result;
    }
}
