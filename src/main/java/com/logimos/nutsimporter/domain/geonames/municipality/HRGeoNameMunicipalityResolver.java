package com.logimos.nutsimporter.domain.geonames.municipality;

import com.logimos.nutsimporter.domain.Municipality;
import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.logimos.nutsimporter.infrastructure.jpa.model.GeoNameEntity;
import com.logimos.nutsimporter.infrastructure.jpa.repository.jpa.GeoNameJpaRepository;
import com.vividsolutions.jts.geom.Point;
import cz.jirutka.unidecode.Unidecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HRGeoNameMunicipalityResolver implements GeoNameMunicipalityResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(HRGeoNameMunicipalityResolver.class);

    private static final String MINIMAL_ADMIN_LVL = "ADM2";

    private final GeoNameJpaRepository jpaRepository;

    public HRGeoNameMunicipalityResolver(GeoNameJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean canResolve(String countryCodeIso2) {
        return "HR".equals(countryCodeIso2);
    }

    @Override
    public Municipality resolveMunicipality(GeoNamesPostalCode geoName, Point point) {
        String countryCode = geoName.getCountryIso2();
        String placeName = geoName.getPlaceName();
        String placeNameAscii = Unidecode.toAscii().decode(placeName);

        String parentPlace = geoName.getAdminName3();
        String parentPlaceAscii = Unidecode.toAscii().decode(parentPlace);

        List<GeoNameEntity> results = jpaRepository.findByNameWithinDistance(countryCode, parentPlace, parentPlaceAscii, MINIMAL_ADMIN_LVL, point);
        if (results.isEmpty()) {
            results = jpaRepository.findByNameWithinDistance(countryCode, placeName, placeNameAscii, MINIMAL_ADMIN_LVL, point);
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
            List<GeoNameEntity> adminAreas = jpaRepository.findClosestAdminAreas(countryCode, MINIMAL_ADMIN_LVL, point);
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
                return new Municipality(municipality.getName());
            }
        }

        return null;
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
        if (StringUtils.hasText(place.getFipsCode())) {
            return jpaRepository.findAdminAreasByFips(place.getCountryCodeIso2(), place.getFipsCode(), MINIMAL_ADMIN_LVL).get(0);
        } else {
            return jpaRepository.findClosestAdminAreas(place.getCountryCodeIso2(), MINIMAL_ADMIN_LVL, place.getLocation()).get(0);
        }
    }
}
