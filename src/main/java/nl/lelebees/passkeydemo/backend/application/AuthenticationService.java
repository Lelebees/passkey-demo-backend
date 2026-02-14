package nl.lelebees.passkeydemo.backend.application;

import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialRpEntity;
import com.webauthn4j.data.PublicKeyCredentialUserEntity;
import com.webauthn4j.data.client.challenge.Challenge;
import nl.lelebees.passkeydemo.backend.domain.SimpleChallenge;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthenticationService {

    public PublicKeyCredentialCreationOptions getOptions () {
        var rp = new PublicKeyCredentialRpEntity("me?");
        var user = new PublicKeyCredentialUserEntity();
        var options = new PublicKeyCredentialCreationOptions(rp, user, createChallenge(), new ArrayList<>());
    }

    public Challenge createChallenge() {
        return SimpleChallenge.random();
    }
}
