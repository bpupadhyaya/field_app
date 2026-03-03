package com.fieldapp.repo;
import com.fieldapp.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditEventRepo extends JpaRepository<AuditEvent, Long> {}
