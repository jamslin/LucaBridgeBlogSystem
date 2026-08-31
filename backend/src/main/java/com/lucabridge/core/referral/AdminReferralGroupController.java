package com.lucabridge.core.referral;

import com.lucabridge.core.referral.dto.AdminReferralGroupDto;
import com.lucabridge.core.referral.dto.ReferralGroupUpsertRequest;
import com.lucabridge.core.referral.dto.ReferralGroupUsageDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every route here is under /api/admin/referral-groups/**, which SecurityConfig restricts to
 * ADMIN/EDITOR (DELETE further narrowed to ADMIN-only). No created_by/updated_by on this table
 * — it's a bare reference table — so no CurrentUser dependency here.
 */
@RestController
@RequestMapping("/api/admin/referral-groups")
public class AdminReferralGroupController {

    private final ReferralGroupService referralGroupService;

    public AdminReferralGroupController(ReferralGroupService referralGroupService) {
        this.referralGroupService = referralGroupService;
    }

    @GetMapping
    public List<AdminReferralGroupDto> list() {
        return referralGroupService.listAll().stream().map(ReferralGroupMapper::toAdminDto).toList();
    }

    @GetMapping("/{id}")
    public AdminReferralGroupDto get(@PathVariable Long id) {
        return ReferralGroupMapper.toAdminDto(referralGroupService.get(id));
    }

    /** What the delete confirmation dialog shows before removing the referral group. */
    @GetMapping("/{id}/usage")
    public ReferralGroupUsageDto usage(@PathVariable Long id) {
        return new ReferralGroupUsageDto(referralGroupService.usage(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminReferralGroupDto create(@Valid @RequestBody ReferralGroupUpsertRequest request) {
        return ReferralGroupMapper.toAdminDto(referralGroupService.create(request));
    }

    @PutMapping("/{id}")
    public AdminReferralGroupDto update(@PathVariable Long id, @Valid @RequestBody ReferralGroupUpsertRequest request) {
        return ReferralGroupMapper.toAdminDto(referralGroupService.update(id, request));
    }

    /** Refuses with 409 (reference count in the message) unless confirm=true when the group is still in use — see ReferralGroupService.delete. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm) {
        referralGroupService.delete(id, confirm);
    }
}
