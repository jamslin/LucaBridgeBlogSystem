package com.lucabridge.core.company;

import com.lucabridge.core.company.dto.AdminCompanyDto;
import com.lucabridge.core.company.dto.CompanyUpsertRequest;
import com.lucabridge.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Singleton — GET and PUT only, no POST, no DELETE, no /{id}. ADMIN-only per its own
 * SecurityConfig rule (not the general ADMIN-or-EDITOR /api/admin/** one): this holds the
 * charity registration number, the org name and the contact details shown on every page.
 */
@RestController
@RequestMapping("/api/admin/company")
public class AdminCompanyController {

    private final CompanyService companyService;
    private final CurrentUser currentUser;

    public AdminCompanyController(CompanyService companyService, CurrentUser currentUser) {
        this.companyService = companyService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public AdminCompanyDto get() {
        return CompanyMapper.toAdminDto(companyService.get());
    }

    @PutMapping
    public AdminCompanyDto update(@Valid @RequestBody CompanyUpsertRequest request) {
        return CompanyMapper.toAdminDto(companyService.update(request, currentUser.id()));
    }
}
