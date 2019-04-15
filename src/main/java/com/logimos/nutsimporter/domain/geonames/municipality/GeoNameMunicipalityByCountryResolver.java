package com.logimos.nutsimporter.domain.geonames.municipality;

import com.logimos.nutsimporter.domain.Municipality;
import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoNameMunicipalityByCountryResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNameMunicipalityByCountryResolver.class);

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
    private static final GeoNameMunicipalityResolver NO_RESOLVER = new NoResolverGeoName();

    private final List<GeoNameMunicipalityResolver> resolvers;

    public GeoNameMunicipalityByCountryResolver(List<GeoNameMunicipalityResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public Municipality findMunicipalityForPostalCode(GeoNamesPostalCode geoName) {
        Point point = GF.createPoint(new Coordinate(geoName.getLongitude().doubleValue(), geoName.getLatitude().doubleValue()));

        String countryCode = geoName.getCountryIso2();
        String placeName = geoName.getPlaceName().split(",")[0];

        return resolvers.stream().filter(r -> r.canResolve(countryCode)).findFirst().orElse(NO_RESOLVER)
                .resolveMunicipality(countryCode, placeName, point);
    }

    static class NoResolverGeoName implements GeoNameMunicipalityResolver {

        @Override
        public boolean canResolve(String countryCodeIso2) {
            return false;
        }

        @Override
        public Municipality resolveMunicipality(String countryCode, String placeName, Point point) {
            LOGGER.warn("No resolver found for {}", placeName);
            return null;
        }
    }
}