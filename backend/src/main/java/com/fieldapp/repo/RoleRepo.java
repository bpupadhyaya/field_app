package com.fieldapp.repo;
import com.fieldapp.model.Role;
import com.fieldapp.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RoleRepo extends JpaRepository<Role, Long> { Optional<Role> findByName(RoleName name); }
