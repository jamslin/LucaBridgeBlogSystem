package com.lucabridge.blog.repository;

import com.lucabridge.blog.entity.HomepageBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HomepageBannerRepository extends JpaRepository<HomepageBanner, Long> {
    List<HomepageBanner> findAllByOrderBySortOrderAscIdAsc();
}
