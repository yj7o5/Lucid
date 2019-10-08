package com.core.compilers;

public class CompilationResult {
    public String Output;
    public boolean Successful;

    public CompilationResult(String result, boolean success) {
        Output = result;
        Successful = success;
    }
}
