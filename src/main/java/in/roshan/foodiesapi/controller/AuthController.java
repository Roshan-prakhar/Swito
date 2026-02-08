package in.roshan.foodiesapi.controller;

import in.roshan.foodiesapi.io.AuthenticationRequest;
import in.roshan.foodiesapi.io.AuthenticationResponse;
import in.roshan.foodiesapi.service.AppUserDetailsService;
import in.roshan.foodiesapi.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody AuthenticationRequest request) {
        System.out.println("AuthController - Login attempt for: " + request.getEmail());
        
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        System.out.println("AuthController - Authentication successful");
        
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        System.out.println("AuthController - User loaded: " + userDetails.getUsername());
        
        // Log JWT secret key during generation
        jwtUtil.logSecretKey();
        
        final String jwtToken = jwtUtil.generateToken(userDetails);
        System.out.println("AuthController - Token generated: " + jwtToken.substring(0, 20) + "...");
        System.out.println("AuthController - Token length: " + jwtToken.length());
        
        return new AuthenticationResponse(request.getEmail(), jwtToken);
    }
}
