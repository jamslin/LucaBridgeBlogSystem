package com.lucabridge.core.referral;

import com.lucabridge.core.i18n.Lang;
import com.lucabridge.core.referral.dto.ReferralGroupDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public read API — active referral groups only, in sort_order. Feeds the registration form's "how did you hear about us" dropdown. */
@RestController
@RequestMapping("/api/referral-groups")
public class ReferralGroupController {

    private final ReferralGroupService referralGroupService;

    public ReferralGroupController(ReferralGroupService referralGroupService) {
        this.referralGroupService = referralGroupService;
    }

    @GetMapping
    public List<ReferralGroupDto> list(@RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        return referralGroupService.listActive().stream().map(g -> ReferralGroupMapper.toDto(g, lang)).toList();
    }
}
