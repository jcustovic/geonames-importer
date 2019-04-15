package com.logimos.nutsimporter.infrastructure.batch;

import com.logimos.nutsimporter.domain.Municipality;
import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.logimos.nutsimporter.domain.geonames.municipality.GeoNameMunicipalityByCountryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GeoNamesPostalCodeWriter implements ItemWriter<GeoNamesPostalCode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesPostalCodeWriter.class);

    final static String EXISTING_POSTAL_CODE = "SELECT PC_ID FROM POSTAL_CODES" +
            " WHERE PC_COUNTRY_CODE = ? AND PC_POSTAL_CODE = ?";

    final static String INSERT_POSTAL_CODE = "INSERT INTO POSTAL_CODES (PC_POSTAL_CODE, PC_COUNTRY_CODE, PC_NUTS_CODE)" +
            " VALUES (?, ?, ?)";

    final static String FIND_CITY_BY_NAME_AND_NUTS_CODE = "SELECT C_ID FROM CITIES" +
            " WHERE C_COUNTRY_CODE = ? AND C_NAME = ? AND C_NUTS_CODE = ?";

    final static String INSERT_CITY = "INSERT INTO CITIES (C_NAME, C_COUNTRY_CODE, C_NUTS_CODE, C_M_ID)" +
            " VALUES (?, ?, ?, ?)";

    final static String FIND_POSTAL_CODE_CITY_COMBINATION = "SELECT * FROM CITIES_2_POSTAL_CODES" +
            " WHERE C2PC_C_ID = ? AND C2PC_PC_ID = ?";

    final static String INSERT_POSTAL_CODE_CITY_COMBINATION = "INSERT INTO CITIES_2_POSTAL_CODES (C2PC_C_ID, C2PC_PC_ID)" +
            " VALUES (?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GeoNameMunicipalityByCountryResolver geoNameMunicipalityByCountryResolver;

    @Override
    public void write(List<? extends GeoNamesPostalCode> geoNames) {
        AtomicInteger failedCount = new AtomicInteger();
        geoNames.forEach(g -> {
            Municipality municipality = geoNameMunicipalityByCountryResolver.findMunicipalityForPostalCode(g);
            if (municipality == null) {
                LOGGER.error("Municipality not found for {}", g);
                failedCount.getAndIncrement();
            } else {
                Long pcId = getOrCreatePostalCode(g, municipality);
                createUpdateCityAndLinkPostal(g, pcId, municipality);
            }
        });
        LOGGER.info("Processed {} (Failed: {})", geoNames.size(), failedCount.intValue());
    }

    private void createUpdateCityAndLinkPostal(GeoNamesPostalCode geoName, Long postalCodeId, Municipality municipality) {
        Long cityId;
        try {
            cityId = jdbcTemplate.queryForObject(FIND_CITY_BY_NAME_AND_NUTS_CODE, new Object[]{geoName.getCountryIso2(),
                    geoName.getPlaceName(), municipality.getNutsCode()}, Long.class);
        } catch (IncorrectResultSizeDataAccessException e) {
            KeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(INSERT_CITY, new String[]{"c_id"});
                        ps.setString(1, geoName.getPlaceName());
                        ps.setString(2, geoName.getCountryIso2());
                        ps.setString(3, municipality.getNutsCode());
                        ps.setObject(4, municipality.getId());

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

    private Long getOrCreatePostalCode(GeoNamesPostalCode geoName, Municipality municipality) {
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
                        ps.setString(3, municipality.getNutsCode());

                        return ps;
                    }, holder);

            pcId = holder.getKey().longValue();
        }


        return pcId;
    }
}
