package com.example.BitzNomad.Config;

import com.example.BitzNomad.Constrant.PredefinePermission;
import com.example.BitzNomad.Constrant.PredefineRole;
import com.example.BitzNomad.Entity.Permission;
import com.example.BitzNomad.Entity.Role;
import com.example.BitzNomad.Entity.User;
import com.example.BitzNomad.Repository.PermissionRepository;
import com.example.BitzNomad.Repository.RoleRepository;
import com.example.BitzNomad.Repository.UserRepository;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@Slf4j
public class ApplicationConfig {
    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";


    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository;
    @Bean
    ApplicationRunner applicationRunner(PasswordEncoder passwordEncoder) {
        return args -> {
            // Tạo các quyền CRUD
            createPermissionIfNotExists(PredefinePermission.CREATE_PERMISSION, "Create permission");
            createPermissionIfNotExists(PredefinePermission.UPDATE_PERMISSION, "Update permission");
            createPermissionIfNotExists(PredefinePermission.DELETE_PERMISSION, "Delete permission");
            createPermissionIfNotExists(PredefinePermission.VIEW_PERMISSION, "Read permission");

            createPermissionIfNotExists(PredefinePermission.VIEW_ROLE,"Read role");
            createPermissionIfNotExists(PredefinePermission.CREATOR_ROLE, "Create role");
            createPermissionIfNotExists(PredefinePermission.UPDATE_ROLE,"Update role");
            createPermissionIfNotExists(PredefinePermission.DELETE_ROLE,"Delete role");
// Tạo role với các quyền CRUD
            Role adminRole = roleRepository.save(Role.builder()
                    .name(PredefineRole.ADMIN_ROLE)
                    .description("Admin role")
                    .permissions(
                            new HashSet<>(permissionRepository.findAll())
                    )
                    .build());

            Role userRole = roleRepository.save(Role.builder()
                    .name(PredefineRole.OWNER_ROLE)
                    .description("User role")
                    .permissions(Set.of(
                    ))
                    .build());

            Role customerRole = roleRepository.save(Role.builder()
                    .name(PredefineRole.OWNER_ROLE)
                    .description("Customer role")
                    .permissions(Set.of(
                    ))
                    .build());

            // Tạo user với role admin
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User user = User.builder()
                    .username(ADMIN_USER_NAME)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .roles(roles)
                    .build();
            userRepository.save(user);

            log.warn("admin user has been created with default password: admin, please change it");

        };
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    // Phương thức tạo quyền nếu chưa tồn tại
    private void createPermissionIfNotExists(String name,String descreption) {
        if (!permissionRepository.existsById(name)) {
            permissionRepository.save(Permission.builder()
                    .permissionName(name)
                            .description(descreption)
                    .build());
        }
    }
}
