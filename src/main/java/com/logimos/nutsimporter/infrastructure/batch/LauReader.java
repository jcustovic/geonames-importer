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
public class LauReader extends FlatFileItemReader<FieldSet> {

    public LauReader(@Value("#{jobParameters['nuts.lau.file']}") Resource resource) {
        super();
        setResource(resource);
        setEncoding("UTF-8");
        setLinesToSkip(1);
        setLineMapper(new DefaultLineMapper<FieldSet>() {{
            setLineTokenizer(new DelimitedLineTokenizer(";") {{
                setComments(new String[]{"#"});
                setQuoteCharacter((char) 0);
                //setNames("nuts3Code", "lauCode", "lauName", "lauNameLatin");
            }});
            setFieldSetMapper(new PassThroughFieldSetMapper());
        }});
    }
}
