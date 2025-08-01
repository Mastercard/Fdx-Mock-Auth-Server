package com.mastercard.fdx.mock.oauth2.server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oauth2_registered_client_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2RegisteredClientFDX {

    @Id
    @Column(length = 100)
    private String id;

    @Column(length = 1000)
    private String clientUri;

    @Column(length = 5000)
    private String contacts;

    @Column(length = 5000)
    private String description;

    @Column(length = 1000)
    private String durationType;

    @Column(length = 4)
    private String durationPeriod;

    @Column(length = 4)
    private String lookbackPeriod;

    @Column(length = 1000)
    private String logoUri;

    @Column(columnDefinition = "VARCHAR")
    private String registryReferences; // JSON stored as plain string

    @Column(columnDefinition = "VARCHAR")
    private String intermediaries; // JSON stored as plain string
}
