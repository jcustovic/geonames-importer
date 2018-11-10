CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
CALL H2GIS_SPATIAL();

CREATE TABLE GEONAMES (
  G_ID                INT            NOT NULL,
  G_NAME              VARCHAR(200)   NOT NULL,
  G_ASCIINAME         VARCHAR(200)   NOT NULL,
  G_ALTERNATENAMES    VARCHAR(10000) NULL,
  G_LATITUDE          DECIMAL(10, 8) NOT NULL,
  G_LONGITUDE         DECIMAL(11, 8) NOT NULL,
  G_FEATURE_CLASS     VARCHAR(1)     NOT NULL,
  G_FEATURE_CODE      VARCHAR(10)    NOT NULL,
  G_COUNTRY_CODE      VARCHAR(2)     NOT NULL,
  G_FIPS_CODE         VARCHAR(20)    NOT NULL,
  G_ADMIN2_CODE       VARCHAR(80),
  G_ADMIN3_CODE       VARCHAR(20),
  G_ADMIN4_CODE       VARCHAR(20),
  G_MODIFICATION_DATE DATE           NOT NULL,
  G_GEOM              GEOMETRY       NOT NULL,
  G_IGNORE            BOOLEAN        NOT NULL,

  PRIMARY KEY (G_ID)
);


CREATE INDEX GEONAMES_NAME_IDX
  ON GEONAMES (G_NAME);

CREATE INDEX GEONAMES_ASCIINAME_IDX
  ON GEONAMES (G_ASCIINAME);

CREATE INDEX GEONAMES_COUNTRY_CODE_IDX
  ON GEONAMES (G_COUNTRY_CODE);

CREATE INDEX GEONAMES_FEATURE_CLASS_IDX
  ON GEONAMES (G_FEATURE_CLASS);

CREATE INDEX GEONAMES_FEATURE_CODE_IDX
  ON GEONAMES (G_FEATURE_CODE);

CREATE INDEX GEONAMES_FIPS_CODE_IDX
  ON GEONAMES (G_FIPS_CODE);

CREATE SPATIAL INDEX GEONAMES_GEOM_IDX
  ON GEONAMES (G_GEOM);

CREATE SEQUENCE POSTAL_CODES_seq;
CREATE TABLE POSTAL_CODES (
  PC_ID           BIGINT      NOT NULL DEFAULT nextval('POSTAL_CODES_seq'),
  PC_POSTAL_CODE  VARCHAR(20) NOT NULL,
  PC_COUNTRY_CODE VARCHAR(2)  NOT NULL,
  PC_NUTS_CODE    VARCHAR(5),

  PRIMARY KEY (PC_ID),
  CONSTRAINT uq_pc_code_country UNIQUE (PC_POSTAL_CODE, PC_COUNTRY_CODE)
);

CREATE INDEX POSTAL_CODES_CODE_IDX
  ON POSTAL_CODES (PC_POSTAL_CODE);

CREATE INDEX POSTAL_CODES_COUNTRY_CODE_IDX
  ON POSTAL_CODES (PC_COUNTRY_CODE);

CREATE INDEX POSTAL_CODES_NUTS_CODE_IDX
  ON POSTAL_CODES (PC_NUTS_CODE);

CREATE SEQUENCE MUNICIPALITIES_seq;
CREATE TABLE MUNICIPALITIES (
  M_ID           BIGINT     NOT NULL DEFAULT nextval('MUNICIPALITIES_seq'),
  M_NAME         VARCHAR(200),
  M_COUNTRY_CODE VARCHAR(2) NOT NULL,
  M_NUTS_CODE    VARCHAR(5) NOT NULL,
  M_G_ID         INT        NOT NULL,

  PRIMARY KEY (M_ID),
  CONSTRAINT fk_municipalities_g_id FOREIGN KEY (M_G_ID) REFERENCES GEONAMES (G_ID)
);

CREATE INDEX MUNICIPALITIES_COUNTRY_CODE_IDX
  ON MUNICIPALITIES (M_COUNTRY_CODE);

CREATE INDEX MUNICIPALITIES_NUTS_CODE_IDX
  ON MUNICIPALITIES (M_NUTS_CODE);

CREATE SEQUENCE CITIES_seq;
CREATE TABLE CITIES (
  C_ID           BIGINT       NOT NULL DEFAULT nextval('CITIES_seq'),
  C_NAME         VARCHAR(100) NOT NULL,
  C_COUNTRY_CODE VARCHAR(2)   NOT NULL,
  C_NUTS_CODE    VARCHAR(5)   NOT NULL,
  C_M_ID         BIGINT,
  C_G_ID         INT,

  PRIMARY KEY (C_ID),
  CONSTRAINT fk_cities_m_id FOREIGN KEY (C_M_ID) REFERENCES MUNICIPALITIES (M_ID),
  CONSTRAINT fk_cities_g_id FOREIGN KEY (C_G_ID) REFERENCES GEONAMES (G_ID)
);

CREATE INDEX CITIES_COUNTRY_CODE_IDX
  ON CITIES (C_COUNTRY_CODE);

CREATE INDEX CITIES_NUTS_CODE_IDX
  ON CITIES (C_NUTS_CODE);

CREATE TABLE CITIES_2_POSTAL_CODES (
  C2PC_C_ID  BIGINT NOT NULL,
  C2PC_PC_ID BIGINT NOT NULL,

  PRIMARY KEY (C2PC_C_ID, C2PC_PC_ID),
  CONSTRAINT fk_c2pc_c_id FOREIGN KEY (C2PC_C_ID) REFERENCES CITIES (C_ID),
  CONSTRAINT fk_c2pc_pc_id FOREIGN KEY (C2PC_PC_ID) REFERENCES POSTAL_CODES (PC_ID)
);
