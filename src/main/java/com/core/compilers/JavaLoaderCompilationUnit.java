package com.core.compilers;
import com.components.TerminalPane;
import com.utilities.Helper;

import java.io.*;

/*
 Note: certain features such as compile, loadClass adapted from online ClassLoader pdf on shared drive
 */
public class JavaLoaderCompilationUnit extends ClassLoader implements ICodeCompilationUnit {
    private File workingDirectory;

    public JavaLoaderCompilationUnit(File root) {
        workingDirectory = root;
    }

    @Override
    public CompilationResult compile() {
        try {
            this.loadClass("Main");
        } catch (Exception e) {
            TerminalPane.WriteLn("Build Failure: " + e.getMessage());
            return new CompilationResult("Failed", false);
        }

        TerminalPane.WriteLn("Build Successful");
        return new CompilationResult("Success", true);
    }

    private boolean compile(File file) {
        return compile(file.getAbsolutePath());
    }

    private boolean compile(String javaFile) {
        TerminalPane.WriteLn("Compiling: " + javaFile);

        // Start up the compiler
        Process p = null;
        try {
            String pathToLib = workingDirectory.getAbsolutePath() + "/libs";
            String[] commands = new String[]{"javac", "--release", "11", /*"-cp", pathToLib + "/*",*/ javaFile};
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(workingDirectory);
            p = pb.command(commands).start();
            p.waitFor();
        }
        catch (Exception e) {
            TerminalPane.WriteLn("Failed Compiling: " + javaFile + "\n" + "Error: " + e.getMessage());
            return false;
        }

        boolean exit = p.exitValue() == 0;

        if (exit) {
            TerminalPane.WriteLn("Compiled: " + javaFile);
        }
        else {
            String output = Helper.readProcessOutput(p.getErrorStream());
            TerminalPane.WriteLn(output);
        }

        return exit;
    }

    // automatically compile source as necessary when looking class files
    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        Class klass = null;

        klass = findLoadedClass(name);
        TerminalPane.WriteLn("Loading Class: " + name);

        String fileStub = name.replace('.', '/');

        String javaFilename = fileStub+".java";
        String classFilename = fileStub+".class";

        String rootPath = workingDirectory.getAbsolutePath();
        File javaFile = new File(rootPath + "/" + javaFilename);
        File classFile = new File(rootPath + "/" + classFilename);

        if (javaFile.exists() &&
                (!classFile.exists() || javaFile.lastModified() > classFile.lastModified())) {
            try {
                if (!compile(javaFile) || !classFile.exists()) {
                    throw new ClassNotFoundException(javaFilename);
                }
            } catch (Exception e) {
                throw new ClassNotFoundException(e.toString());
            }
        }

        try {
            byte raw[] = getBytes(classFile.getAbsolutePath());

            klass = defineClass(name, raw, 0, raw.length);
        } catch (IOException ie) {

        }

        // Maybe the class is in standard system library
        if (klass == null) {
            klass = findSystemClass(name);
        }

        if (resolve && klass != null) {
            resolveClass(klass);
        }

        if (klass == null) {
            throw new ClassNotFoundException(name);
        }

        return klass;
    }

    // Given a filename, read the entirety of that file from disk
    // and return it as a byte array.
    private byte[] getBytes(String filename) throws IOException {
        // Find out the length of the file
        File file = new File(filename);
        long len = file.length();

        byte[] raw = new byte[(int) len];

        FileInputStream fs = new FileInputStream(file);

        // Read all of it into the array
        int r = fs.read(raw);
        if (r != len) {
            throw new IOException("Can't read all");
        }
        fs.close();
        return raw;
    }
}
