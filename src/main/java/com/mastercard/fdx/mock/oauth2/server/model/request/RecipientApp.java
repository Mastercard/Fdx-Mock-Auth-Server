package com.mastercard.fdx.mock.oauth2.server.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class RecipientApp {

    @JsonProperty("client_name")
    public String clientName;

    @JsonProperty("description")
    public String description;

    @JsonProperty("redirect_uris")
    public Set<String> redirectUris;

    @JsonProperty("logo_uri")
    public String logoUri;

    @JsonProperty("client_uri")
    public String clientUri;

    @JsonProperty("contacts")
    public List<String> contacts;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("duration_type")
    public List<String> durationType;

    @JsonProperty("duration_period")
    public Integer durationPeriod;

    @JsonProperty("lookback_period")
    public Integer lookbackPeriod;

    @JsonProperty("registry_references")
    public List<RegistryReference> registryReferences;

    @JsonProperty("intermediaries")
    public List<Intermediary> intermediaries;

}
