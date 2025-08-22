package com.pm.team1_dash_v.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "flags")
public class Flaging {
    @Id
    private String flagId;
    private String messageId; // reference to Message
    private List<String> comments; // policy comments

    // getters and setters
}
