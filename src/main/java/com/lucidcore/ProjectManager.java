package com.lucidcore;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ProjectManager implements IProjectManager {
    // Create a project folder at this location directory which contains main.java file
    // along with lib folder for any dependencies
    public void createProject(String name, String locationDir) throws IOException {
        Guard.newGuard().nullCheck(name, "Invalid project name: " + name);

        Path path = Paths.get(locationDir+"/"+name);

        if(Files.exists(path)) throw new IllegalArgumentException("Project already exists");

        // Init directory
        new File(path.toUri()).mkdir();

        // Add main.java file
        File mainFile = new File(path.toAbsolutePath() + "/main.java");
        mainFile.createNewFile();

        // Create the libs folder
        new File(path.toAbsolutePath() + "/libs").mkdir();
    }

    public void removeProject(String locationDir) throws IOException {
        // Implementation copied directly from: https://stackoverflow.com/a/36330841
        Files.walkFileTree(Paths.get(locationDir), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void openProject() {

    }

    public void saveProject() {

    }
}
