package com.logimos.nutsimporter.infrastructure.jpa.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NutsRepository {

    final static String QUERY_WITHIN_NUTS = "SELECT NUTS_ID FROM NUTS_RG_01M_2016_4326" +
            " WHERE ST_DWITHIN(GEOM, ST_SetSRID(ST_MAKEPOINT(:longitude, :latitude), 4326), 0) = TRUE" +
            " AND CNTR_CODE = :countryCode AND LEVL_CODE = 3";

    final static String QUERY_CLOSEST_NUTS = "SELECT NUTS_ID FROM NUTS_RG_01M_2016_4326" +
            " WHERE CNTR_CODE = :countryCode" +
            " AND ST_Distance(GEOM, ST_SetSRID(ST_MAKEPOINT(:longitude, :latitude), 4326)) < 0.5" +
            " ORDER BY LEVL_CODE DESC, ST_Distance(GEOM, ST_SetSRID(ST_MAKEPOINT(:longitude, :latitude), 4326))";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
    public String findNutsWithinBoundaries(String countryCodeIso2, BigDecimal longitude, BigDecimal latitude) {
        Map<String, Object> params = new HashMap<>();
        params.put("countryCode", countryCodeIso2);
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        
        return jdbcTemplate.queryForObject(QUERY_WITHIN_NUTS, params, String.class); 
    }

    public List<String> findClosestNutsWithinRange(String countryCodeIso2, BigDecimal longitude, BigDecimal latitude) {
        Map<String, Object> params = new HashMap<>();
        params.put("countryCode", countryCodeIso2);
        params.put("longitude", longitude);
        params.put("latitude", latitude);
        
        return jdbcTemplate.queryForList(QUERY_CLOSEST_NUTS, params, String.class);
    }
   
}
