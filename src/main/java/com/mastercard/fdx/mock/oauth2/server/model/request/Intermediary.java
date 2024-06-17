package com.mastercard.fdx.mock.oauth2.server.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Intermediary{

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("uri")
    public String uri;

    @JsonProperty("logo_uri")
    public String logoUri;

    @JsonProperty("contacts")
    public List<String> contacts;

    @JsonProperty("registry_references")
    public List<RegistryReference> registryReferences;
}