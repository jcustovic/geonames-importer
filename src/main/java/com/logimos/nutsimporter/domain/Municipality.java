package com.logimos.nutsimporter.domain;

public class Municipality {
    
    private Long id;
    private String name;
    private String nutsCode;

    public Municipality(Long id, String name, String nutsCode) {
        this.id = id;
        this.name = name;
        this.nutsCode = nutsCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNutsCode() {
        return nutsCode;
    }
}
