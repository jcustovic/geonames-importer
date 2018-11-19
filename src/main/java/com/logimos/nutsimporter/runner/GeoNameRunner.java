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

//@Component
@Order(2)
public class GeoNameRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoNameRunner.class);

    private final Resource geoNamesDataDir;
    private final JobLauncher jobLauncher;
    private final Job geonamesImporterJob;

    public GeoNameRunner(@Value("${app.geonames.data-dir}") Resource geoNamesDataDir, JobLauncher jobLauncher,
                         Job geonamesImporterJob) {
        this.geoNamesDataDir = geoNamesDataDir;
        this.jobLauncher = jobLauncher;
        this.geonamesImporterJob = geonamesImporterJob;
    }

    @Override
    public void run(String... args) throws IOException, JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        if (geoNamesDataDir.exists()) {
            File[] files = geoNamesDataDir.getFile().listFiles((dir, filename) -> filename.endsWith(".txt"));
            if (files == null || files.length == 0) {
                LOGGER.warn("GeoNames data dir is empty. No files to process");
            } else {
                for (File file : files)
                    jobLauncher.run(geonamesImporterJob,
                            new JobParametersBuilder()
                                    .addLong("run.id", currentTimeMillis())
                                    .addString("geonames.file", "file:/" + file.getAbsolutePath(), false)
                                    .toJobParameters()
                    );
            }
        } else {
            LOGGER.warn("GeoNames data dir does not exist");
        }
    }

}
