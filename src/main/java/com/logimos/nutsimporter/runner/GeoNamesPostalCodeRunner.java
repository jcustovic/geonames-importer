package com.logimos.nutsimporter.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;

@Component
@Order(2)
public class GeoNamesPostalCodeRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNamesPostalCodeRunner.class);

    private final Resource geoNamesPostalCodeDir;
    private final JobLauncher jobLauncher;
    private final Job geonamesPostalCodeImporterJob;

    public GeoNamesPostalCodeRunner(@Value("${app.geonames.postal-code-data-dir}") Resource geoNamesPostalCodeDir,
                                    JobLauncher jobLauncher, Job geonamesPostalCodeImporterJob) {
        this.geoNamesPostalCodeDir = geoNamesPostalCodeDir;
        this.jobLauncher = jobLauncher;
        this.geonamesPostalCodeImporterJob = geonamesPostalCodeImporterJob;
    }

    @Override
    public void run(String... args) throws IOException, JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        if (geoNamesPostalCodeDir.exists()) {
            File[] files = geoNamesPostalCodeDir.getFile().listFiles((dir, filename) -> filename.endsWith(".txt"));
            if (files == null || files.length == 0) {
                LOGGER.warn("GeoNames postal code dir is empry. No files to process");
            } else {
                for (File file : files)
                    jobLauncher.run(geonamesPostalCodeImporterJob,
                            new JobParametersBuilder()
                                    .addLong("run.id", currentTimeMillis())
                                    .addString("geonames.postal-code.file", "file:/" + file.getAbsolutePath(), false)
                                    .toJobParameters()
                    );
            }
        } else {
            LOGGER.warn("GeoNames postal code dir does not exist");
        }
    }

}
