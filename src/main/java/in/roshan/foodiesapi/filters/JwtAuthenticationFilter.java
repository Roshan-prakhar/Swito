package in.roshan.foodiesapi.filters;

import in.roshan.foodiesapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        // Also check for lowercase authorization (HTTP headers are case-insensitive)
        final String authHeaderLower = request.getHeader("authorization");
        final String finalAuthHeader = authHeader != null ? authHeader : authHeaderLower;
        
        System.out.println("JWT Filter - Request URI: " + request.getRequestURI());
        System.out.println("JWT Filter - Auth Header (Authorization): " + authHeader);
        System.out.println("JWT Filter - Auth Header (authorization): " + authHeaderLower);
        System.out.println("JWT Filter - Final Auth Header: " + finalAuthHeader);
        
        if (StringUtils.hasText(finalAuthHeader) && finalAuthHeader.startsWith("Bearer ")) {
            String token = finalAuthHeader.substring(7);
            System.out.println("JWT Filter - Token extracted: " + token.substring(0, Math.min(token.length(), 20)) + "...");
            
            // Log JWT secret key for debugging
            jwtUtil.logSecretKey();
            
            try {
                String email = jwtUtil.extractUsername(token);
                System.out.println("JWT Filter - Email extracted: " + email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    System.out.println("JWT Filter - User loaded: " + userDetails.getUsername());

                    if (jwtUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        System.out.println("JWT Filter - Authentication set successfully");
                    } else {
                        System.out.println("JWT Filter - Token validation failed");
                    }
                } else {
                    System.out.println("JWT Filter - Email null or authentication already exists");
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - Exception: " + e.getMessage());
            }
        } else {
            System.out.println("JWT Filter - No valid Authorization header found");
        }
        filterChain.doFilter(request, response);
    }
}
