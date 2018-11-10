package com.logimos.nutsimporter.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConditionalOnProperty(name = "spring.datasource.platform", havingValue = "h2")
@Component
@Order(value = Integer.MIN_VALUE)
public class H2NutsDbInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2NutsDbInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final Resource nutsDataDir;

    public H2NutsDbInitializer(JdbcTemplate jdbcTemplate, @Value("${app.nuts.data-dir}") Resource nutsDataDir) {
        this.jdbcTemplate = jdbcTemplate;
        this.nutsDataDir = nutsDataDir;
    }

    @Override
    public void run(String... args) throws IOException {
        LOGGER.info("Initializing NUTS database");
        if (!nutsDataDir.exists()) {
            throw new RuntimeException("Specified NUTS data dir does not exist: " + nutsDataDir.getFile().getAbsolutePath());
        }
        Path nutsBnShpFile = Paths.get(nutsDataDir.getFile().getAbsolutePath(), "NUTS_BN_01M_2016_4326.shp");
        //jdbcTemplate.update("CALL FILE_TABLE(?, 'NUTS_BN_01M_2016_4326')", nutsBnShpFile.toString());
        jdbcTemplate.update("CALL SHPRead(?, 'NUTS_BN_01M_2016_4326')", nutsBnShpFile.toString());
        jdbcTemplate.update("ALTER TABLE NUTS_BN_01M_2016_4326 ALTER COLUMN THE_GEOM RENAME TO GEOM");

        Path nutsLbShpFile = Paths.get(nutsDataDir.getFile().getAbsolutePath(), "NUTS_LB_2016_4326.shp");
        //jdbcTemplate.update("CALL FILE_TABLE(?, 'NUTS_LB_2016_4326')", nutsLbShpFile.toString());
        jdbcTemplate.update("CALL SHPRead(?, 'NUTS_LB_2016_4326')", nutsLbShpFile.toString());
        jdbcTemplate.update("ALTER TABLE NUTS_LB_2016_4326 ALTER COLUMN THE_GEOM RENAME TO GEOM");

        Path nutsRgShpFile = Paths.get(nutsDataDir.getFile().getAbsolutePath(), "NUTS_RG_01M_2016_4326.shp");
        //jdbcTemplate.update("CALL FILE_TABLE(?, 'NUTS_RG_01M_2016_4326')", nutsRgShpFile.toString());
        jdbcTemplate.update("CALL SHPRead(?, 'NUTS_RG_01M_2016_4326')", nutsRgShpFile.toString());
        jdbcTemplate.update("ALTER TABLE NUTS_RG_01M_2016_4326 ALTER COLUMN THE_GEOM RENAME TO GEOM");
        jdbcTemplate.update("CREATE SPATIAL INDEX NUTS_RG_01M_2016_4326_GEOM_IDX ON NUTS_RG_01M_2016_4326(GEOM)");

        LOGGER.info("NUTS database ready");
    }
}
