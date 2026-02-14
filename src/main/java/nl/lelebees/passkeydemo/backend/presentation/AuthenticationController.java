package nl.lelebees.passkeydemo.backend.presentation;

import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.RegistrationData;
import nl.lelebees.passkeydemo.backend.application.dto.UserDto;
import nl.lelebees.passkeydemo.backend.application.exception.UserNotFoundException;
import nl.lelebees.passkeydemo.backend.application.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller("/authentication")
public class AuthenticationController {

    private final UserService service;

    public AuthenticationController(UserService service) {
        this.service = service;
    }


    @PostMapping("/challenge")
    public void getChallenge() {

    }

    @PostMapping("/register")
    public void register(RegistrationData data){

    }

    @PostMapping("/register-options")
    public PublicKeyCredentialCreationOptions getOptions() {

    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        try {
            return service.getUserById(id);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id: "+ id + " Could not be found");
        }
    }
}
