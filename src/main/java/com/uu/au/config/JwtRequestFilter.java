package com.uu.au.config;

import com.uu.au.controllers.AuthController;
import com.uu.au.enums.errors.AuthErrors;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    AuthController authController;

    @Autowired
    private UserRepository users;

    private final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/portfolio")  /// Web sockets
                || request.getServletPath().startsWith("/admin/") /// Adming endpoints can only be run from localhost
                || request.getServletPath().startsWith("/dev/")
                || ok.contains(request.getServletPath());
    }

    // FIXME: replace with proper handling
    private static final List<String> ok = List.of("/autoclose", "/su", "/login/cas", "/consume-token", "/auth", "/authDemo", "/secured", "/unregistered", "/unsuccessful", "/error", "/", "/webhook/github/accept", "/restart");
    private static final List<String> gitHubAuthEndPoints = List.of("/login/oauth2/code/github", "/authenticate-github", "/webhook/github/accept");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final var authHeader = Optional.ofNullable(request.getHeader("Authorization"));
        var token = Optional.ofNullable(authHeader.flatMap(h -> h.startsWith("Bearer") ? Optional.of(h.substring(7)) : Optional.empty()).orElse(request.getHeader("token")));

        if (gitHubAuthEndPoints.stream().anyMatch(url -> request.getServletPath().startsWith(url))) {
            token = Optional.ofNullable(request.getParameter("state"));
        }

        if (token.isPresent()) {
            try {
                var auUser = authController.installUserFromToken(token.get());

                final var usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            auUser.getUsername(), null, auUser.getAuthorities());
                usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                /// Only point of continuation into system
                chain.doFilter(request, response);

            } catch (JwtException e) {
                logger.warn("Attempt to login with expired token " + e.getMessage() + "  :: " + request.getRequestURI());
                throw AuthErrors.JWTTokenExpired();
            } catch (NullPointerException | NumberFormatException e) {
                logger.warn("Invalid token found in request: '" + token + "'");
                throw UserErrors.malformedUserName("Invalid token");
            }
        } else {
            logger.info("No token in request to " + request.getRequestURI());
            //chain.doFilter(request, response);
            throw UserErrors.userNotFound();
        }
    }


}

