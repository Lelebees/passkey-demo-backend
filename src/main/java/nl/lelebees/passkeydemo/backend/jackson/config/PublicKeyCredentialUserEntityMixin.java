package nl.lelebees.passkeydemo.backend.jackson.config;

import com.webauthn4j.converter.jackson.serializer.json.ByteArrayBase64UrlSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

public abstract class PublicKeyCredentialUserEntityMixin {
    @JsonSerialize(using = ByteArrayBase64UrlSerializer.class)
    public abstract byte[] getId();

}
