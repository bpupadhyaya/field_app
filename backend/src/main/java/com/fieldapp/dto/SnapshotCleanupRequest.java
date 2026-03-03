package com.fieldapp.dto;
public record SnapshotCleanupRequest(Integer keepMinCount, Integer maxAgeDays) {}
