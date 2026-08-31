package com.lucabridge.core.user;

import com.lucabridge.core.user.dto.AdminUserDto;
import com.lucabridge.core.user.dto.ChangePasswordRequest;
import com.lucabridge.core.user.dto.CreateUserRequest;
import com.lucabridge.core.user.dto.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Every route here is ADMIN-only per SecurityConfig's /api/admin/users/** rule. */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<AdminUserDto> list() {
        return userService.list().stream().map(UserMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public AdminUserDto get(@PathVariable Long id) {
        return UserMapper.toDto(userService.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserDto create(@Valid @RequestBody CreateUserRequest request) {
        return UserMapper.toDto(userService.create(request));
    }

    @PutMapping("/{id}")
    public AdminUserDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserMapper.toDto(userService.update(id, request));
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.newPassword());
    }

    /** Hard delete, unlike content — an account has no reference-counted media to protect. Guarded against deleting the last enabled admin; see UserService. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
