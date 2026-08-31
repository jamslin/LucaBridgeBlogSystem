package com.lucabridge.core.company;

import com.lucabridge.core.company.dto.CompanyDto;
import com.lucabridge.core.i18n.Lang;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public read. Singleton — GET only, no {id} in the path. */
@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public CompanyDto get(@RequestParam(name = "lang", required = false) String rawLang) {
        Lang lang = Lang.orDefault(rawLang);
        return CompanyMapper.toDto(companyService.get(), lang);
    }
}
