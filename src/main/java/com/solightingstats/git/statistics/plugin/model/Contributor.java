package com.solightingstats.git.statistics.plugin.model;

public class Contributor {
    private String email;
    private Integer contributionsCount;

    public Contributor() {
        this.contributionsCount = 0;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getContributionsCount() {
        return contributionsCount;
    }

    public void addContribution() {
        ++contributionsCount;
    }
}