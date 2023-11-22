//package com.evhub.app.filter;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//
//import com.evhub.app.util.CommonUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@WebFilter("/*")
//public class StatsFilter implements Filter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(StatsFilter.class);
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        // empty
//    }
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
//        long time = CommonUtils.getCurrentTimeInMillis();
//        try {
//            chain.doFilter(req, resp);
//        } finally {
//            time = CommonUtils.getCurrentTimeInMillis() - time;
//            System.out.println(((HttpServletRequest) req).getRequestURI() + " time taken in millisecond " + time);
//        }
//    }
//
//    @Override
//    public void destroy() {
//        // empty
//    }
//}
