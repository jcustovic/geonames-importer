package com.logimos.nutsimporter.infrastructure.batch;

import com.logimos.nutsimporter.domain.geonames.model.GeoNamesPostalCode;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class GeoNamesPostalCodeReader extends FlatFileItemReader<GeoNamesPostalCode> {

    public GeoNamesPostalCodeReader(@Value("#{jobParameters['geonames.postal-code.file']}") Resource resource) {
        super();
        setResource(resource);
        setEncoding("UTF-8");
        setLineMapper(new DefaultLineMapper<GeoNamesPostalCode>() {{
            setLineTokenizer(new DelimitedLineTokenizer(DelimitedLineTokenizer.DELIMITER_TAB) {{
                setComments(new String[]{"#"});
                setQuoteCharacter((char) 0);
                setNames("countryIso2", "postalCode", "placeName", "adminName1", "adminCode1", "adminName2", "adminCode2"
                        , "adminName3", "adminCode3", "latitude", "longitude", "accuracy");
            }});

            FieldSetMapper<GeoNamesPostalCode> mapper = new BeanWrapperFieldSetMapper<>();
            ((BeanWrapperFieldSetMapper<GeoNamesPostalCode>) mapper).setTargetType(GeoNamesPostalCode.class);
            setFieldSetMapper(mapper);
        }});
    }
}
