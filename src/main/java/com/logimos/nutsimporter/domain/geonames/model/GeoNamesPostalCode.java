package com.logimos.nutsimporter.domain.geonames.model;

import java.math.BigDecimal;

/**
 * Readme for GeoNames Postal Code files :
 *
 * allCountries.zip: all countries, for the UK only the outwards codes, the UK total codes are in GB_full.csv.zip 
 * GB_full.csv.zip the full codes for the UK, ca 1.7 mio rows
 * <iso countrycode>: country specific subset also included in allCountries.zip
 * This work is licensed under a Creative Commons Attribution 3.0 License.
 * This means you can use the dump as long as you give credit to geonames (a link on your website to www.geonames.org is ok)
 * see http://creativecommons.org/licenses/by/3.0/
 * UK (GB_full.csv.zip): Contains Royal Mail data Royal Mail copyright and database right 2017.
 * The Data is provided "as is" without warranty or any representation of accuracy, timeliness or completeness.
 *
 * This readme describes the GeoNames Postal Code dataset.
 * The main GeoNames gazetteer data extract is here: http://download.geonames.org/export/dump/
 *
 *
 * For many countries lat/lng are determined with an algorithm that searches the place names in the main geonames database 
 * using administrative divisions and numerical vicinity of the postal codes as factors in the disambiguation of place names. 
 * For postal codes and place name for which no corresponding toponym in the main geonames database could be found an average 
 * lat/lng of 'neighbouring' postal codes is calculated.
 * Please let us know if you find any errors in the data set. Thanks
 *
 * For Canada we have only the first letters of the full postal codes (for copyright reasons)
 *
 * For Ireland we have only the first letters of the full postal codes (for copyright reasons)
 *
 * For Malta we have only the first letters of the full postal codes (for copyright reasons)
 *
 * The Argentina data file contains the first 5 positions of the postal code.
 *
 * For Brazil only major postal codes are available (only the codes ending with -000 and the major code per municipality).
 *
 * The data format is tab-delimited text in utf8 encoding, with the following fields :
 */
public class GeoNamesPostalCode {
    
    private String countryIso2; // iso country code, 2 characters
    private String postalCode; // varchar(20)
    private String placeName; // varchar(20)
    private String adminName1; // 1. order subdivision (state) varchar(100)
    private String adminCode1; // 1. order subdivision (state) varchar(20)
    private String adminName2; // 2. order subdivision (county/province) varchar(100)
    private String adminCode2; // 2. order subdivision (county/province) varchar(20)
    private String adminName3; // 3. order subdivision (community) varchar(100)
    private String adminCode3; // 3. order subdivision (community) varchar(20)
    private BigDecimal latitude; // estimated latitude (wgs84)
    private BigDecimal longitude; // estimated longitude (wgs84)
    private Integer accuracy; // accuracy of lat/lng from 1=estimated to 6=centroid

    @Override
    public String toString() {
        return "GeoNamesPostalCode{" +
                "countryIso2='" + countryIso2 + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", placeName='" + placeName + '\'' +
                ", adminName1='" + adminName1 + '\'' +
                ", adminCode1='" + adminCode1 + '\'' +
                ", adminName2='" + adminName2 + '\'' +
                ", adminCode2='" + adminCode2 + '\'' +
                ", adminName3='" + adminName3 + '\'' +
                ", adminCode3='" + adminCode3 + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                '}';
    }

    public String getCountryIso2() {
        return countryIso2;
    }

    public void setCountryIso2(String countryIso2) {
        this.countryIso2 = countryIso2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAdminName1() {
        return adminName1;
    }

    public void setAdminName1(String adminName1) {
        this.adminName1 = adminName1;
    }

    public String getAdminCode1() {
        return adminCode1;
    }

    public void setAdminCode1(String adminCode1) {
        this.adminCode1 = adminCode1;
    }

    public String getAdminName2() {
        return adminName2;
    }

    public void setAdminName2(String adminName2) {
        this.adminName2 = adminName2;
    }

    public String getAdminCode2() {
        return adminCode2;
    }

    public void setAdminCode2(String adminCode2) {
        this.adminCode2 = adminCode2;
    }

    public String getAdminName3() {
        return adminName3;
    }

    public void setAdminName3(String adminName3) {
        this.adminName3 = adminName3;
    }

    public String getAdminCode3() {
        return adminCode3;
    }

    public void setAdminCode3(String adminCode3) {
        this.adminCode3 = adminCode3;
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

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }
}
