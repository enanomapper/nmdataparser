drop database if exists ambit_templates;
CREATE DATABASE `ambit_templates` /*!40100 DEFAULT CHARACTER SET utf8 */;
use ambit_templates;

drop table if exists assay_template;
CREATE TABLE `assay_template` (
  `endpoint` varchar(64) DEFAULT NULL,
  `assay` varchar(45) DEFAULT NULL,
  `row` int(11) DEFAULT NULL,
  `col` int(11) DEFAULT NULL,
  `idtemplate` varchar(45) DEFAULT NULL,
  `module` varchar(16) DEFAULT NULL,
  `level1` varchar(32) DEFAULT NULL,
  `level2` varchar(32) DEFAULT NULL,
  `level3` varchar(32) DEFAULT NULL,
  `value` varchar(192) DEFAULT NULL,
  `value_clean` varchar(192) DEFAULT NULL,
  `header1` varchar(80) DEFAULT NULL,
  `hint` varchar(160) DEFAULT NULL,
  `unit` varchar(32) DEFAULT NULL,
  `annotation` varchar(64) DEFAULT NULL,
  `file` varchar(32) DEFAULT NULL,
  `folder` varchar(32) DEFAULT NULL,
  `sheet` varchar(32) DEFAULT NULL,
  `visible` tinyint(4) DEFAULT '1',
  KEY `primary_index` (`idtemplate`,`row`,`col`),
  KEY `endpointx` (`endpoint`,`assay`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOAD DATA INFILE 'D:/src-restored/ambit3/nmdataparser/enmexcelparser/src/main/resources/net/enanomapper/templates/JRCTEMPLATES_102016.csv' 
INTO TABLE assay_template
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(@annotation,@_col,@file,@folder,
@JSON_LEVEL1,@JSON_LEVEL2,@JSON_LEVEL3,@_row,@sheet,@value,@cleanedvalue,
@endpoint,@header1,@hint,@id,@module,@s_uuid,@unit
)
set idtemplate=@id,
module=@module,
endpoint=@endpoint,
assay=@s_uuid,
level1=@JSON_LEVEL1,
level2=@JSON_LEVEL2,
level3=@JSON_LEVEL3,
row=@_row,
col=@_col,
value=@value,
value_clean=@cleanedvalue,
header1=@header1,
hint=@hint,
unit=@unit,
file=@file,
folder=@folder,
sheet=@sheet,
annotation=@annotation
;
-- potential errors
update assay_template set visible=0 where sheet="Aerosol Characterisation" and idtemplate="cd616a90_sae";
update assay_template set visible=0 where sheet="Aspect of deposit" and endpoint="epithelial barrier integrity";
update assay_template set visible=0 where sheet="hCTA" and endpoint="cytotoxicity";
update assay_template set visible=0 where sheet="hemolysis" and endpoint="cytotoxicity";
update assay_template set visible=0 where sheet="hemolysis" and endpoint="epithelial barrier integrity";
