package com.example.BitzNomad.Service;

import com.example.BitzNomad.DTO.AuthRequest;
import com.example.BitzNomad.Entity.User;

public interface UserService {
    User createdUser(AuthRequest authRequest);
}
