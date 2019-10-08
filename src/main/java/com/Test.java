package com;

import com.core.ICodeCompiler;
import com.core.compilers.ICodeCompilationUnit;
import com.core.compilers.JavaCompilationUnit;

import java.io.IOException;
import java.io.File;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException  {
        Scanner sc = new Scanner(System.in);

        File target = new File("/Users/yawarjamal/Desktop/Discovery");
        ICodeCompilationUnit compiler = new JavaCompilationUnit(target);

        System.out.println(compiler.compile().Output);
    }
}
