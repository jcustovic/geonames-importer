package com.logimos.nutsimporter.infrastructure.batch;

import com.logimos.nutsimporter.domain.Municipality;
import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.logimos.nutsimporter.infrastructure.jpa.model.GeoNameEntity;
import com.logimos.nutsimporter.infrastructure.repository.GeoNameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeoNamesPostalCodeWriter implements ItemWriter<GeoNamesPostalCode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesPostalCodeWriter.class);

    final static String QUERY_WITHIN_NUTS = "SELECT NUTS_ID FROM NUTS_RG_01M_2016_4326" +
            " WHERE ST_DWITHIN(GEOM, ST_SetSRID(ST_MAKEPOINT(?, ?), 4326), 0.009) = TRUE" +
            " AND CNTR_CODE = ? AND LEVL_CODE = 3";

    final static String QUERY_CLOSEST_NUTS = "SELECT NUTS_ID FROM NUTS_RG_01M_2016_4326" +
            " WHERE CNTR_CODE = ?" +
            " ORDER BY LEVL_CODE DESC, ST_Distance(GEOM, ST_SetSRID(ST_MAKEPOINT(?, ?), 4326))" +
            " LIMIT 1";

    final static String EXISTING_POSTAL_CODE = "SELECT PC_ID FROM POSTAL_CODES" +
            " WHERE PC_COUNTRY_CODE = ? AND PC_POSTAL_CODE = ?";

    final static String INSERT_POSTAL_CODE = "INSERT INTO POSTAL_CODES (PC_POSTAL_CODE, PC_COUNTRY_CODE, PC_NUTS_CODE)" +
            " VALUES (?, ?, ?)";

    final static String FIND_MUNICIPALITY_BY_NAME = "SELECT M_ID, M_NUTS_CODE FROM MUNICIPALITIES" +
            " WHERE M_COUNTRY_CODE = ? AND M_NAME = ?";

    final static String FIND_CITY_BY_NAME = "SELECT C_ID FROM CITIES" +
            " WHERE C_COUNTRY_CODE = ? AND C_NAME = ?";

    final static String FIND_PLACE_GEONAME_BY_NAME = "SELECT G_ID FROM GEONAMES" +
            " WHERE G_COUNTRY_CODE = ? AND G_NAME = ? AND G_FEATURE_CLASS = 'P' AND G_IGNORE IS FALSE";

    final static String INSERT_CITY = "INSERT INTO CITIES (C_NAME, C_COUNTRY_CODE, C_NUTS_CODE, C_M_ID, C_G_ID)" +
            " VALUES (?, ?, ?, ?, ?)";

    final static String FIND_POSTAL_CODE_CITY_COMBINATION = "SELECT * FROM CITIES_2_POSTAL_CODES" +
            " WHERE C2PC_C_ID = ? AND C2PC_PC_ID = ?";

    final static String INSERT_POSTAL_CODE_CITY_COMBINATION = "INSERT INTO CITIES_2_POSTAL_CODES (C2PC_C_ID, C2PC_PC_ID)" +
            " VALUES (?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GeoNameRepository geoNameRepository;

    private SimpleJdbcInsert jdbcMunicipalityInsert;

    @PostConstruct
    public void setup() {
        jdbcMunicipalityInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("MUNICIPALITIES").usingGeneratedKeyColumns("m_id");
    }

    @Override
    public void write(List<? extends GeoNamesPostalCode> geoNames) {
        geoNames.forEach(g -> {
            GeoNameEntity geoNameMunicipality = geoNameRepository.findMunicipalityForPostalCode(g);
            Long mId = null;
            String nutsCode = null;
            if (geoNameMunicipality != null) {
                Municipality municipality = getOrCreateMunicipality(geoNameMunicipality);
                mId = municipality.getId();
                nutsCode = municipality.getNutsCode();
            }

            Long pcId = getOrCreatePostalCode(g, nutsCode);
            createUpdateCityAndLinkPostal(g, pcId, mId);
            LOGGER.info("Processed postal code {}", pcId);
        });
        LOGGER.info("Processed {}", geoNames.size());
    }

    private Municipality getOrCreateMunicipality(GeoNameEntity geoNameMunicipality) {
        Long mId;
        String nutsCode;
        try {
            // TODO: Also serach by nuts code because you can have same name across country.
            Map mObject = jdbcTemplate.queryForMap(FIND_MUNICIPALITY_BY_NAME, geoNameMunicipality.getCountryCodeIso2(),
                    geoNameMunicipality.getName());
            mId = (Long) mObject.get("M_ID");
            nutsCode = (String) mObject.get("M_NUTS_CODE");
        } catch (IncorrectResultSizeDataAccessException e) {
            nutsCode = calculateNutsCode(geoNameMunicipality.getCountryCodeIso2(), geoNameMunicipality.getLongitude(),
                    geoNameMunicipality.getLatitude());
            final String finalNutsCode = nutsCode;

            Map<String, Object> params = new HashMap<>();
            params.put("M_NAME", geoNameMunicipality.getName());
            params.put("M_COUNTRY_CODE", geoNameMunicipality.getCountryCodeIso2());
            params.put("M_NUTS_CODE", finalNutsCode);
            params.put("M_G_ID", geoNameMunicipality.getId());

            mId = jdbcMunicipalityInsert.executeAndReturnKey(params).longValue();
            /* KeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_MUNICIPALITY, new String[]{"M_ID"});
                        ps.setString(1, geoNameMunicipality.getName());
                        ps.setString(2, geoNameMunicipality.getCountryCodeIso2());
                        ps.setString(3, finalNutsCode);
                        ps.setObject(4, geoNameMunicipality.getId());
                        return ps;
                    }, holder);

            mId = holder.getKey().longValue();*/
        }

        return new Municipality(mId, geoNameMunicipality.getName(), nutsCode);
    }

    private void createUpdateCityAndLinkPostal(GeoNamesPostalCode geoName, Long postalCodeId, Long municipalityId) {
        Long cityId;
        try {
            cityId = jdbcTemplate.queryForObject(FIND_CITY_BY_NAME, new Object[]{geoName.getCountryIso2(),
                    geoName.getPlaceName()}, Long.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            Long gId = null;
            try {
                gId = jdbcTemplate.queryForObject(FIND_PLACE_GEONAME_BY_NAME, new Object[]{geoName.getCountryIso2(),
                        geoName.getPlaceName()}, Long.class);
            } catch (IncorrectResultSizeDataAccessException e1) {
                /*Point point = GF.createPoint(new Coordinate(geoName.getLongitude().doubleValue(),
                        geoName.getLatitude().doubleValue()));
                try {
                    gId = jdbcTemplate.queryForObject(FIND_NEAREST_CITY_GEONAME, new Object[]{point}, Long.class);
                } catch (IncorrectResultSizeDataAccessException ignorable) {
                    throw new RuntimeException("Cannot find place with name " + geoName.getPlaceName());
                }*/
            }
            KeyHolder holder = new GeneratedKeyHolder();
            String nutsCode = calculateNutsCode(geoName.getCountryIso2(), geoName.getLongitude(), geoName.getLatitude()); // TODO: Coordinates from Municipality (gId)
            Long finalGId = gId;
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_CITY, new String[]{"c_id"});
                        ps.setString(1, geoName.getPlaceName());
                        ps.setString(2, geoName.getCountryIso2());
                        ps.setString(3, nutsCode);
                        ps.setObject(4, municipalityId);
                        ps.setObject(5, finalGId);
                        return ps;
                    }, holder);

            cityId = holder.getKey().longValue();
        }

        try {
            jdbcTemplate.queryForMap(FIND_POSTAL_CODE_CITY_COMBINATION, cityId, postalCodeId);
        } catch (IncorrectResultSizeDataAccessException e) {
            jdbcTemplate.update(INSERT_POSTAL_CODE_CITY_COMBINATION, cityId, postalCodeId);
        }
    }

    private Long getOrCreatePostalCode(GeoNamesPostalCode geoName, String nutsCode) {
        Long pcId;
        try {
            pcId = jdbcTemplate.queryForObject(EXISTING_POSTAL_CODE, new Object[]{geoName.getCountryIso2(),
                    geoName.getPostalCode()}, Long.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            KeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_POSTAL_CODE, new String[]{"pc_id"});
                        ps.setString(1, geoName.getPostalCode());
                        ps.setString(2, geoName.getCountryIso2());
                        ps.setString(3, nutsCode);
                        return ps;
                    }, holder);

            pcId = holder.getKey().longValue();
        }


        return pcId;
    }

    private String calculateNutsCode(String countryCodeIso2, BigDecimal longitude, BigDecimal latitude) {
        try {
            return jdbcTemplate.queryForObject(QUERY_WITHIN_NUTS, new Object[]{longitude, latitude, countryCodeIso2}, String.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            return jdbcTemplate.queryForObject(QUERY_CLOSEST_NUTS, new Object[]{countryCodeIso2, longitude, latitude}, String.class);
        }
    }
}
