package com.lucabridge.core.homeblock;

import com.lucabridge.core.homeblock.dto.AdminHomeBlockDto;
import com.lucabridge.core.homeblock.dto.HomeBlockUpsertRequest;
import com.lucabridge.core.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Every route here is under /api/admin/home-blocks/**, which SecurityConfig restricts to ADMIN/EDITOR (DELETE further narrowed to ADMIN-only). */
@RestController
@RequestMapping("/api/admin/home-blocks")
public class AdminHomeBlockController {

    private final HomeBlockService homeBlockService;
    private final CurrentUser currentUser;

    public AdminHomeBlockController(HomeBlockService homeBlockService, CurrentUser currentUser) {
        this.homeBlockService = homeBlockService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AdminHomeBlockDto> list() {
        return homeBlockService.listAll().stream().map(HomeBlockMapper::toAdminDto).toList();
    }

    @GetMapping("/{id}")
    public AdminHomeBlockDto get(@PathVariable Long id) {
        return HomeBlockMapper.toAdminDto(homeBlockService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminHomeBlockDto create(@Valid @RequestBody HomeBlockUpsertRequest request) {
        return HomeBlockMapper.toAdminDto(homeBlockService.create(request, currentUser.id()));
    }

    @PutMapping("/{id}")
    public AdminHomeBlockDto update(@PathVariable Long id, @Valid @RequestBody HomeBlockUpsertRequest request) {
        return HomeBlockMapper.toAdminDto(homeBlockService.update(id, request, currentUser.id()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        homeBlockService.delete(id);
    }
}
