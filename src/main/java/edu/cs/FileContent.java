package edu.cs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet("/FileContent")
public class FileContent extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ContentDAO contentDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        String applicationPath = getServletContext().getRealPath("");
        contentDAO = new ContentDAO(applicationPath);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("list".equals(action)) {
            handleListAction(response);
        } else if ("view".equals(action)) {
            handleViewAction(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action parameter.");
        }
    }

    private void handleListAction(HttpServletResponse response) throws IOException {
        List<Content> topContents = contentDAO.getTop10Contents();
        //System.out.println("Retrieved contents: " + topContents); // Debug log
        String json = new Gson().toJson(topContents);
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            out.flush();
        }
    }

    private void handleViewAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = request.getParameter("filePath");
        byte[] fileContent = contentDAO.getFileContent(fileName);

        if (fileContent != null) {
            String fileType = getServletContext().getMimeType(fileName);
            response.setContentType(fileType != null ? fileType : "application/octet-stream");
            response.setContentLength(fileContent.length);
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

            try (OutputStream out = response.getOutputStream()) {
                out.write(fileContent);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found in database.");
        }
    }
}
