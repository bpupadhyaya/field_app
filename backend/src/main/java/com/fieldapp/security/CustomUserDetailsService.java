package com.fieldapp.security;

import com.fieldapp.repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepo appUserRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = appUserRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var authorities = user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getName().name())).toList();
        return new User(user.getUsername(), user.getPasswordHash(), user.isEnabled(), true, true, true, authorities);
    }
}
