package edu.cs;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.time.format.DateTimeFormatter;

public class ContentDAO {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    // Constructor to initialize the database properties from the web app's configuration
    public ContentDAO(String applicationPath) {
        loadProperties(applicationPath);
    }

    // Method to load database properties from the db.properties file
    private void loadProperties(String applicationPath) {
        Properties properties = new Properties();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (FileInputStream infile = new FileInputStream(applicationPath + java.io.File.separator + "WEB-INF" + java.io.File.separator + "db.properties")) {
            properties.load(infile);
            dbUrl = properties.getProperty("db.url");
            dbUser = properties.getProperty("db.user");
            dbPassword = properties.getProperty("db.password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to fetch the top 10 contents ordered by upload time
    public List<Content> getTop10Contents() {
        List<Content> contents = new ArrayList<>();

        // Wrap the database operations in a try-catch block to handle SQLException
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT file_name, upload_time FROM uploaded_files ORDER BY upload_time DESC LIMIT 10";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Content content = new Content();
                content.setFileName(resultSet.getString("file_name"));
                content.setUploadTime(resultSet.getObject("upload_time", LocalDateTime.class));
                content.setFormattedUploadTime(getFormattedUploadTime(content.getUploadTime()));
                contents.add(content);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle any SQL exceptions here
        }

        cleanup();//clean up JDBC
        return contents;
    }

    // Method to fetch the content of a specific file
    public byte[] getFileContent(String fileName) {
        byte[] fileContent = null;

        // Wrap the database operations in a try-catch block to handle SQLException
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            String query = "SELECT file_content FROM uploaded_files WHERE file_name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, fileName);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                fileContent = resultSet.getBytes("file_content");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle any SQL exceptions here
        }

        cleanup();//clean up JDBC
        return fileContent;
    }

    public String getFormattedUploadTime(LocalDateTime uploadTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return uploadTime.format(formatter);
    }

    // Method to clean up resources like deregistering the JDBC driver and stopping the cleanup thread
    public void cleanup() {
        try {
            // Deregister the MySQL JDBC Driver to avoid memory leaks
            DriverManager.deregisterDriver(new com.mysql.cj.jdbc.Driver());
        } catch (SQLException e) {
            e.printStackTrace(); // Handle deregistration errors
        }

        // Shut down the abandoned connection cleanup thread to prevent memory leaks
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            e.printStackTrace(); // Handle any other potential errors
        }
    }
}
