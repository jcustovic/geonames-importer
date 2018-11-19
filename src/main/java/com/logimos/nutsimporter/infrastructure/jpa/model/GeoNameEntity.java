package com.logimos.nutsimporter.infrastructure.jpa.model;

import com.vividsolutions.jts.geom.Point;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "GEONAMES")
public class GeoNameEntity {

    @Id
    @Column(name = "G_ID")
    private Long id;

    @Column(name = "G_NAME", nullable = false)
    private String name;

    @Column(name = "G_ASCIINAME", nullable = false)
    private String asciiName;

    @Column(name = "G_ALTERNATENAMES")
    private String alternateNames;

    @Column(name = "G_LATITUDE", nullable = false)
    private BigDecimal latitude;

    @Column(name = "G_LONGITUDE", nullable = false)
    private BigDecimal longitude;

    @Column(name = "G_FEATURE_CLASS", nullable = false)
    private String featureClass;

    @Column(name = "G_FEATURE_CODE", nullable = false)
    private String featureCode;

    @Column(name = "G_COUNTRY_CODE", nullable = false)
    private String countryCodeIso2;

    @Column(name = "G_FIPS_CODE", nullable = false)
    private String fipsCode;

    @Column(name = "G_ADMIN2_CODE")
    private String admin2Code;

    @Column(name = "G_ADMIN3_CODE")
    private String admin3Code;

    @Column(name = "G_ADMIN4_CODE")
    private String admin4Code;

    @Column(name = "G_MODIFICATION_DATE", nullable = false)
    private LocalDate modificationDate;

    @Column(name = "G_GEOM", nullable = false)
    private Point location;

    @Column(name = "G_IGNORE", nullable = false)
    private Boolean ignore;

    @Override
    public String toString() {
        return "GeoNameEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", asciiName='" + asciiName + '\'' +
                ", alternateNames='" + alternateNames + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", featureClass='" + featureClass + '\'' +
                ", featureCode='" + featureCode + '\'' +
                ", countryCodeIso2='" + countryCodeIso2 + '\'' +
                ", fipsCode='" + fipsCode + '\'' +
                ", admin2Code='" + admin2Code + '\'' +
                ", admin3Code='" + admin3Code + '\'' +
                ", admin4Code='" + admin4Code + '\'' +
                ", modificationDate=" + modificationDate +
                ", location=" + location +
                ", ignore=" + ignore +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAsciiName() {
        return asciiName;
    }

    public void setAsciiName(String asciiName) {
        this.asciiName = asciiName;
    }

    public String getAlternateNames() {
        return alternateNames;
    }

    public void setAlternateNames(String alternateNames) {
        this.alternateNames = alternateNames;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public String getCountryCodeIso2() {
        return countryCodeIso2;
    }

    public void setCountryCodeIso2(String countryCodeIso2) {
        this.countryCodeIso2 = countryCodeIso2;
    }

    public String getFipsCode() {
        return fipsCode;
    }

    public void setFipsCode(String fipsCode) {
        this.fipsCode = fipsCode;
    }

    public String getAdmin2Code() {
        return admin2Code;
    }

    public void setAdmin2Code(String admin2Code) {
        this.admin2Code = admin2Code;
    }

    public String getAdmin3Code() {
        return admin3Code;
    }

    public void setAdmin3Code(String admin3Code) {
        this.admin3Code = admin3Code;
    }

    public String getAdmin4Code() {
        return admin4Code;
    }

    public void setAdmin4Code(String admin4Code) {
        this.admin4Code = admin4Code;
    }

    public LocalDate getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDate modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Boolean getIgnore() {
        return ignore;
    }

    public void setIgnore(Boolean ignore) {
        this.ignore = ignore;
    }
}
