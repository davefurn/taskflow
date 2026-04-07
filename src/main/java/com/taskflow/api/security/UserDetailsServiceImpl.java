package com.taskflow.api.security;

import com.taskflow.api.entity.User;
import com.taskflow.api.repository.authAndUsers.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String emailOrId)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(emailOrId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + emailOrId));

        return new org.springframework.security.core.userdetails.User(
                // Store UUID as username — SecurityUtil.getCurrentUser() uses this
                user.getId().toString(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name().toUpperCase()))
        );
    }
}