package nl.lelebees.passkeydemo.backend.user.presentation;

import nl.lelebees.passkeydemo.backend.security.application.dto.DesiredMediumDto;
import nl.lelebees.passkeydemo.backend.security.application.dto.PublicKeyCredentialCreationOptionsDto;
import nl.lelebees.passkeydemo.backend.security.application.jwt.JwtUserDetails;
import nl.lelebees.passkeydemo.backend.security.presentation.AuthenticationController;
import nl.lelebees.passkeydemo.backend.user.application.UserService;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDetailsUpdateDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.user.application.dto.UserOverviewDto;
import nl.lelebees.passkeydemo.backend.user.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.user.domain.IncorrectEmailFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

@Controller
@RequestMapping("/users/me")
public class UserController {
    private final UserService service;
    private final AuthenticationController authenticationController;

    public UserController(UserService service, AuthenticationController authenticationController) {
        this.service = service;
        this.authenticationController = authenticationController;
    }

    @GetMapping
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal JwtUserDetails userDetails) {
        try {
            return ResponseEntity.ok(service.getUserById(userDetails.getId()));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(CONFLICT, "You claim to be %s, but that e-mail address is not registered." + userDetails.getUsername());
        }
    }

    @PutMapping
    public ResponseEntity<UserOverviewDto> updateDetails(@AuthenticationPrincipal JwtUserDetails userDetails, @RequestBody UserDetailsUpdateDto newDetails) {
        try {
            return ResponseEntity.ok(service.updateDetails(userDetails.getUser(), newDetails));
        } catch (IncorrectEmailFormatException e) {
            throw new ResponseStatusException(UNPROCESSABLE_CONTENT, "%s is not in a valid email format".formatted(newDetails.email()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<PublicKeyCredentialCreationOptionsDto> startAddPasskey(@AuthenticationPrincipal JwtUserDetails userDetails, @RequestBody DesiredMediumDto desiredMedium) {
        return authenticationController.startAddPasskey(userDetails, desiredMedium);
    }

    @PatchMapping("/add")
    public ResponseEntity<UserDto> addPasskey(@RequestBody String data, @RequestHeader("User-Agent") String userAgent, @AuthenticationPrincipal JwtUserDetails userDetails) {
        return authenticationController.addPasskey(data, userAgent, userDetails);
    }

    @DeleteMapping("/add")
    public ResponseEntity<String> cancelAdd(@AuthenticationPrincipal JwtUserDetails userDetails) {
        return authenticationController.cancelAdd(userDetails);
    }
}
