package com.components;

import com.core.IProjectManager;
import com.core.ProjectManager;
import com.utilities.Guard;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ProjectFolderPane {
    private JTree projectFolderComponent;
    private JPopupMenu projectFolderMenuComponent;
    private JPopupMenu projectFileMenuComponent;

    private MenuClickHandler menuHandler;
    private ProjectPaneClickHandler projectPaneHanlder;

    private IProjectManager projectManager;
    private JPanel mainFrame;

    private EditorPane editorPane;

    File currentDirectory;

    public ProjectFolderPane(JTree comp, JPanel frame, EditorPane ePane) {
        projectFolderComponent = comp;
        mainFrame = frame;
        editorPane = ePane;

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
        for(JMenuItem menu : menuHandler.getMenus()) {
            projectFolderMenuComponent.add(menu);
        }

        // Project file options: deleting, renaming
        for(JMenuItem menu : projectPaneHanlder.getMenus(menuHandler)) {
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

        if (Guard.safeNullCheck(projectName)) return;

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

    public void saveProject() {
        // TODO: output to terminal -- create a live feed terminal listening for print commands
        JOptionPane.showMessageDialog(mainFrame, "Project Saved!");
    }

    private void renderTree(File dir) {
        if (dir == null) {
            projectFolderComponent.setModel(null);
            return;
        }

        currentDirectory = dir;

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

    private String getFileContent(String fileName) throws IOException {
        String path = String.format("%s/%s", currentDirectory.getAbsolutePath(), fileName);
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null)
        {
            sb.append(line + "\n");
        }
        reader.close();
        return sb.toString();
    }

    private class MenuClickHandler implements ActionListener {
        public final static String NEW_FILE = "New File";
        public final static String NEW_PROJECT = "New Project";
        public final static String CLOSE_PROJECT = "Close Project";
        public final static String SAVE_PROJECT = "Save Project";

        public JMenuItem[] getMenus() {
            return Arrays.stream(
                    new String[]{
                            NEW_FILE,
                            NEW_PROJECT,
                            CLOSE_PROJECT,
                            SAVE_PROJECT
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
                case HeaderMenuPane.FileMenuHandler.CLOSE_PROJECT:
                    closeProject();
                    return;
                case SAVE_PROJECT:
                    saveProject();
                    return;

                default:
                    return;
            }
        }
    }

    private class ProjectPaneClickHandler extends MouseAdapter {
        public final static String DELETE_FILE = "Delete File";
        public final static String RENAME_FILE = "Rename File";

        public JMenuItem[] getMenus(ActionListener listener) {
            ProjectPaneClickHandler parent = this;
            return Arrays.stream(
                    new String[]{
                            DELETE_FILE,
                            RENAME_FILE
                    })
                    .map(menu -> {
                        JMenuItem mi = new JMenuItem(menu);
                        mi.addActionListener(listener);

                        return mi;
                    }).toArray(JMenuItem[]::new);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX(), y = e.getY(),
                    row = projectFolderComponent.getClosestRowForLocation(x, y);

            projectFolderComponent.setSelectionRow(row);

            if (SwingUtilities.isRightMouseButton(e))
            {
                if (row == 0) projectFolderMenuComponent.show(e.getComponent(), x, y);
                // else projectFileMenuComponent.show(e.getComponent(), x, y);
            }
            // check if double clicked
            else if (e.getClickCount() == 2) {
                TreePath tp = projectFolderComponent.getClosestPathForLocation(x, y);
                String name = tp.getLastPathComponent().toString();
                try {
                    String content = getFileContent(name);
                    editorPane.openEditor(name, content);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                String message = Arrays.stream(tp.getPath()).map(s -> s.toString()).collect(Collectors.joining("/"));
                JOptionPane.showMessageDialog(mainFrame, message);
            }
        }
    }
}
