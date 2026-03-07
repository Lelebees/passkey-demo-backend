package nl.lelebees.passkeydemo.backend.jackson.config;

import com.webauthn4j.converter.jackson.WebAuthnJSONModule;
import com.webauthn4j.converter.util.ObjectConverter;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class JacksonConfig implements JsonMapperBuilderCustomizer {

    @Override
    public void customize(JsonMapper.Builder jsonMapperBuilder) {
        ObjectConverter converter = new ObjectConverter();
        jsonMapperBuilder.addModule(new WebAuthnJSONModule(converter));
    }
}
