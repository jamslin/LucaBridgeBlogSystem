package com.lucabridge.core.service.dto;

/** What the admin delete confirmation dialog shows before a service is actually removed. */
public record ServiceUsageDto(long blogCount, long eventCount) {
}
