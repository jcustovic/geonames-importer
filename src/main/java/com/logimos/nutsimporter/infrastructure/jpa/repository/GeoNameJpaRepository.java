package com.logimos.nutsimporter.infrastructure.jpa.repository;

import com.logimos.nutsimporter.infrastructure.jpa.model.GeoNameEntity;
import com.vividsolutions.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GeoNameJpaRepository extends JpaRepository<GeoNameEntity, Long> {

    // TODO: Also search alt names / full text search
    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND (G_NAME = ?2 OR G_ASCIINAME = ?3)" +
            " AND G_FEATURE_CLASS IN ('P', 'A')" +
            " AND G_IGNORE IS FALSE" +
            " AND ST_DISTANCE(?4, G_GEOM) < 0.3" + // radius cca 33km
            " ORDER BY ST_DISTANCE(?4, G_GEOM)", nativeQuery = true)
    List<GeoNameEntity> findByNameWithinDistance(String countryCode, String name, String asciiName, Point location);

    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND G_FIPS_CODE = ?2" +
            " AND G_FEATURE_CLASS IN ('A')" +
            " AND G_IGNORE IS FALSE" +
            " ORDER BY G_FEATURE_CODE ASC", nativeQuery = true)
    List<GeoNameEntity> findAdminAreasForFips(String countryCodeIso2, String fipsCode);

    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND G_FEATURE_CODE IN ('ADM1', 'ADM2', 'ADM3', 'ADM4')" +
            " AND G_IGNORE IS FALSE" +
            " AND ST_DISTANCE(?2, G_GEOM) < 0.3" +
            " ORDER BY ST_DISTANCE(?2, G_GEOM)", nativeQuery = true)
    List<GeoNameEntity> findClosestAdminAreas(String countryCode, Point location);

}
