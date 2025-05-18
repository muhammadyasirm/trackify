Trackify - A Personal Productivity App
Trackify is a Java-based desktop application built with JavaFX and MySQL, designed to help users manage their tasks and notes efficiently. It features user authentication, a modern graphical user interface with sidebar navigation, and user-specific data storage to ensure privacy and personalization.
Features

User Authentication:
Register with a username, email, and password.
Log in to access personal tasks and notes.
Passwords are stored in plain text (for development; not recommended for production).


Task Management:
Add, edit, and delete tasks with details such as due date, priority (A, B, C, D), and category (Work, Personal, Study, Home, Other).
Task cards display a preview (first 50 characters); full details (task text, due date, priority, category, completed status) are shown in an edit dialog.
Mark tasks as completed, with visual strikethrough on cards.


Notes Management:
Create, edit, and delete rich-text notes using an HTML editor.
Notes are displayed in a grid with a preview (first 100 characters).
Edit or delete notes by clicking on their cards.


User-Specific Data:
Tasks and notes are tied to the logged-in user’s UserID, ensuring data isolation between users.


Modern GUI:
Consistent 1300x700 pixel windows with a light gray background (#f1f5f9) and white cards for content.
Sidebar navigation for switching between Tasks and Notes, with a logout option.
Logo and "Trackify" title displayed in the header of all windows (Intro, Login, Register, Main Dashboard).
Professional styling with shadows, rounded corners, cyan buttons (#00acc1), and hover effects for interactivity.



Technologies Used

Java 23: Core programming language.
JavaFX: For the graphical user interface.
MySQL: Database for storing users, tasks, and notes.
MySQL Connector/J: JDBC driver for database connectivity.

Project Structure
copy these all files in this repository and paste them to Src folder in your project


Setup Instructions

Prerequisites
Java Development Kit (JDK) 23.
JavaFX SDK.
MySQL Server.
IntelliJ IDEA.


Steps

Clone or Download the Repository:

If using Git:
git clone https://github.com/muhammadyasirm/trackify.git
cd trackify


Alternatively, download the ZIP file from GitHub and extract it.



Set Up MySQL Database:

Start MySQL Server.

Create a database named todoapp:
CREATE DATABASE todoapp;


Run the schema.sql file to create tables:
USE todoapp;
CREATE TABLE users (
    UserID INT PRIMARY KEY AUTO_INCREMENT,
    Username VARCHAR(50),
    Email VARCHAR(100),
    Password VARCHAR(100)
);

CREATE TABLE notes (
    Notes_ID INT PRIMARY KEY AUTO_INCREMENT,
    user_ID INT,
    Notes LONGTEXT,
    lastModified TIMESTAMP,
    FOREIGN KEY (user_ID) REFERENCES users(UserID)
);

CREATE TABLE tasks (
    task_ID INT PRIMARY KEY AUTO_INCREMENT,
    task VARCHAR(10000),
    dueDate DATE,
    dueTime TIME,
    priority VARCHAR(50),
    category VARCHAR(100),
    completed BOOLEAN,
    user_ID INT,
    FOREIGN KEY (user_ID) REFERENCES users(UserID)
);


Ensure MySQL credentials match those in DBconnection.java or replace these with your own:
private static final String URL = "jdbc:mysql://127.0.0.1:3306/todoapp?useSSL=false";
private static final String USERNAME = "root";
private static final String PASSWORD = "yasir";




Configure JavaFX:

Download and install the JavaFX SDK from openjfx.io.
In IntelliJ IDEA:
Go to File > Project Structure > Libraries, add the JavaFX SDK lib folder.

In the run configuration for Main.java, add VM options:
--module-path "/path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml,javafx.web --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED 

Replace /path/to/javafx-sdk/lib with the actual path to the JavaFX SDK.


Add MySQL Connector:

The mysql-connector-java-8.0.33.jar is included in the lib/ folder.
In IntelliJ IDEA:
Go to File > Project Structure > Modules > Dependencies.
Click "+" > JARs or Directories, select Trackify/lib/mysql-connector-java-8.0.33.jar, and click OK.
Ensure Export is checked.




Run the Application:

Open the project in IntelliJ IDEA.
Build the project (Build > Rebuild Project).
Run Main.java (select it and press Shift+F10).



Usage

Intro Screen:
Displays the Trackify logo and title for 3 seconds, then transitions to the login screen.


Login/Register:
Register a new user with a username, email, and password.
Log in using your credentials to access the main dashboard.


Main Dashboard:
Use the sidebar to navigate between Tasks and Notes.
Tasks:
Add tasks with a description, due date, priority, and category via the input area at the bottom.
View task previews on cards; click Edit to modify details (task text, due date, priority, category, completed status) or Delete to remove.


Notes:
Add rich-text notes using the HTML editor.
View note previews in a grid; click a card to edit or delete.


Logout: Return to the login screen.


Data Privacy:
Each user sees only their own tasks and notes, based on their UserID.


Security Note
Passwords are stored in plain text for development simplicity. For production, use a secure hashing library like jBCrypt.
Update DBconnection.java with secure MySQL credentials.


Contact
Email: semairfatimamazari@gmail.com

