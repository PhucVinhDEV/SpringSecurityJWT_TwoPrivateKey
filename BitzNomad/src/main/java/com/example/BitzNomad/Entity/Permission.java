package com.example.BitzNomad.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import static java.lang.Boolean.FALSE;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission{
    @Id
    private String permissionName;

    private String description;

    private boolean deleted = FALSE;

}
