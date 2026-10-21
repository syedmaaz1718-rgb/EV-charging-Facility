package com.abcgreen.evnet.config;

import com.abcgreen.evnet.model.Models.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String SESSION_USER = "user";

    public static User currentUser(HttpServletRequest req) {
        Object u = req.getSession().getAttribute(SESSION_USER);
        return u instanceof User user ? user : null;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                User u = currentUser(req);
                if (u == null) { res.sendRedirect("/login"); return false; }
                String path = req.getRequestURI();
                if (path.startsWith("/admin") && !u.isAdmin()) { res.sendRedirect("/dashboard"); return false; }
                return true;
            }
        }).addPathPatterns("/dashboard/**", "/history/**", "/book/**", "/admin/**", "/api/**", "/invoice/**", "/session/**")
          .excludePathPatterns("/api/login", "/api/register", "/api/public/**");
    }
}
