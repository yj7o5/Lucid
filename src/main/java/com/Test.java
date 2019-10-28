package com;

import com.core.compilers.ICodeExecutionUnit;
import com.core.compilers.JavaCodeExecutionUnit;

import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException  {
        Scanner sc = new Scanner(System.in);

        File target = new File("/Users/yawarjamal/Desktop/Discovery");
        // ICodeCompilationUnit compiler = new JavaCompilationUnit(target);

        ICodeExecutionUnit codeExecution = new JavaCodeExecutionUnit(target, "Main");

        codeExecution.execute();
        // System.out.println(compiler.compile().Output);
    }
}
