package com.components;

import com.lucidcore.IProjectManager;
import com.lucidcore.ProjectManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class ProjectFolderPane {
    private JTree projectFolderComponent;
    private JPopupMenu projectFolderMenuComponent;
    private JPopupMenu projectFileMenuComponent;

    private MenuClickHandler menuHandler;
    private ProjectPaneClickHandler projectPaneHanlder;

    private IProjectManager projectManager;
    private JPanel mainFrame;

    public ProjectFolderPane(JTree comp, JPanel frame) {
        projectFolderComponent = comp;
        mainFrame = frame;

        projectFolderMenuComponent = new JPopupMenu();
        projectFileMenuComponent = new JPopupMenu();

        menuHandler = new MenuClickHandler();
        projectPaneHanlder = new ProjectPaneClickHandler();

        projectManager = new ProjectManager();

        projectFolderComponent.addMouseListener(projectPaneHanlder);
        projectFolderComponent.setModel(null);
    }

    public void init() {
        // Project folder menus: creating new file, new project, and closing project
        for(JMenuItem menu : createJMenuItems(new String[] {"New File", "New Project", "Close Project"})) {
            menu.addActionListener(menuHandler);
            projectFolderMenuComponent.add(menu);
        }

        // Project file options: deleting, renaming
        for(JMenuItem menu : createJMenuItems(new String[] {"Delete File", "Rename File"})) {
            menu.addActionListener(menuHandler);
            projectFileMenuComponent.add(menu);
        }
    }

    public void openProject() {
        File dir = projectManager.openProject(mainFrame);
        renderTree(dir);
    }

    public void closeProject() {
        renderTree(null);
    }

    public void newProject() {
        String projectName = JOptionPane.showInputDialog("Provide project name: ");

        // select the location TODO: don't allow existing location
        JFileChooser chooser = new JFileChooser("f:");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) return;

        File location = chooser.getSelectedFile();

        try {
            File project = projectManager.createProject(projectName, location.getAbsolutePath());
            renderTree(project);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Unabled to create project: " + e.getMessage());
        }
    }

    private void renderTree(File dir) {
        if (dir == null) {
            projectFolderComponent.setModel(null);
            return;
        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(dir.getName());
        DefaultTreeModel model = new DefaultTreeModel(root);

        for(File f : dir.listFiles()) {
            if (f.isHidden()) continue;
            root.add(new DefaultMutableTreeNode(f.getName()));
        }

        projectFolderComponent.setModel(model);
    }

    private ArrayList<JMenuItem> createJMenuItems(String[] menus) {
        ArrayList<JMenuItem> jmenus = new ArrayList<JMenuItem>();
        for (String menu : menus) {
            JMenuItem m = new JMenuItem(menu);
            jmenus.add(m);

            m.addActionListener(menuHandler);
        }
        return jmenus;
    }

    private class MenuClickHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch(actionEvent.getActionCommand()) {
                case HeaderMenuPane.FileMenuHandler.CLOSE_PROJECT:
                    closeProject();
                    return;
                default:
                    return;
            }
        }
    }

    private class ProjectPaneClickHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isRightMouseButton(e)) return;

            int x = e.getX(), y = e.getY(),
                row = projectFolderComponent.getClosestRowForLocation(x, y);

            projectFolderComponent.setSelectionRow(row);

            if (row == 0) projectFolderMenuComponent.show(e.getComponent(), x, y);
            else projectFileMenuComponent.show(e.getComponent(), x, y);
        }
    }
}
