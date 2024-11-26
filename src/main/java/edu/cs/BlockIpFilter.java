package edu.cs;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@WebFilter("/*")
public class BlockIpFilter implements Filter {
    private Set<String> bannedIPs = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String applicationPath = filterConfig.getServletContext().getRealPath("");
        loadBanList(applicationPath + File.separator + "WEB-INF" + File.separator + "banlist.txt");
    }

    private void loadBanList(String filePath) throws ServletException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bannedIPs.add(line.trim());
            }
        } catch (IOException e) {
            throw new ServletException("Error loading ban list", e);  // Or log it more appropriately
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIP = httpRequest.getRemoteAddr();

        if (bannedIPs.contains(clientIP)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("Access denied.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        bannedIPs.clear();
        System.out.println("IPBanFilter is being destroyed. Resources have been cleaned up.");
    }
}
