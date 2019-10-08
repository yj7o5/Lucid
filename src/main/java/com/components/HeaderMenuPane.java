package com.components;

import com.core.compilers.ICodeCompilationUnit;
import com.core.compilers.JavaCompilationUnit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class HeaderMenuPane {
    private JPopupMenu jfilePopupMenu;
    private JPopupMenu jbuildPopupMenu;

    private FileMenuHandler fileMenuHandler;
    private BuildMenuHandler buildMenuHandler;

    private ProjectFolderPane projectFolderPane;
    private JButton fileButton;
    private JButton buildButton;

    public HeaderMenuPane(JButton fileBtn, JButton buildBtn, ProjectFolderPane projFolderPane) {
        fileMenuHandler = new FileMenuHandler();
        buildMenuHandler = new BuildMenuHandler();

        jfilePopupMenu = new JPopupMenu();
        jbuildPopupMenu = new JPopupMenu();

        fileButton = fileBtn;
        buildButton = buildBtn;

        projectFolderPane = projFolderPane;

        fileBtn.addActionListener(fileMenuHandler);
        buildBtn.addActionListener(buildMenuHandler);
    }

    public void init() {
        for(JMenuItem o : fileMenuHandler.getMenus()) {
            jfilePopupMenu.add(o);
        }
        for(JMenuItem o : buildMenuHandler.getMenus()) {
            jbuildPopupMenu.add(o);
        }
    }

    public class FileMenuHandler implements ActionListener {
        public final static String OPEN_PROJECT = "Open Project";
        public final static String CLOSE_PROJECT = "Close Project";
        public final static String NEW_PROJECT = "New Project";

        public JMenuItem[] getMenus() {
             return Arrays.stream(
                     new String[]{
                         OPEN_PROJECT,
                         CLOSE_PROJECT,
                         NEW_PROJECT
                     })
                     .map(menu -> {
                        JMenuItem mi = new JMenuItem(menu);
                        mi.addActionListener(this);

                        return mi;
                     }).toArray(JMenuItem[]::new);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch(actionEvent.getActionCommand()) {
                case OPEN_PROJECT:
                    projectFolderPane.openProject();
                    return;
                case CLOSE_PROJECT:
                    projectFolderPane.closeProject();
                    return;
                case NEW_PROJECT:
                    projectFolderPane.newProject();
                    return;
                default:
                    jfilePopupMenu.show(fileButton, fileButton.getX(), fileButton.getY());
            }
        }
    }

    private void buildProject() {
        ICodeCompilationUnit cu = new JavaCompilationUnit(projectFolderPane.currentDirectory);

        TerminalPane.Clear();
        TerminalPane.Write(cu.compile().Output);
    }

    private class BuildMenuHandler implements ActionListener {
        public final static String BUILD_PROJECT = "Build Project";
        public final static String RUN_PROJECT = "Run Project";

        public JMenuItem[] getMenus() {
            return Arrays.stream(
                    new String[]{
                            BUILD_PROJECT,
                            RUN_PROJECT
                    })
                    .map(menu -> {
                        JMenuItem mi = new JMenuItem(menu);
                        mi.addActionListener(this);

                        return mi;
                    }).toArray(JMenuItem[]::new);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch(actionEvent.getActionCommand()) {
                case BUILD_PROJECT:
                    buildProject();
                    break;
                case RUN_PROJECT:
                    //projectFolderPane.closeProject();
                default:
                    jbuildPopupMenu.show(buildButton, buildButton.getX(), buildButton.getY());
            }
        }
    }
}
