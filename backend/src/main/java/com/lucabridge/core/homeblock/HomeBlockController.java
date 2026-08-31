package com.lucabridge.core.homeblock;

import com.lucabridge.core.homeblock.dto.HomeBlockDto;
import com.lucabridge.core.i18n.Lang;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Public read — every visible block, every slot, in one call. React groups/renders by slot; this API just supplies content. */
@RestController
@RequestMapping("/api/home")
public class HomeBlockController {

    private final HomeBlockService homeBlockService;

    public HomeBlockController(HomeBlockService homeBlockService) {
        this.homeBlockService = homeBlockService;
    }

    @GetMapping("/blocks")
    public Map<HomeBlockSlot, List<HomeBlockDto>> blocks(@RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        return HomeBlockMapper.toGroupedDto(homeBlockService.listVisible(), lang);
    }
}
