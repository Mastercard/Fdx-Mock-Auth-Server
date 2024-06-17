package org.springframework.security.oauth2.server.authorization.jackson2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
abstract class StringArrayMixin {

    @JsonCreator
    StringArrayMixin(String[] list) {
    }

}