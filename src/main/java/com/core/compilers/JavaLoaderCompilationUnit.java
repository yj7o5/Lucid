package com.core.compilers;
import com.components.TerminalPane;

import java.io.*;
import java.lang.reflect.Method;

/*
 Note: certain features such as compile, loadClas adapted from online ClassLoader pdf on shared drive
 */
public class JavaLoaderCompilationUnit extends ClassLoader implements ICodeCompilationUnit {
    private File workingDirectory;

    public JavaLoaderCompilationUnit(File root) {
        workingDirectory = root;
    }

    @Override
    public CompilationResult compile() {
        boolean passed = false;

        try {
            Class klass = this.loadClass(workingDirectory.getAbsolutePath() + "/Main");
            Class mainArgType[] = { (new String[0]).getClass() };

            Method main = klass.getMethod("main", mainArgType);
            main.invoke(null, new Object[0]);

            passed = true;
        } catch (Exception e) { }

        return new CompilationResult("Success", passed);
    }

    private boolean compile(String javaFile) {
        TerminalPane.Write("Compiling " + javaFile);

        // Start up the compiler
        Process p = null;
        try {
            String[] commands = new String[]{"javac", javaFile};
            p = Runtime.getRuntime().exec(commands);
            p.waitFor();
        }
        catch (Exception e) {
            TerminalPane.Write("Failed compilation: " + e.getMessage());
        }

        int ret = p.exitValue();

        return ret == 0;
    }

    // automatically compile source as necessary when looking class files
    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        Class klass = null;

        klass = findLoadedClass(name);
        TerminalPane.Write("Loading Class: " + name);

        String fileStub = name; //name.replace('.', '/');

        String javaFilename = fileStub+".java";
        String classFilename = fileStub+".class";

        File javaFile = new File(javaFilename);
        File classFile = new File(classFilename);

        if (javaFile.exists() &&
                (!classFile.exists() || javaFile.lastModified() > classFile.lastModified())) {
            try {
                if (!compile(javaFilename) || !classFile.exists()) {
                    throw new ClassNotFoundException("Compile Failed: " + javaFilename);
                }
            } catch (Exception e) {
                throw new ClassNotFoundException(e.toString());
            }
        }

        try {
            byte raw[] = getBytes(classFilename);

            klass = defineClass("Main", raw, 0, raw.length);
        } catch (IOException ie) {
            System.err.println(ie);
        }

        if (klass == null) {
            String[] parts = classFilename.split("/");
            classFilename = parts[parts.length - 1];
            klass = findSystemClass(classFilename);
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
