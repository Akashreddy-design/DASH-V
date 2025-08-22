package com.pm.team1_dash_v.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "groups")

public class Grouping {
    @Id
    private String id;
    private String channelName;
    private List<String> userIds; // list of users in this group
    private List<String> attachments;
    // getters and setters
}