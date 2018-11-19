package com.logimos.nutsimporter.domain.geonames.municipality;

import com.logimos.nutsimporter.domain.Municipality;
import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.logimos.nutsimporter.infrastructure.jpa.model.GeoNameEntity;
import com.logimos.nutsimporter.infrastructure.jpa.model.MunicipalityEntity;
import com.logimos.nutsimporter.infrastructure.jpa.repository.NutsRepository;
import com.logimos.nutsimporter.infrastructure.jpa.repository.jpa.GeoNameJpaRepository;
import com.logimos.nutsimporter.infrastructure.jpa.repository.jpa.MunicipalityJpaRepository;
import com.vividsolutions.jts.geom.Point;
import cz.jirutka.unidecode.Unidecode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DEGeoNameMunicipalityResolver implements GeoNameMunicipalityResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DEGeoNameMunicipalityResolver.class);

    private static final String LOWEST_ADMIN_LVL = "ADM4";

    private final GeoNameJpaRepository jpaRepository;
    private final MunicipalityJpaRepository municipalityJpaRepository;
    private final NutsRepository nutsRepository;

    public DEGeoNameMunicipalityResolver(GeoNameJpaRepository jpaRepository, MunicipalityJpaRepository municipalityJpaRepository,
                                         NutsRepository nutsRepository) {
        this.jpaRepository = jpaRepository;
        this.municipalityJpaRepository = municipalityJpaRepository;
        this.nutsRepository = nutsRepository;
    }

    @Override
    public boolean canResolve(String countryCodeIso2) {
        return "DE".equals(countryCodeIso2);
    }

    @Override
    public Municipality resolveMunicipality(GeoNamesPostalCode geoName, Point point) {
        String countryCode = geoName.getCountryIso2();
        String placeName = geoName.getPlaceName().split(",")[0];
        String placeNameAscii = Unidecode.toAscii().decode(placeName);

        Municipality municipality = resolveMunicipality(countryCode, placeName, point);
        if (municipality != null) {
            return municipality;
        }

        List<GeoNameEntity> results = jpaRepository.findByNameWithinDistance(countryCode, placeName, placeNameAscii, LOWEST_ADMIN_LVL, point);
        if (results.isEmpty()) {
            LOGGER.info("Place not found by name, searching closest... ({})", geoName);
            results = jpaRepository.findPlaceWithinSmallRange(countryCode, LOWEST_ADMIN_LVL, point);
            String lowerCasePlaceName = placeName.toLowerCase();
            results = results.stream()
                    .filter(r -> {
                                String lowerCaseName = r.getName().toLowerCase();
                                return lowerCasePlaceName.contains(lowerCaseName) || lowerCaseName.contains(lowerCasePlaceName)
                                        || FuzzySearch.ratio(lowerCasePlaceName, lowerCaseName) > 80;
                                //        || FuzzySearch.ratio(placeNameAscii, r.getName()) > 80
                            }
                    )
                    .collect(Collectors.toList());
        }

        if (results.isEmpty()) {
            LOGGER.warn("Place not found for {}. Searching closest admin area...", geoName);
            results = jpaRepository.findClosestAdminAreas(countryCode, LOWEST_ADMIN_LVL, point);
        }

        if (results.isEmpty()) {
            LOGGER.error("Place not found for {}.", geoName);
        } else {
            // In case that the result contains 2 entries it is probably because place is PPL and ADM so doesn't matter which we take
            GeoNameEntity place = results.get(0);
            results = jpaRepository.findAdminAreasByAdminCode(place.getCountryCodeIso2(), place.getAdmin4Code(), LOWEST_ADMIN_LVL);
            if (results.isEmpty()) {
                LOGGER.error("Admin area not found for {}", geoName);
            } else {
                GeoNameEntity adminArea = results.get(0);
                municipality = resolveMunicipality(adminArea.getCountryCodeIso2(), adminArea.getName(), adminArea.getLocation());
                if (municipality == null) {
                    LOGGER.info("Creating new municipality with name {} (geonameId: {})... -> caused by {}", adminArea.getName(),
                            adminArea.getId(), geoName);
                    municipality = createMunicipality(adminArea.getCountryCodeIso2(), adminArea.getName(), adminArea.getLocation(),
                            adminArea.getId());
                }

                return municipality;
            }
        }

        return null;
    }

    private Municipality resolveMunicipality(String countryCode, String placeName, Point point) {
        List<MunicipalityEntity> municipalities = municipalityJpaRepository.findByCountryCodeIso2AndName(countryCode, placeName);
        if (municipalities.size() == 1) {
            return map(municipalities.get(0));
        } else if (municipalities.size() > 1) {
            List<String> finalNutsCodes = findClosestNutsLvl3Codes(countryCode, point);
            municipalities = municipalities.stream().filter(m -> finalNutsCodes.contains(m.getNutsCode())).collect(Collectors.toList());
            if (municipalities.size() > 0) {
                if (municipalities.size() > 1) {
                    LOGGER.debug("Multiple municipalities found for name {} and closest nuts codes", placeName);
                }
                return map(municipalities.get(0));
            } else {
                LOGGER.debug("Municipality not found for name {} and closest nuts codes", placeName);
            }
        }

        List<String> nutsCodes = findClosestNutsLvl3Codes(countryCode, point);
        municipalities = municipalityJpaRepository.findByCountryCodeIso2AndNutsCodeIn(countryCode, nutsCodes);

        String lowerCasePlaceName = placeName.toLowerCase();

        return municipalities.stream()
                .filter(m -> {
                    String lowerCaseName = m.getName().toLowerCase();
                    return lowerCasePlaceName.contains(lowerCaseName) || lowerCaseName.contains(lowerCasePlaceName);
                }).findFirst().map(this::map)
                .orElse(municipalities.stream()
                        .map(m -> new ScoreObject<>(m, FuzzySearch.ratio(m.getName().toLowerCase(), lowerCasePlaceName)))
                        .filter(score -> score.getScore() > 65)
                        .max(Comparator.comparingInt(ScoreObject::getScore))
                        .map(m -> map(m.getObject())).orElse(null)
                );
    }

    private Municipality createMunicipality(String countryCodeIso2, String name, Point location, Long geonameId) {
        MunicipalityEntity entity = new MunicipalityEntity();
        entity.setName(name);
        entity.setGeonamesId(geonameId);
        entity.setCountryCodeIso2(countryCodeIso2);
        String nutsCode = nutsRepository.findNutsWithinBoundaries(countryCodeIso2, BigDecimal.valueOf(location.getX()),
                BigDecimal.valueOf(location.getY()));
        entity.setNutsCode(nutsCode);

        return map(municipalityJpaRepository.saveAndFlush(entity));
    }

    private Municipality map(MunicipalityEntity entity) {
        return new Municipality(entity.getId(), entity.getName(), entity.getNutsCode());
    }

    private List<String> findClosestNutsLvl3Codes(String countryCode, Point point) {
        List<String> nutsCodes = nutsRepository.findClosestNutsWithinRange(countryCode, BigDecimal.valueOf(point.getX()), BigDecimal.valueOf(point.getY()));

        return nutsCodes.stream().filter(n -> n.length() == 5).collect(Collectors.toList());
    }

    public class ScoreObject<T> {
        private T object;
        private int score;

        ScoreObject(T object, int score) {
            this.object = object;
            this.score = score;
        }

        T getObject() {
            return object;
        }

        int getScore() {
            return score;
        }
    }
}
