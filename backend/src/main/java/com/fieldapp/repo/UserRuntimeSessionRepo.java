package com.fieldapp.repo;
import com.fieldapp.model.UserRuntimeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRuntimeSessionRepo extends JpaRepository<UserRuntimeSession, Long> { Optional<UserRuntimeSession> findByUsername(String username); }
