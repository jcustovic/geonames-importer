package com.logimos.nutsimporter.infrastructure.repository;

import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.logimos.nutsimporter.infrastructure.jpa.model.GeoNameEntity;
import com.logimos.nutsimporter.infrastructure.jpa.repository.GeoNameJpaRepository;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import cz.jirutka.unidecode.Unidecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GeoNameRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNameRepository.class);
    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    private final GeoNameJpaRepository jpaRepository;

    public GeoNameRepository(GeoNameJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public GeoNameEntity findMunicipalityForPostalCode(GeoNamesPostalCode geoName) {
        String countryCode = geoName.getCountryIso2();
        Point point = GF.createPoint(new Coordinate(geoName.getLongitude().doubleValue(), geoName.getLatitude().doubleValue()));
        String placeName = getPlaceName(geoName);
        String placeNameAscii = Unidecode.toAscii().decode(placeName);

        // TODO: Different for different countries
        String parentPlace = getParentPlace(geoName);
        String parentPlaceAscii = Unidecode.toAscii().decode(parentPlace);

        List<GeoNameEntity> results = jpaRepository.findByNameWithinDistance(countryCode, parentPlace, parentPlaceAscii, point);
        if (results.isEmpty()) {
            results = jpaRepository.findByNameWithinDistance(countryCode, placeName, placeNameAscii, point);
        }
        GeoNameEntity place = null;
        if (results.isEmpty()) {
            LOGGER.info("Place {} not found", geoName);
        } else if (results.size() > 2) {
            List<GeoNameEntity> placesWithReference = results.stream().filter(gn -> StringUtils.hasText(gn.getAdmin2Code())).collect(Collectors.toList());
            if (placesWithReference.isEmpty()) {
                LOGGER.info("Place {} not found after multi result", geoName);
            } else if (placesWithReference.size() > 1) {
                LOGGER.info("Multiple places found for {}", geoName);
            } else {
                place = placesWithReference.get(0);
            }
        } else if (results.size() == 2) {
            // TODO: Usually the place itself and administrative area
            place = results.get(0);
        } else {
            place = results.get(0);
        }
        if (place == null) {
            List<GeoNameEntity> adminAreas = jpaRepository.findClosestAdminAreas(countryCode, point);
            if (adminAreas.isEmpty()) {
                LOGGER.info("Cannot find closest admin area for {}", geoName);
            } else if (adminAreas.size() > 1) {
                LOGGER.info("Multiple closest admin area found for {}", geoName);
                place = adminAreas.get(0);
            } else {
                place = adminAreas.get(0);
            }
        }

        if (place != null) {
            GeoNameEntity municipality = findMunicipality(place);
            if (municipality == null) {
                LOGGER.info("Municipality not found for {}", geoName);
            } else {
                return municipality;
            }
        }

        return null;
    }

    private String getParentPlace(GeoNamesPostalCode geoName) {
        switch (geoName.getCountryIso2()) {
            case "DE":
                return geoName.getPlaceName();
        }
        
        return geoName.getAdminName3();
    }

    private String getPlaceName(GeoNamesPostalCode geoName) {
        switch (geoName.getCountryIso2()) {
            case "DE":
                return geoName.getAdminName1();
        }

        return geoName.getPlaceName();
    }

    private GeoNameEntity findMunicipality(GeoNameEntity place) {
        switch (place.getFeatureClass()) {
            case "A": {
                return place;
            }
            case "P": {
                if (StringUtils.hasText(place.getAdmin2Code())) {
                    Optional<GeoNameEntity> result = jpaRepository.findById(Long.valueOf(place.getAdmin2Code()));
                    return result.orElse(findFirstAdminAdreasForFips(place));
                } else {
                    return findFirstAdminAdreasForFips(place);
                }
            }
            default:
                LOGGER.info("Unknown place {}", place);
        }
        return null;
    }

    private GeoNameEntity findFirstAdminAdreasForFips(GeoNameEntity place) {
        // TODO: Maybe look better and not just take 1st
        return jpaRepository.findAdminAreasForFips(place.getCountryCodeIso2(), place.getFipsCode()).get(0);
    }
}
