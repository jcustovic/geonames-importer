package com.logimos.nutsimporter.domain.geonames.municipality;

import com.logimos.nutsimporter.domain.Municipality;
import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.vividsolutions.jts.geom.Point;

public interface GeoNameMunicipalityResolver {

    boolean canResolve(String countryCodeIso2);

    Municipality resolveMunicipality(GeoNamesPostalCode geoName, Point point);
}
