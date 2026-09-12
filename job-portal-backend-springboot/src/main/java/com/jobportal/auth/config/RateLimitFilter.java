package com.jobportal.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic in-memory rate limiter for auth endpoints — mitigates brute-force
 * login/register attempts. Good enough for a single-instance deployment;
 * for multi-instance production use, swap this for Redis-backed limiting
 * (e.g. Bucket4j + Redis) so counters are shared across instances.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 15 * 60 * 1000; // 15 minutes

    private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isAuthEndpoint = path.equals("/api/v1/auth/register") || path.equals("/api/v1/auth/login");

        if (!isAuthEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        RequestWindow window = requestCounts.computeIfAbsent(clientIp, k -> new RequestWindow());

        long now = System.currentTimeMillis();
        if (now - window.windowStart > WINDOW_MS) {
            window.windowStart = now;
            window.count.set(0);
        }

        if (window.count.incrementAndGet() > MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many attempts, please try again later\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private static class RequestWindow {
        volatile long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
