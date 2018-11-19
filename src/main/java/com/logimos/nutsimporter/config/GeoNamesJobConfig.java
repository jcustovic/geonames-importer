package com.logimos.nutsimporter.config;

import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import com.logimos.nutsimporter.infrastructure.batch.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class GeoNamesJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job lauImporterJob() {
        return jobBuilderFactory.get("lauImporterJob")
                .start(lauImporterStep(null, null))
                .preventRestart()
                .build();
    }

    @Bean
    public Step lauImporterStep(LauReader reader, LauWriter writer) {
        return stepBuilderFactory.get("lauImporterStep")
                .<FieldSet, FieldSet>chunk(1000)
                //.faultTolerant().skip(Exception.class).skipLimit(0)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public Job geonamesImporterJob() {
        return jobBuilderFactory.get("geonamesImporterJob")
                .start(geonamesImporterStep(null, null))
                .preventRestart()
                .build();
    }

    @Bean
    public Step geonamesImporterStep(GeoNamesReader reader, GeoNamesWriter writer) {
        return stepBuilderFactory.get("geonamesImporterStep")
                .<FieldSet, FieldSet>chunk(1000)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    public Job geonamesPostalCodeImporterJob() {
        return jobBuilderFactory.get("geonamesPostalCodeImporterJob")
                .start(geonamesPostalCodeImporterStep(null, null))
                .preventRestart()
                .build();
    }

    @Bean
    public Step geonamesPostalCodeImporterStep(GeoNamesPostalCodeReader reader, GeoNamesPostalCodeWriter writer) {
        return stepBuilderFactory.get("geonamesPostalCodeImporterStep")
                .<GeoNamesPostalCode, GeoNamesPostalCode>chunk(1000)
                .reader(reader)
                .writer(writer)
                .build();
    }
}
