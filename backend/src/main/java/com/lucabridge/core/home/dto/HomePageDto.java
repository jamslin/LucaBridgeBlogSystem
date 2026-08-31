package com.lucabridge.core.home.dto;

import com.lucabridge.core.blog.dto.BlogSummaryDto;
import com.lucabridge.core.company.dto.CompanyDto;
import com.lucabridge.core.event.dto.EventSummaryDto;
import com.lucabridge.core.homeblock.HomeBlockSlot;
import com.lucabridge.core.homeblock.dto.HomeBlockDto;
import com.lucabridge.core.service.dto.ServiceDto;

import java.util.List;
import java.util.Map;

/**
 * Everything the home page needs in one SSR round trip: home_block content by slot, the
 * upcoming-events timeline (already carrying registration state per row), the latest posts,
 * the service chip row, and the company footer/contact block. See HomeController for why this
 * exists alongside the five endpoints it orchestrates rather than replacing them.
 */
public record HomePageDto(
        Map<HomeBlockSlot, List<HomeBlockDto>> homeBlocks,
        List<EventSummaryDto> upcomingEvents,
        List<BlogSummaryDto> latestPosts,
        List<ServiceDto> services,
        CompanyDto company) {
}
