package com.lucabridge.core.company;

import com.lucabridge.core.company.dto.CompanyUpsertRequest;
import com.lucabridge.core.error.BadRequestException;
import com.lucabridge.core.media.Media;
import com.lucabridge.core.media.MediaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final MediaRepository mediaRepository;

    public CompanyService(CompanyRepository companyRepository, MediaRepository mediaRepository) {
        this.companyRepository = companyRepository;
        this.mediaRepository = mediaRepository;
    }

    /** The single row always exists — seeded by V2__reference_data.sql before anything can update it. */
    @Transactional(readOnly = true)
    public Company get() {
        return companyRepository.findSingleton()
                .orElseThrow(() -> new IllegalStateException(
                        "company row (id=1) is missing — V2__reference_data.sql should have seeded it"));
    }

    /** No create/delete path, so no code-uniqueness race to guard against — this is always an UPDATE on the row from get(). */
    @Transactional
    public Company update(CompanyUpsertRequest req, Long currentUserId) {
        Company company = get();
        company.setCharityRegNo(req.charityRegNo());
        company.setFoundedYear(req.foundedYear());
        company.setPhone(req.phone());
        company.setEmail(req.email());
        applyLogo(company, req.logoMediaId());
        company.setInstagramUrl(req.instagramUrl());
        company.setFacebookUrl(req.facebookUrl());
        company.setYoutubeUrl(req.youtubeUrl());
        company.setUpdatedBy(currentUserId);
        applyText(company, req);
        return company;
    }

    private void applyLogo(Company company, Long logoMediaId) {
        if (logoMediaId == null) {
            company.setLogoMedia(null);
            return;
        }
        Media media = mediaRepository.findById(logoMediaId)
                .orElseThrow(() -> new BadRequestException("Unknown media: " + logoMediaId));
        company.setLogoMedia(media);
    }

    private void applyText(Company company, CompanyUpsertRequest req) {
        CompanyText text = company.getText();
        if (text == null) {
            text = new CompanyText();
            company.setText(text);
            text.setCompany(company);
        }
        text.setTcName(req.tcName());
        text.setEnName(req.enName());
        text.setScName(req.scName());
        text.setTcTagline(req.tcTagline());
        text.setEnTagline(req.enTagline());
        text.setScTagline(req.scTagline());
        text.setTcAbout(req.tcAbout());
        text.setEnAbout(req.enAbout());
        text.setScAbout(req.scAbout());
        text.setTcAddress(req.tcAddress());
        text.setEnAddress(req.enAddress());
        text.setScAddress(req.scAddress());
        text.setTcOfficeHours(req.tcOfficeHours());
        text.setEnOfficeHours(req.enOfficeHours());
        text.setScOfficeHours(req.scOfficeHours());
    }
}
