package in.roshan.foodiesapi.service;

import in.roshan.foodiesapi.entity.UserEntity;
import in.roshan.foodiesapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        // Handle role with proper ROLE_ prefix
        String userRole = user.getRole();
        if (userRole == null || userRole.isEmpty()) {
            userRole = "ROLE_USER"; // Default role
        } else if (!userRole.startsWith("ROLE_")) {
            userRole = "ROLE_" + userRole; // Add ROLE_ prefix if missing
        }
        
        System.out.println("AppUserDetailsService - User: " + user.getEmail() + ", Role: " + userRole);
        
        return new User(user.getEmail(), user.getPassword(), 
            AuthorityUtils.createAuthorityList(userRole));
    }
}
