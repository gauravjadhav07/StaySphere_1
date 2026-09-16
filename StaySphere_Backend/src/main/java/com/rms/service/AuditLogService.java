package com.rms.service;

import com.rms.dtos.AuditLogResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void record(String actorEmail, String actorRole, String action, String targetType, Long targetId, String details);

    Page<AuditLogResponseDTO> getAuditLogs(Pageable pageable);
}