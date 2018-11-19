package com.logimos.nutsimporter.infrastructure.jpa.repository.jpa;

import com.logimos.nutsimporter.infrastructure.jpa.model.MunicipalityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MunicipalityJpaRepository extends JpaRepository<MunicipalityEntity, Long> {

    @Query(value = "SELECT * FROM MUNICIPALITIES WHERE M_COUNTRY_CODE = ?1 AND M_NAME ILIKE ?2 AND M_NUTS_CODE = ?3",
            nativeQuery = true)
    MunicipalityEntity findOne(String countryCodeIso2, String name, String nutsCode);

    List<MunicipalityEntity> findByCountryCodeIso2AndNutsCode(String countryCodeIso2, String nutsCode);
    
    List<MunicipalityEntity> findByCountryCodeIso2AndNutsCodeIn(String countryCodeIso2, List<String> nutsCode);

    List<MunicipalityEntity> findByCountryCodeIso2AndName(String countryCode, String name);
}
