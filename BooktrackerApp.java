import java.sql.*;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

public class BooktrackerApp {
    private static Connection conn;
    private static Scanner scanner = new Scanner(System.in);

    // Menu-related methods first
    private static void showMenu() {
        while (true) {
            System.out.println("\n1. Add user");
            System.out.println("2. Get reading habits by user");
            System.out.println("3. Change book title");
            System.out.println("4. Delete reading record");
            System.out.println("5. Mean age of users");
            System.out.println("6. Total users per book");
            System.out.println("7. Total pages read");
            System.out.println("8. Users with >1 book");
            System.out.println("9. Add 'Name' column to User");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: addUser(); break;
                case 2: getHabitsByUser(); break;
                case 3: changeBookTitle(); break;
                case 4: deleteRecord(); break;
                case 5: calculateMeanAge(); break;
                case 6: countUsersPerBook(); break;
                case 7: calculateTotalPages(); break;
                case 8: countUsersWithMultipleBooks(); break;
                case 9: addNameColumn(); break;
                case 0: System.exit(0);
                default: System.out.println("Invalid choice");
            }
        }
    }

    private static void addUser() {
        System.out.print("Enter userID: ");
        int userID = scanner.nextInt();
        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("enter gender (m/f): ");
        String gender = scanner.nextLine();
        System.out.print("Enter name (optional): ");
        String name = scanner.nextLine();

        String sql = "INSERT OR IGNORE INTO User (userID, age, gender, Name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, age);
            pstmt.setString(3, gender);
            pstmt.setString(4, name);
            pstmt.executeUpdate();
            System.out.println("User added");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getHabitsByUser() {
        System.out.print("Enter userID: ");
        int userID = scanner.nextInt();

        // Check if user exists first
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM User WHERE userID = ?")) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                System.out.println("User " + userID + " does not exist");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error checking user: " + e.getMessage());
            return;
        }

        // Get reading habits
        String sql = "SELECT * FROM ReadingHabit WHERE user = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nReading habits for user " + userID + ":");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf(
                        "HabitID: %d\nBook: %s\nPages: %d\nDate: %s\n\n",
                        rs.getInt("habitID"),
                        rs.getString("book"),
                        rs.getInt("pagesRead"),
                        rs.getString("submissionMoment")
                );
            }

            if (count == 0) {
                System.out.println("No reading habits found for this user");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Other menu operations (implement similarly)
    private static void changeBookTitle() {
        System.out.print("Enter habitID to update: ");
        int habitID = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter new book title: ");
        String newTitle = scanner.nextLine();

        String sql = "UPDATE ReadingHabit SET book = ? WHERE habitID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setInt(2, habitID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Book title updated successfully");
            } else {
                System.out.println("No record found with habitID: " + habitID);
            }
        } catch (SQLException e) {
            System.out.println("Error updating book title: " + e.getMessage());
        }
    }

    private static void deleteRecord() {
        System.out.print("Enter habitID to delete: ");
        int habitID = scanner.nextInt();

        String sql = "DELETE FROM ReadingHabit WHERE habitID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Record deleted successfully");
            } else {
                System.out.println("No record found with habitID: " + habitID);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    private static void calculateMeanAge() {
        String sql = "SELECT AVG(age) FROM User";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                double meanAge = rs.getDouble(1);
                System.out.printf("Mean age of users: %.2f years\n", meanAge);
            }
        } catch (SQLException e) {
            System.out.println("Error calculating mean age: " + e.getMessage());
        }
    }

    private static void countUsersPerBook() {
        scanner.nextLine(); // Clear buffer
        System.out.print("Enter book title to search: ");
        String bookTitle = scanner.nextLine();

        String sql = "SELECT COUNT(DISTINCT user) FROM ReadingHabit WHERE book LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + bookTitle + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Number of users who read '" + bookTitle + "': " + count);
            }
        } catch (SQLException e) {
            System.out.println("Error counting users per book: " + e.getMessage());
        }
    }

    private static void calculateTotalPages() {
        String sql = "SELECT SUM(pagesRead) FROM ReadingHabit";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int totalPages = rs.getInt(1);
                System.out.println("Total pages read by all users: " + totalPages);
            }
        } catch (SQLException e) {
            System.out.println("Error calculating total pages: " + e.getMessage());
        }
    }

    private static void countUsersWithMultipleBooks() {
        String sql = "SELECT COUNT(*) FROM (" +
                "SELECT user FROM ReadingHabit " +
                "GROUP BY user HAVING COUNT(DISTINCT book) > 1)";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Number of users who read more than one book: " + count);
            }
        } catch (SQLException e) {
            System.out.println("Error counting users with multiple books: " + e.getMessage());
        }
    }

    private static void addNameColumn() {
        try (Statement stmt = conn.createStatement()) {
            // Check if column already exists
            ResultSet rs = conn.getMetaData().getColumns(null, null, "User", "Name");
            if (rs.next()) {
                System.out.println("Name column already exists in User table");
            } else {
                stmt.execute("ALTER TABLE User ADD COLUMN Name TEXT DEFAULT ''");
                System.out.println("Name column added to User table");
            }
        } catch (SQLException e) {
            System.out.println("Error adding Name column: " + e.getMessage());
        }
    }

    // Database operations
    private static void connectToDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = System.getProperty("user.dir") + "/booktracker.db";
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void clearDatabase() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS ReadingHabit");
            stmt.execute("DROP TABLE IF EXISTS User");
        } catch (SQLException e) {
            System.err.println("Error clearing database: " + e.getMessage());
        }
    }

    private static void initializeDatabase() {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS User (" +
                    "userID INTEGER PRIMARY KEY, " +
                    "age INTEGER, " +
                    "gender TEXT, " +
                    "Name TEXT DEFAULT '')");

            stmt.execute("CREATE TABLE IF NOT EXISTS ReadingHabit (" +
                    "habitID INTEGER PRIMARY KEY, " +
                    "user INTEGER, " +
                    "book TEXT, " +
                    "pagesRead INTEGER, " +
                    "submissionMoment TEXT, " +
                    "FOREIGN KEY(user) REFERENCES User(userID))");

        } catch (SQLException e) {
            System.err.println("Table creation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void importUserCSV() {
        String csvFile = "User.csv";
        String sql = "INSERT OR IGNORE INTO User (userID, age, gender) VALUES (?, ?, ?)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            br.readLine(); // Skip header row
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",");
                    pstmt.setInt(1, Integer.parseInt(data[0].trim()));
                    pstmt.setInt(2, Integer.parseInt(data[1].trim()));
                    pstmt.setString(3, data[2].trim());
                    pstmt.executeUpdate();
                    count++;
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in User CSV: " + e.getMessage());
        }
    }

    private static void importReadingHabitCSV() {
        String csvFile = "ReadingHabit.csv";
        String sql = "INSERT OR IGNORE INTO ReadingHabit (habitID, user, book, pagesRead, submissionMoment) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            br.readLine(); // Skip header
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                    pstmt.setInt(1, Integer.parseInt(data[0].trim()));
                    pstmt.setInt(2, Integer.parseInt(data[1].trim()));
                    pstmt.setString(3, data[3].replace("\"", "").trim());
                    pstmt.setInt(4, Integer.parseInt(data[2].trim()));
                    pstmt.setString(5, data[4].trim());
                    pstmt.executeUpdate();
                    count++;
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ReadingHabit CSV: " + e.getMessage());
        }
    }

    // Main method last
    public static void main(String[] args) {
        connectToDatabase();
        clearDatabase();
        initializeDatabase();
        importUserCSV();
        importReadingHabitCSV();
        showMenu();
    }
}