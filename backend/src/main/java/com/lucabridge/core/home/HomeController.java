package com.lucabridge.core.home;

import com.lucabridge.core.blog.BlogController;
import com.lucabridge.core.company.CompanyController;
import com.lucabridge.core.event.EventController;
import com.lucabridge.core.home.dto.HomePageDto;
import com.lucabridge.core.homeblock.HomeBlockController;
import com.lucabridge.core.service.ServiceController;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * One call for the busiest page on the site instead of five. The frontend is React Router 7
 * SSR calling this backend over HTTP — every one of those five calls is a real server-to-server
 * round trip on the critical path of every render, not a browser request that could ride
 * HTTP/2 multiplexing or a client-side cache. This is a thin orchestrator: it calls the same
 * public controllers /api/home-blocks, /api/events, /api/blog, /api/services and /api/company
 * already expose, so those endpoints stay exactly as useful for their own dedicated pages —
 * this is pure addition, not a replacement.
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private static final int UPCOMING_EVENTS_LIMIT = 5;
    private static final int LATEST_POSTS_LIMIT = 5;

    private final HomeBlockController homeBlockController;
    private final EventController eventController;
    private final BlogController blogController;
    private final ServiceController serviceController;
    private final CompanyController companyController;

    public HomeController(HomeBlockController homeBlockController, EventController eventController,
                           BlogController blogController, ServiceController serviceController,
                           CompanyController companyController) {
        this.homeBlockController = homeBlockController;
        this.eventController = eventController;
        this.blogController = blogController;
        this.serviceController = serviceController;
        this.companyController = companyController;
    }

    @GetMapping
    public HomePageDto get(@RequestParam(name = "lang", required = false) String lang) {
        return new HomePageDto(
                homeBlockController.blocks(lang),
                eventController.list(lang, 0, UPCOMING_EVENTS_LIMIT).getContent(),
                blogController.list(lang, PageRequest.of(0, LATEST_POSTS_LIMIT, Sort.by(Sort.Direction.DESC, "publishedAt"))).getContent(),
                serviceController.list(lang),
                companyController.get(lang));
    }
}
