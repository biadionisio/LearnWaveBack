package com.example.learnwave.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ChatAuthenticationFilter implements Filter {
    public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";
    private final ChatTokenService tokens;

    public ChatAuthenticationFilter(ChatTokenService tokens) { this.tokens = tokens; }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!request.getRequestURI().startsWith("/api/chat/")) {
            chain.doFilter(req, res);
            return;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Faça login para acessar o chat.");
            return;
        }
        try {
            request.setAttribute(USER_ID_ATTRIBUTE, tokens.validateAndGetUserId(authorization.substring(7)));
            chain.doFilter(req, res);
        } catch (IllegalArgumentException error) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessão inválida ou expirada.");
        }
    }
}
