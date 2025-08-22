package com.pm.team1_dash_v.model;

import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "messages")
public class Message {

    @Id
    private String id;

    @DBRef
    @NotNull
    private User userId; // referencing users collection, must exist

    @DBRef
    private Grouping groupId; // optional, can be null

    @DBRef
    private Flaging flagId; // optional

    @NotNull
    @NotBlank
    private String tenantId; // must exist

    @NotNull
    @NotBlank
    private String channel; // must exist

    @NotNull
    @NotBlank
    private String data; // must exist

    @NotNull
    private Date timestamp; // must exist
}
