package com.solightingstats.git.statistics.plugin.model;

public class Contributor implements Comparable<Contributor>{
    private String name;
    private String email;
    private Integer contributionsCount;

    public Contributor() {
        this.contributionsCount = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public int compareTo(Contributor contributor) {
        if (contributor.getContributionsCount() > this.getContributionsCount()) 
            return 1;
        
        if (contributor.getContributionsCount() < this.getContributionsCount())
            return -1;
        
        return 0;
    }
}