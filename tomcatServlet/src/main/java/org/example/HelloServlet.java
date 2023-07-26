package org.example;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 1. SingleThreadModel 是一个接口，作为标识，是一个单线程访问
 * 2.默认 servlet 是线程共享的
 */
public class HelloServlet  extends HttpServlet implements SingleThreadModel{
    public HelloServlet(){}
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("This is HelloServlet Application...");
     }
}
