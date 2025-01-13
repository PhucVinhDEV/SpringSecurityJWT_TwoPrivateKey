package com.example.BitzNomad.Repository;

import com.example.BitzNomad.Entity.Role;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Lazy
public interface RoleRepository extends JpaRepository<Role, Long> {
}
