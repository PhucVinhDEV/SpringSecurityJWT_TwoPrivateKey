package com.example.BitzNomad.Service;

import com.example.BitzNomad.DTO.AuthRequest;
import com.example.BitzNomad.Entity.Role;
import com.example.BitzNomad.Entity.User;
import com.example.BitzNomad.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Override
    public User createdUser(AuthRequest authRequest) {

        return userRepository.save(User.builder()
                        .username(authRequest.getEmail())
                        .password(passwordEncoder.encode(authRequest.getPassword()))
                        .roles(Set.of())
                .build());
    }
}
