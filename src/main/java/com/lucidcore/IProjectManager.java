package com.lucidcore;

import java.io.IOException;

public interface IProjectManager {
    void createProject(String name, String locationDir) throws IOException;
    void removeProject(String locationDir) throws IOException;
    void openProject();
    void saveProject();
}
