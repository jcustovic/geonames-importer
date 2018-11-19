package com.logimos.nutsimporter.infrastructure.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "MUNICIPALITIES")
public class MunicipalityEntity {

    @Id
    @SequenceGenerator(name = "MUNICIPALITIES_SEQ", sequenceName = "MUNICIPALITIES_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MUNICIPALITIES_SEQ")
    @Column(name = "M_ID")
    private Long id;

    @Column(name = "M_NAME", nullable = false)
    private String name;

    @Column(name = "M_COUNTRY_CODE", nullable = false)
    private String countryCodeIso2;

    @Column(name = "M_NUTS_CODE", nullable = false)
    private String nutsCode;

    @Column(name = "M_G_ID")
    private Long geonamesId;

    @Override
    public String toString() {
        return "MunicipalityEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", countryCodeIso2='" + countryCodeIso2 + '\'' +
                ", nutsCode='" + nutsCode + '\'' +
                ", geonamesId=" + geonamesId +
                '}';
    }

    // Getters & setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCodeIso2() {
        return countryCodeIso2;
    }

    public void setCountryCodeIso2(String countryCodeIso2) {
        this.countryCodeIso2 = countryCodeIso2;
    }

    public String getNutsCode() {
        return nutsCode;
    }

    public void setNutsCode(String nutsCode) {
        this.nutsCode = nutsCode;
    }

    public Long getGeonamesId() {
        return geonamesId;
    }

    public void setGeonamesId(Long geonamesId) {
        this.geonamesId = geonamesId;
    }
}
