package com.esw.parkingmanager.security;

import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Spring calls this at login and then checks the password against the hash itself
    // A wrong email and a wrong password look identical to the client and both give 401.
    // Spring hides “user not found” on purpose so nobody can look which emails have accounts.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(AppUser.normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthenticatedUser(user);
    }
}