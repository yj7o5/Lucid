package com;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.lucidcore.*;

public class TestApp extends JFrame {

    private JButton saveButton;
    private JPanel panel1;


    public TestApp() {
        final IProjectManager projMgr = new ProjectManager();
        final JFrame frame = this;

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String projectName = JOptionPane.showInputDialog("Provide Project Name: ");

                JFileChooser chooser = new JFileChooser("f:");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        projMgr.createProject(projectName, chooser.getCurrentDirectory().getAbsolutePath());
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(frame, "Error creating project: " + e.getMessage());
                    }
                }
                else {
                    System.out.println("No Selection ");
                }
            }
        });

        this.setTitle("Test App");
        this.add(saveButton);

        this.setSize(500, 500);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new TestApp();
    }
}
