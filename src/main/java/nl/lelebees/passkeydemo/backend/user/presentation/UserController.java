package nl.lelebees.passkeydemo.backend.user.presentation;

import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtUserDetails;
import nl.lelebees.passkeydemo.backend.user.application.UserService;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDetailsUpdateDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserOverviewDto;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal JwtUserDetails userDetails) {
        try {
            return ResponseEntity.ok(service.getUserById(userDetails.getId()));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(CONFLICT, "You claim to be %s, but that e-mail address is not registered." + userDetails.getUsername());
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UserOverviewDto> updateDetails(@AuthenticationPrincipal JwtUserDetails userDetails, @RequestBody UserDetailsUpdateDto newDetails) {
        try {
            return ResponseEntity.ok(service.updateDetails(userDetails.getUser(), newDetails));
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not in a valid email format".formatted(newDetails.email()));
        }
    }
}
