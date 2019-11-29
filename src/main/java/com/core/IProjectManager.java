package com.core;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public interface IProjectManager {
    File createProject(String name, String locationDir) throws IOException;
    void removeProject(String locationDir) throws IOException;
    File openProject(Component frame);
    void deleteFile(File directory, String name) throws IOException;
}
