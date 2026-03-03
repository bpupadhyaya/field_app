package com.fieldapp.repo;
import com.fieldapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AppUserRepo extends JpaRepository<AppUser, Long> { Optional<AppUser> findByUsername(String username); }
