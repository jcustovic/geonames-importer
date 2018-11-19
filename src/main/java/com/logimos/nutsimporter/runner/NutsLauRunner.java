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
@Order(1)
public class NutsLauRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(NutsLauRunner.class);

    private final Resource nutsLauDataDir;
    private final JobLauncher jobLauncher;
    private final Job lauImporterJob;

    public NutsLauRunner(@Value("${app.nuts.lau-csv-dir}") Resource nutsLauDataDir, JobLauncher jobLauncher,
                         Job lauImporterJob) {
        this.nutsLauDataDir = nutsLauDataDir;
        this.jobLauncher = jobLauncher;
        this.lauImporterJob = lauImporterJob;
    }

    @Override
    public void run(String... args) throws IOException, JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        if (nutsLauDataDir.exists()) {
            File[] files = nutsLauDataDir.getFile().listFiles((dir, filename) -> filename.endsWith(".csv"));
            if (files == null || files.length == 0) {
                LOGGER.warn("NUTS LAU data dir is empty. No files to process");
            } else {
                for (File file : files)
                    jobLauncher.run(lauImporterJob,
                            new JobParametersBuilder()
                                    .addLong("run.id", currentTimeMillis())
                                    .addString("nuts.lau.file", "file:/" + file.getAbsolutePath(), false)
                                    .toJobParameters()
                    );
            }
        } else {
            LOGGER.warn("NUTS LAU data dir does not exist");
        }
    }

}
