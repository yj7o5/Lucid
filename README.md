# Lucid
A simple code editor to build great projects

Code editors help programmers to develop software efficiently. They help developers to browse, edit, build and execute their programs. In this project, you develop a small editor.

Core functionality of the system:
1. Open/create/close/save project
2. Open/create/close/edit/save/remove file
3. Compile project
4. Execute project
5. Providing real-time statistics about the number of keywords in the project
6. Use blue to color for “if”, “else”, “for”, “while” keywords.
7. Use red to color arithmetic and Boolean operators, e.g., “+”, “-“, “/”, “||”.
8. Use green for strings.

Assumptions:
---
* Each project is stored in separate directory from others.
* There is only one project open in the editor
* Content of each project is stored in separate directories that is selected by users.
* A project has one Main.java file that includes the main method, and at most two other Java files, all in the same directory.
* All dependencies (jar files) of a project is stored in a /lib subdirectory under the project directory

Timetable:
---
* Sept 5  --- Lo-Fi prototype
* Sept 12 --- 1
* Sept 26 --- 2,6,7
* Oct 10  --- 3,4,5
* Oct 24  --- removing code smells
* Nov 7   --- new feature 1 / propose configurable syntax theme - background color - font size
* Nov 21  --- new feature 2 / propose terminal tab
