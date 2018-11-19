package com.logimos.nutsimporter.infrastructure.jpa.repository.jpa;

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
            " AND (" +
            "      (G_FEATURE_CLASS = 'A' AND G_FEATURE_CODE = ?4)" +
            "  OR  G_FEATURE_CLASS = 'P'" +
            ")" +
            " AND G_IGNORE IS FALSE" +
            " AND ST_DISTANCE(?5, G_GEOM) < 0.3" + // radius cca 33km
            " ORDER BY ST_DISTANCE(?5, G_GEOM)", nativeQuery = true)
    List<GeoNameEntity> findByNameWithinDistance(String countryCode, String name, String asciiName, String featureCode, Point location);

    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND G_FIPS_CODE = ?2" +
            " AND G_FEATURE_CLASS = 'A' AND G_FEATURE_CODE = ?3" +
            " AND G_IGNORE IS FALSE" +
            " ORDER BY G_FEATURE_CODE ASC", nativeQuery = true)
    List<GeoNameEntity> findAdminAreasByFips(String countryCodeIso2, String fipsCode, String featureCode);

    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND G_ADMIN4_CODE = ?2" +
            " AND G_FEATURE_CLASS = 'A' AND G_FEATURE_CODE = ?3" +
            " AND G_IGNORE IS FALSE" +
            " ORDER BY G_FEATURE_CODE ASC", nativeQuery = true)
    List<GeoNameEntity> findAdminAreasByAdminCode(String countryCodeIso2, String adminCode, String featureCode);

    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND G_FEATURE_CODE = ?2" +
            " AND G_IGNORE IS FALSE" +
            " AND ST_DISTANCE(?3, G_GEOM) < 0.3" +
            " ORDER BY ST_DISTANCE(?3, G_GEOM)", nativeQuery = true)
    List<GeoNameEntity> findClosestAdminAreas(String countryCode, String featureCode, Point location);

    @Query(value = "SELECT * FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ?1" +
            " AND (" +
            "      G_FEATURE_CLASS = 'A' AND G_FEATURE_CODE = ?2" +
            "  OR  G_FEATURE_CLASS = 'P'" +
            ")" +
            " AND G_IGNORE IS FALSE" +
            " AND ST_DISTANCE(?3, G_GEOM) < 0.1" + // radius cca 11km
            " ORDER BY ST_DISTANCE(?3, G_GEOM)", nativeQuery = true)
    List<GeoNameEntity> findPlaceWithinSmallRange(String countryCode, String featureCode, Point location);
}
