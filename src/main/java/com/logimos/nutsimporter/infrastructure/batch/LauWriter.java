package com.logimos.nutsimporter.infrastructure.batch;

import com.logimos.nutsimporter.infrastructure.jpa.model.MunicipalityEntity;
import com.logimos.nutsimporter.infrastructure.jpa.repository.jpa.MunicipalityJpaRepository;
import com.logimos.nutsimporter.infrastructure.utils.PlaceNameCleanerUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LauWriter implements ItemWriter<FieldSet> {

    @Autowired
    private MunicipalityJpaRepository municipalityJpaRepository;

    @Override
    public void write(List<? extends FieldSet> lauLines) {
        lauLines.forEach(this::createMunicipalityIfNotExists);
    }

    private void createMunicipalityIfNotExists(FieldSet fieldSet) {
        String nutsCode = fieldSet.readString(0);
        String countryCodeIso2 = nutsCode.substring(0, 2);
        String name = PlaceNameCleanerUtils.cleanName(countryCodeIso2, fieldSet.readString(2));
        String latinName = PlaceNameCleanerUtils.cleanName(countryCodeIso2, fieldSet.readString(3));
        String lauCode = fieldSet.readString(1);
        
        MunicipalityEntity municipality = municipalityJpaRepository.findOne(countryCodeIso2, name, nutsCode);
        if (municipality == null) {
            municipality = new MunicipalityEntity();
            municipality.setName(name);
            municipality.setCountryCodeIso2(countryCodeIso2);
            municipality.setNutsCode(nutsCode);
            // TODO: Save latin name and lau code
            //params.put("M_LAU_CODE", lauCode);
            //params.put("M_LATIN_NAME", latinName);

            municipalityJpaRepository.save(municipality);
        }

    }

}
