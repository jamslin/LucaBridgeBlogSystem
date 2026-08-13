package com.lucabridge.blog.dto;

public record HomepageBannerDto(Long id, String imageUrl, String linkUrl, int sortOrder,
                                String title, String subtitle, String buttonLabel) {}
