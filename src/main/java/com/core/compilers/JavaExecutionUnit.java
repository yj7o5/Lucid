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
    private long execTime = 0;

    public JavaExecutionUnit(File directory)
    {
        workingDirectory = directory;
    }

    //This should return the execution time of the Main.java if it is ran AFTER running the execute() method.
    //Otherwise it will just return 0.
    public long getExecTime()
    {
        return execTime;
    }

    //The project says that there will be a Main.java and I've found that as long as all the dependecies are compiled, the only
    //one that actually needs to be executed is the Main. Which is why I just put the command together in this method instead
    //of creating a method for that portion
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

    public String executeCommand(String command) throws Exception
    {
        StringBuilder result = new StringBuilder();

        try {
            Process proc = Runtime.getRuntime().exec(command);

//        String result = proc.getInputStream().toString()

            long start = System.nanoTime();
            proc.waitFor();
            long end = System.nanoTime();

            execTime = (end-start) / 1_000_000; //gives run time of proc in milliseconds.

            if (proc.waitFor() != 0)
            {
                result.append(String.format("Process Error - Exit Code : %s", proc.exitValue()));
                //result.append(readProcessOutput(proc.getErrorStream()));

                return result.toString();
            }

            result.append(proc.getInputStream());
        }
        catch(IOException e)
        {
            result.append(String.format("Error executing command: %s\nTrace: %s", command, e.getStackTrace()));
        }
        catch(Exception e)
        {
            result.append(String.format("Exception: %s\nStack: %s", e.getMessage(), e.getStackTrace()));
        }

        return result.toString();
    }
}
