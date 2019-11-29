package com.components;

import com.core.IProjectManager;
import com.core.ProjectManager;
import com.sun.source.tree.Tree;
import com.utilities.Guard;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ProjectFolderPane {
    private JTree projectFolderComponent;
    private JPopupMenu projectFolderMenuComponent;
    private JPopupMenu fileMenuComponent;
    private JPopupMenu libMenuComponent;
    private JPopupMenu libFileMenuComponent;

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
        fileMenuComponent = new JPopupMenu();
        libMenuComponent = new JPopupMenu();
        libFileMenuComponent = new JPopupMenu();

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
            projectFileMenuComponent.add(menu);
        }

        for(JMenuItem menu : menuHandler.getFileMenus()) {
            fileMenuComponent.add(menu);
        }

        for(JMenuItem menu : menuHandler.getLibMenus()) {
            libMenuComponent.add(menu);
        }

        for(JMenuItem menu : menuHandler.getLibFileMenus()) {
            libFileMenuComponent.add(menu);
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

    public void newFile() throws IOException {
        String fileName = JOptionPane.showInputDialog("Enter new file: ");

        File file = new File(currentDirectory.getPath() + File.separator + fileName);
        if(!file.exists()){
            file.createNewFile();
        }

        renderTree(currentDirectory);
    }

    public void saveProject() {
        if(editorPane.saveChanges()) {
            JOptionPane.showMessageDialog(mainFrame, "Project Saved!");
        }
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
            if (f.getName().endsWith(".class")) continue;
            if (f.isDirectory() && "libs".equals(f.getName().toLowerCase())) {
                DefaultMutableTreeNode libRoot = new DefaultMutableTreeNode(f.getName());
                Arrays.stream(f.listFiles(next -> next.getName().endsWith(".jar")))
                        .forEach(law -> {
                            libRoot.add(new DefaultMutableTreeNode(law.getName()));
                        });
                root.add(libRoot);
            }
            else {
                root.add(new DefaultMutableTreeNode(f.getName()));
            }
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

    private File getFile(String fileName) {
        return Arrays.stream(currentDirectory.listFiles())
                .filter(f -> f.getName().equals(fileName))
                .findFirst()
                .orElse(null);
    }

    public void saveFile() throws IOException {
        Editor editor = editorPane.getActiveEditor();

        editor.saveChanges();
    }

    public void deleteFile() throws IOException {
        String filename = projectFolderComponent.getLastSelectedPathComponent().toString();
        projectManager.deleteFile(currentDirectory, filename);
    }

    private class MenuClickHandler implements ActionListener {
        public final static String NEW_FILE = "New File";
        public final static String NEW_PROJECT = "New Project";
        public final static String CLOSE_PROJECT = "Close Project";
        public final static String SAVE_PROJECT = "Save Project";
        public final static String SAVE_FILE = "Save File";

        public final static String RENAME_FILE = "Rename File";
        public final static String DELETE_FILE = "Delete File";

        public final static String IMPORT_JAR = "Import Jar";
        public final static String DELETE_ALL_JAR = "Delete All Jar";

        public JMenuItem[] getMenus() {
            return getMenus(new String[]{ NEW_FILE, NEW_PROJECT, CLOSE_PROJECT, SAVE_PROJECT, SAVE_FILE });
        }

        public JMenuItem[] getFileMenus() {
            return getMenus(new String[] { RENAME_FILE, DELETE_FILE });
        }

        public JMenuItem[] getLibMenus() {
            return getMenus(new String[] { IMPORT_JAR, DELETE_ALL_JAR });
        }

        public JMenuItem[] getLibFileMenus() {
            return getMenus(new String[] { DELETE_FILE });
        }

        private JMenuItem[] getMenus(String[] menus) {
            return Arrays.stream(menus)
                    .map(this::addMenuListener)
                    .toArray(JMenuItem[]::new);
        }

        private JMenuItem addMenuListener(String menu) {
            JMenuItem mi = new JMenuItem(menu);
            mi.addActionListener(this);

            return mi;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                switch (actionEvent.getActionCommand()) {
                    case HeaderMenuPane.FileMenuHandler.CLOSE_PROJECT:
                        closeProject();
                        return;
                    case SAVE_PROJECT:
                        saveProject();
                        return;
                    case NEW_FILE:
                        newFile();
                        return;
                    case SAVE_FILE:
                        saveFile();
                        return;
                    case DELETE_FILE:
                        deleteFile();

                    default:
                        return;
                }
            }
            catch(IOException e) {}
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
                TreePath path = projectFolderComponent.getPathForRow(row);
                PathFileCheck pathCheck = new PathFileCheck(path);
                JPopupMenu activeMenu = null;

                if (pathCheck.isCodeFile())
                    activeMenu = fileMenuComponent;
                else if (pathCheck.isLibFolder())
                    activeMenu = libMenuComponent;
                else if (pathCheck.isLibFile()) {
                    activeMenu = libFileMenuComponent;
                }
                else if (row > 0) {
                    activeMenu = projectFolderMenuComponent;
                }

                if (activeMenu != null) activeMenu.show(e.getComponent(), x, y);
            }
            // check if double clicked
            else if (e.getClickCount() == 2) {
                TreePath tp = projectFolderComponent.getClosestPathForLocation(x, y);
                String name = tp.getLastPathComponent().toString();
                File file = getFile(name);

                try {
                    editorPane.openEditor(name, file);
                }
                catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
                }
            }
        }

        class PathFileCheck {
            TreePath p;
            public PathFileCheck(TreePath path) { p = path; }
            private String getLastPartOf(TreePath path) {
                return path.getPath()[path.getPathCount() - 1].toString().toLowerCase();
            }
            public boolean isLibFile() {
                return getLastPartOf(p).endsWith(".jar") &&
                        getLastPartOf(p.getParentPath()).equals("libs");
            }
            public boolean isCodeFile() {
                return getLastPartOf(p).endsWith(".java");
            }
            public boolean isLibFolder() {
                return getLastPartOf(p).equals("libs");
            }
        }
    }
}
