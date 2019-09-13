package com.lucidcore;

import com.utilities.Guard;

import javax.management.InvalidApplicationException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class ProjectManager implements IProjectManager {
    // Create a project folder at this location directory which contains main.java file
    // along with lib folder for any dependencies
    public File createProject(String name, String locationDir) throws IOException {
        Guard.newGuard().nullCheck(name, "Invalid project name: " + name);

        Path path = Paths.get(locationDir+"/"+name);

        if(Files.exists(path)) throw new IllegalArgumentException("Project already exists");

        boolean created;

        // Init directory
        File root = new File(path.toUri());
        created = root.mkdir();
        if(!created) throw new FileSystemException("Unable to create project");

        // Add main.java file
        File mainFile = new File(path.toAbsolutePath() + "/main.java");
        mainFile.createNewFile();

        // Create the libs folder
        created = new File(path.toAbsolutePath() + "/libs").mkdir();

        if (!created) throw new FileSystemException("Unable to create libs directory");

        return root;
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

    public File openProject(Component frame) {
        JFileChooser chooser = new JFileChooser("f:");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return null;

        return chooser.getSelectedFile();
    }

    public void saveProject() {

    }
}
