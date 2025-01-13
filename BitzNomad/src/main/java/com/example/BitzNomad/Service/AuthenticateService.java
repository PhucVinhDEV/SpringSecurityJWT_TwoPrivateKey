package com.example.BitzNomad.Service;

import com.example.BitzNomad.DTO.AuthRequest;
import com.example.BitzNomad.DTO.AuthenticationResponse;
import com.example.BitzNomad.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JWTService jwtService;

    public AuthenticationResponse authenticate(AuthRequest authRequest) {
        var User = userRepository.findById(authRequest.getEmail()).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        boolean authenticated = passwordEncoder.matches(authRequest.getPassword(), User.getPassword());
        if (!authenticated) {
            throw new RuntimeException("Wrong password!");
        }
        return AuthenticationResponse.builder()
                .authenticated(authenticated)
                .token(jwtService.generateJWT(User))
                .build();
    }
}
