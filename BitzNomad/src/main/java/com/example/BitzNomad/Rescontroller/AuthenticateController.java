package com.example.BitzNomad.Rescontroller;

import com.example.BitzNomad.DTO.ApiResponse;
import com.example.BitzNomad.DTO.AuthRequest;
import com.example.BitzNomad.DTO.AuthenticationResponse;
import com.example.BitzNomad.DTO.TokenRequest;
import com.example.BitzNomad.Service.AuthenticateService;
import com.example.BitzNomad.Service.JWTService;
import com.example.BitzNomad.Service.UserService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
public class AuthenticateController {

    @Autowired
    AuthenticateService authenticateService;

    @Autowired
    JWTService jwtService;

    @Autowired
    UserService userService;

    @PostMapping("/login")
    @CrossOrigin
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthRequest request) {

        AuthenticationResponse result = authenticateService.authenticate(request);
        return  ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/register")
    @CrossOrigin
    public ApiResponse<Void> create(@RequestBody AuthRequest request) {
        userService.createdUser(request);
        return ApiResponse.<Void> builder()
                .message("Created User Successfully")
                .status(201)
                .build();
    }
    @PostMapping("/refesh")
    ApiResponse<AuthenticationResponse>  authenticate(@RequestBody TokenRequest request) throws ParseException, JOSEException {
        AuthenticationResponse result = jwtService.refeshToken(request.getToken());
        return  ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void>  logout(@RequestBody TokenRequest request) throws ParseException, JOSEException {
        jwtService.logout(request.getToken());
        return  ApiResponse.<Void>builder()
                .build();
    }
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PERMISSION')")
    public ApiResponse<Void> getAuthentication() {
        return ApiResponse.<Void>builder()
                .message("test123")
                .build();
    }
}
