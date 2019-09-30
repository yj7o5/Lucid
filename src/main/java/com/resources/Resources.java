package com.resources;

import javax.swing.*;

public class Resources {
    public static ImageIcon getIcon(String path) {
        java.net.URL imgURL = Resources.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        return null;
    }
}
