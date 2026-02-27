package nl.lelebees.passkeydemo.backend.user.presentation;

import nl.lelebees.passkeydemo.backend.security.application.jwt.UserDetailsImpl;
import nl.lelebees.passkeydemo.backend.user.application.UserService;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.domain.Email;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            return ResponseEntity.ok(service.getUserByEmail(new Email(userDetails.getUsername())));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(CONFLICT, "You claim to be %s, but that e-mail address is not registered." + userDetails.getUsername());
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(BAD_REQUEST, "You claim to be %s, but that is not a valid e-mail format".formatted(userDetails.getUsername()));
        }
    }
}
