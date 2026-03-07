package nl.lelebees.passkeydemo.backend.user.application.dto;

import com.webauthn4j.data.PublicKeyCredentialDescriptor;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.util.Base64UrlUtil;
import nl.lelebees.passkeydemo.backend.user.domain.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String email, String displayName, Set<PasskeyDto> passkeys) {
    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getEmail().toString(), user.getDisplayName(), PasskeyDto.from(user.getPasskeys()));
    }

    public List<PublicKeyCredentialDescriptor> getPasskeysAsExclusionList() {
        return passkeys.stream()
                .map(passkey -> new PublicKeyCredentialDescriptor(PublicKeyCredentialType.PUBLIC_KEY, Base64UrlUtil.decode(passkey.id()), null))
                .toList();
    }
}
