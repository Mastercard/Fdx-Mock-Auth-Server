package com.mastercard.fdx.mock.oauth2.server.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistryReference {

    @JsonProperty("registered_entity_name")
    public String registeredEntityName;

    @JsonProperty("registered_entity_id")
    public String registeredEntityId;

    @JsonProperty("registry")
    public String registry;
}