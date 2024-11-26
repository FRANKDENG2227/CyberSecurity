package edu.cs;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet({"/FileUploadServlet"})
@MultipartConfig(
        fileSizeThreshold = 1024*1024*10,
        maxFileSize = 1024*1024*50,
        maxRequestSize = 1024*1024*100
)

public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 205242440643911308L;
    private static final String UPLOAD_DIR = "uploads";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;

            File fileSaveDir = new File(uploadFilePath);
            if (!fileSaveDir.exists()) {
                fileSaveDir.mkdirs();
            }

            System.out.println("Upload File Directory=" + fileSaveDir.getAbsolutePath());
            String fileName = "";
            byte[] content = null;
            for (Part part : request.getParts()) {
                if (part.getSize() > 1024 * 1024 * 50) { // 50 MB
                    throw new IllegalStateException("File size exceeds the allowed limit of 50 MB");                }
                else if (part.getSize() > 0) {
                    fileName = getFileName(part);
                    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    part.write(uploadFilePath + File.separator + fileName);
                    // Read file content as bytes
                    content = readFileContentAsBytes(uploadFilePath + File.separator + fileName, response);
                    if (content == null) { // If reading the file content failed, stop further processing
                        response.getWriter().write("Error reading file content.");
                        return;
                    }
                } else {
                    response.getWriter().write("No file uploaded or file is empty.");
                    return;
                }
            }

            // Load database connection details from properties file
            Properties properties = new Properties();
            try (FileInputStream infile = new FileInputStream(applicationPath + File.separator + "WEB-INF" + File.separator + "db.properties")) {
                properties.load(infile);
            } catch (IOException e) {
                e.printStackTrace();
                response.getWriter().write("Error loading database properties: " + e.getMessage());
                return;
            }

            String dbUrl = properties.getProperty("db.url");
            String dbUser = properties.getProperty("db.user");
            String dbPassword = properties.getProperty("db.password");
            // Load JDBC driver
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                response.getWriter().write("Error loading JDBC Driver: " + e.getMessage());
                return;
            }

            // Insert file upload information into the database
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String insertSQL = "INSERT INTO uploaded_files (file_name,file_content,upload_time) VALUES (?,?,?)";
                PreparedStatement statement = connection.prepareStatement(insertSQL);
                statement.setString(1, fileName);
                statement.setBytes(2, content);
                statement.setObject(3, LocalDateTime.now());
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().write("Error saving file info to database: " + e.getMessage());
            }

            // Set success message in session
            request.getSession().setAttribute("successMessage", "File uploaded successfully!");
            // Redirect back to the main page without URL
            response.sendRedirect(request.getContextPath() + "/");

        } catch (IllegalStateException e) {
            e.printStackTrace();
            response.getWriter().write("File size exceeds the allowed limit of 50 MB");

        }
        catch (IOException e) {
            e.printStackTrace();
            response.getWriter().write("I/O error occurred: " + e.getMessage());

        } catch (ServletException e) {
            e.printStackTrace();
            response.getWriter().write("error: "+e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred during file upload: " + e.getMessage());
        }
    }

    // Unified method to read file content as bytes
    private byte[] readFileContentAsBytes(String filePath, HttpServletResponse response) {
        try (InputStream inputStream = new FileInputStream(new File(filePath));
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.getWriter().write("Error reading file content: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }


    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println("content-disposition header= " + contentDisp);
        String[] tokens = contentDisp.split(";");
        String[] var4 = tokens;
        int var5 = tokens.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            String token = var4[var6];
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }

        return "";
    }

}