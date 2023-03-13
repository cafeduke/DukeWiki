package com.github.cafeduke.dukewiki.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class RewriteFilter implements Filter
{
    private static final String URI_HOME = "/home";
    
    private String contextRoot = null;

    @Override
    public void init(FilterConfig config) throws ServletException
    {
       contextRoot = config.getServletContext().getContextPath();
    }
    
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException 
    {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        String requestURI = request.getRequestURI().replace(contextRoot, "").replaceFirst("(.*)/$", "$1");
        File fileTarget = new File(request.getServletContext().getRealPath(requestURI));
        
        System.out.println("Filter: QS=" + request.getQueryString() + " requestURI=" + request.getRequestURL() + " pathInfo=" + request.getPathInfo());
        
        // Directory listing is only for directories that start with URI_HOME
        if (requestURI.startsWith(URI_HOME) )
        {
           
           // String requestURIRewrite = contextRoot;
           String requestURIRewrite = "";
           if (fileTarget.isDirectory())
               requestURIRewrite += "/list.do?path=" + requestURI;
           else if (fileTarget.getName().endsWith(".md"))
               requestURIRewrite += "/render.do?path=" + requestURI;
           else
               requestURIRewrite += "/download.do?path=" + requestURI;
           
           System.out.println("[RewriteFilter] IsDir=" + fileTarget.isDirectory() + " URIRewrite=" + requestURIRewrite);
           request.getRequestDispatcher(requestURIRewrite).forward(request, response);
        }
        else 
            chain.doFilter(request, response);
    }
}
