package com.primaryconnect;

import com.primaryconnect.data.DatabaseManager;
import com.primaryconnect.model.User;
import com.primaryconnect.service.AuthManager;
import com.primaryconnect.ui.ProprietorMenu;
import com.primaryconnect.ui.PupilMenu;
import com.primaryconnect.ui.SessionContext;
import com.primaryconnect.ui.TeacherMenu;

import java.util.Scanner;

/**
 * Provides the console entry point for the PrimaryConnect application, initialising the database layer and routing users to role-specific menus after login.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        
        Scanner scanner = new Scanner(System.in);
        AuthManager authManager = new AuthManager();
        User loggedInUser = null;

        // Login loop - keep prompting until successful login
        while (loggedInUser == null) {
            System.out.println("\n=== PrimaryConnect Login ===");
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            
            loggedInUser = authManager.login(username, password);
            
            if (loggedInUser == null) {
                System.out.println("Invalid username or password. Please try again.");
            }
        }

        // Store logged-in user in session context
        SessionContext.getInstance().login(loggedInUser);
        
        // Dispatch to appropriate role menu
        String role = SessionContext.getInstance().getCurrentRole();
        
        switch (role) {
            case "PROPRIETOR":
                ProprietorMenu proprietorMenu = new ProprietorMenu();
                proprietorMenu.show();
                break;
            case "TEACHER":
                TeacherMenu teacherMenu = new TeacherMenu();
                teacherMenu.show();
                break;
            case "PUPIL":
                PupilMenu pupilMenu = new PupilMenu();
                pupilMenu.show();
                break;
            default:
                System.err.println("Error: Unknown role '" + role + "'. Cannot proceed.");
                databaseManager.closeConnection();
                System.exit(1);
        }

        // Clean shutdown after user logs out
        databaseManager.closeConnection();
        System.out.println("Thank you for using PrimaryConnect. Goodbye!");
    }
}
