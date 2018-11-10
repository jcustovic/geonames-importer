package com.logimos.nutsimporter.infrastructure.batch;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class GeoNamesReader extends FlatFileItemReader<FieldSet> {

    public GeoNamesReader(@Value("#{jobParameters['geonames.file']}") Resource resource) {
        super();
        setResource(resource);
        setEncoding("UTF-8");
        setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(DelimitedLineTokenizer.DELIMITER_TAB) {{
                setComments(new String[]{"#"});
                setQuoteCharacter((char) 0);
                setNames("geonameid", "name", "asciiName", "alternateNames", "latitude", "longitude", "featureClass"
                        , "featureCode", "countryCode", "cc2", "admin1Code", "admin2Code", "admin3Code", "admin4Code"
                        , "population", "elevation", "dem", "timezone", "modificationDate");
            }});
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
    }
}
