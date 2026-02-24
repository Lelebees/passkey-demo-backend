package nl.lelebees.passkeydemo.backend.config;

import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Component
public class JacksonConfig implements JsonMapperBuilderCustomizer {

    @Override
    public void customize(JsonMapper.Builder jsonMapperBuilder) {
        jsonMapperBuilder.addMixIns(Map.of(PublicKeyCredentialCreationOptions.class, PublicKeyCredentialCreationOptionsMixin.class));
    }
}
