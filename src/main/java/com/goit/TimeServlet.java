package com.goit;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        JakartaServletWebApplication jswa =
                JakartaServletWebApplication.buildApplication(this.getServletContext());

        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(jswa);
        resolver.setPrefix("./");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String utcParam = req.getParameter("timezone");
        String currentFormattedTime = getCurrentTimeInSelectedUTC(utcParam, req, resp);

        resp.setContentType("text/html");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Context simpleContext = new Context(req.getLocale(), Map.of("time", currentFormattedTime));

        engine.process("timeTemplate", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }

    private String getCurrentTimeInSelectedUTC(String utc, HttpServletRequest req, HttpServletResponse resp) {
        if (utc == null || utc.isEmpty()) {
            utc = getTimezoneCookie(req);
            if (utc.isEmpty()) {
                utc = "UTC";
            }
        } else {
            resp.addCookie(new Cookie("lastTimezone", utc));
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(utc));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss zz");
        return zonedDateTime.format(formatter);
    }

    private String getTimezoneCookie(HttpServletRequest req) {
        String timezoneCookie = "";
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lastTimezone")) {
                    timezoneCookie = cookie.getValue();
                }
            }
        }
        return timezoneCookie;
    }
}




