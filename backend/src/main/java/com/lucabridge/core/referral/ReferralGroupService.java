package com.lucabridge.core.referral;

import com.lucabridge.core.error.ConflictException;
import com.lucabridge.core.error.ResourceNotFoundException;
import com.lucabridge.core.event.EventRegistrationRepository;
import com.lucabridge.core.referral.dto.ReferralGroupUpsertRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReferralGroupService {

    private final ReferralGroupRepository referralGroupRepository;
    private final EventRegistrationRepository registrationRepository;

    public ReferralGroupService(ReferralGroupRepository referralGroupRepository,
                                 EventRegistrationRepository registrationRepository) {
        this.referralGroupRepository = referralGroupRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReferralGroup> listActive() {
        return referralGroupRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<ReferralGroup> listAll() {
        return referralGroupRepository.findAllForAdmin();
    }

    @Transactional(readOnly = true)
    public ReferralGroup get(Long id) {
        return referralGroupRepository.findByIdWithText(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral group not found: " + id));
    }

    @Transactional
    public ReferralGroup create(ReferralGroupUpsertRequest req) {
        ReferralGroup group = ReferralGroup.builder()
                .code(req.code())
                .sortOrder(req.sortOrder() == null ? 0 : req.sortOrder())
                .active(req.active())
                .build();
        applyText(group, req);
        return save(group);
    }

    @Transactional
    public ReferralGroup update(Long id, ReferralGroupUpsertRequest req) {
        ReferralGroup group = get(id);
        group.setCode(req.code());
        group.setSortOrder(req.sortOrder() == null ? 0 : req.sortOrder());
        group.setActive(req.active());
        applyText(group, req);
        return save(group);
    }

    /** What the delete confirmation dialog needs — how many registrations would lose this referral, past and future. */
    @Transactional(readOnly = true)
    public long usage(Long id) {
        return registrationRepository.countByReferralGroupId(id);
    }

    /**
     * Doesn't touch event_registration.referral_group_id — the FK's ON DELETE SET NULL does
     * that automatically. Refuses without confirm=true when the count is nonzero, so a delete
     * can't silently blank the referral field on live registration records.
     */
    @Transactional
    public void delete(Long id, boolean confirm) {
        ReferralGroup group = get(id);
        long usage = usage(id);
        if (usage > 0 && !confirm) {
            throw new ConflictException("Referral group is referenced by " + usage
                    + " registration(s) — pass confirm=true to delete anyway");
        }
        referralGroupRepository.delete(group);
    }

    private void applyText(ReferralGroup group, ReferralGroupUpsertRequest req) {
        ReferralGroupText text = group.getText();
        if (text == null) {
            text = new ReferralGroupText();
            group.setText(text);
            text.setReferralGroup(group);
        }
        text.setTcName(req.tcName());
        text.setEnName(req.enName());
        text.setScName(req.scName());
    }

    /** Forces the flush so a code conflict surfaces here as a clean ConflictException — same deferred-write trap as ServiceService.save. */
    private ReferralGroup save(ReferralGroup group) {
        try {
            ReferralGroup saved = referralGroupRepository.save(group);
            referralGroupRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Referral group code already in use: " + group.getCode());
        }
    }
}
