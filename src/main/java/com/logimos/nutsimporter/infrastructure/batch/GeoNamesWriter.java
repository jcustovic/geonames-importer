package com.logimos.nutsimporter.infrastructure.batch;

import com.logimos.nutsimporter.infrastructure.jpa.model.GeoNameEntity;
import com.logimos.nutsimporter.infrastructure.jpa.repository.jpa.GeoNameJpaRepository;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;

@Component
public class GeoNamesWriter implements ItemWriter<FieldSet> {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    @Autowired
    private GeoNameJpaRepository jpaRepository;

    @Override
    public void write(List<? extends FieldSet> geonameLines) {
        geonameLines.forEach(g -> {
            String countryCode = g.readString("countryCode");
            String name = g.readString("name");
            boolean ignore = shouldIgnoreCountrySpecific(name, countryCode);

            BigDecimal latitude = new BigDecimal(g.readString("latitude"));
            BigDecimal longitude = new BigDecimal(g.readString("longitude"));
            Point point = GF.createPoint(new Coordinate(longitude.doubleValue(), latitude.doubleValue()));

            GeoNameEntity geoNameEntity = new GeoNameEntity();
            geoNameEntity.setId(g.readLong("geonameid"));
            geoNameEntity.setName(name);
            geoNameEntity.setAsciiName(g.readString("asciiName"));
            geoNameEntity.setAlternateNames(g.readString("alternateNames"));
            geoNameEntity.setLatitude(latitude);
            geoNameEntity.setLongitude(longitude);
            geoNameEntity.setCountryCodeIso2(countryCode);
            geoNameEntity.setFeatureClass(g.readString("featureClass"));
            geoNameEntity.setFeatureCode(g.readString("featureCode"));
            geoNameEntity.setFipsCode(g.readString("admin1Code"));
            geoNameEntity.setAdmin2Code(g.readString("admin2Code"));
            geoNameEntity.setAdmin3Code(g.readString("admin3Code"));
            geoNameEntity.setAdmin4Code(g.readString("admin4Code"));
            geoNameEntity.setModificationDate(g.readDate("modificationDate").toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            geoNameEntity.setLocation(point);
            geoNameEntity.setIgnore(ignore);

            jpaRepository.save(geoNameEntity);
            
            /*SqlParameterSource parameters = new MapSqlParameterSource()
                    .addValue("gId", g.readInt("geonameid"))
                    .addValue("name", name)
                    .addValue("asciiName", g.readString("asciiName"))
                    .addValue("alternateNames", g.readString("alternateNames"))
                    .addValue("latitude", latitude)
                    .addValue("longitude", longitude)
                    .addValue("countryCode", countryCode)
                    .addValue("featureClass", g.readString("featureClass"))
                    .addValue("featureCode", g.readString("featureCode"))
                    .addValue("fipsCode", g.readString("admin1Code"))
                    .addValue("admin2Code", g.readString("admin2Code"))
                    .addValue("admin3Code", g.readString("admin3Code"))
                    .addValue("admin4Code", g.readString("admin4Code"))
                    .addValue("modificationDate", g.readDate("modificationDate"))
                    .addValue("geomPoint", writer.write(point))
                    .addValue("ignore", ignore);

            jdbcTemplate.update("INSERT INTO GEONAMES(G_ID, G_NAME, G_ASCIINAME, G_ALTERNATENAMES, G_LATITUDE, G_LONGITUDE," +
                    " G_FEATURE_CLASS, G_FEATURE_CODE, G_COUNTRY_CODE, G_FIPS_CODE, G_ADMIN2_CODE, G_ADMIN3_CODE, G_ADMIN4_CODE," +
                    " G_MODIFICATION_DATE, G_GEOM, G_IGNORE) VALUES (:gId, :name, :asciiName, :alternateNames, :latitude," +
                    " :longitude, :featureClass, :featureCode, :countryCode, :fipsCode, :admin2Code, :admin3Code, :admin4Code, " +
                    " :modificationDate, :geomPoint, :ignore)", parameters);*/
        });
    }

    private boolean shouldIgnoreCountrySpecific(String name, String countryCode) {
        if ("HR".equals(countryCode)) {
            return (name.startsWith("Gradska cetvrt") || name.startsWith("Gradska četvrt"));
        }

        return false;
    }
}
