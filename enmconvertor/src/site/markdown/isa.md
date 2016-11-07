# Investigation-Study-Assay (ISA)

The [ISA](http://isa-tools.org/) platform includes three major layers: `Investigation`, `Study` and `Assay` where the actual experimental readouts are stored in additional data layer.

## ISA-TAB

[ISA (Investigation-Study-Assay)](http://isa-tools.org/) is a general purpose multi-layer platform for description of
complex meta-data needed for the interpretation of experiments developed by S. Sansone’s
group at the University of Oxford e-Research Centre. ISA includes three major layers:
Investigation, Study and Assay where the actual experimental readouts are stored in an
additional data layer. Since the ISA version 1 published in 2008, the ISA platform implementation
relies on tab delimited text files and the file format representation is known as ISA-Tab. 

## ISA-TAB-Nano

The ISA-Tab-Nano project is an effort of the National Cancer Institute (NCI), National Cancer Informatics Program (NCIP) and Nanotechnology Informatics Working Group (US Nano WG). The [ISA-Tab-Nano extension](https://wiki.nci.nih.gov/display/icr/isa-tab-nano), includes an additional material file within the study layer, which is
used for the description of the NM composition and nominal NM characteristics (size, shape, as reported by the manufacturer).

## ISA-JSON

The ISA group started migration in 2015 to a new JSON format, instead of tab delimited files as more flexible and efficient way to store the experimental meta-data.
Currently, there are ISA v1 and ISA v2 JSON versions. The ISA v2 is based solely on JSON, where the backward compatibility with the ISA v1 and ISA-TAB format is preserved via ISA-tools project utilities.

The new ISA format is labelled ISA-JSON version 1 and uses . The ISA-JSON specification is based on a set of JSON schemas used to validate the syntactical and logical correctness of the ISA-JSON information. It distinguishes between core components and extensions, for example the support for nanomaterials is considered an [extension](https://media.readthedocs.org/pdf/isatools/latest/isatools.pdf). 
The JSON schemas describe various components of the 3 layer of ISA platform (i.e. investigation, study, assay, sample, source, ontology annotation, etc). The schemas are available at [https://github.com/ISA-tools/isa-api/tree/master/isatools/schemas/isa_model_version_1_0_schemas/core](https://github.com/ISA-tools/isa-api/tree/master/isatools/schemas/isa_model_version_1_0_schemas/core)

### New material schema for ISA-JSON

We have developed a Substance/Material JSON schema which is a nano material extension of ISA-JSON v1, the counterpart of the ISA-Tab-Nano format. 

The schema is available at the enanomapper/isa-api fork at GitHub [https://github.com/enanomapper/isa-api/tree/master/isatools/schemas/isa_model_version_1_0_schemas/material](https://github.com/enanomapper/isa-api/tree/master/isatools/schemas/isa_model_version_1_0_schemas/material).

Feedback is welcome!

## ISA-JSON export for enanoMapper database

 


### 

 Currently ISA model is utilized via  following file formats: the new ISA-JSON file format (actively developed in last two years by the Oxford group), the original ISA-TAB format that relies on tab delimited text files and ISA-TAB-Nano format which is a nano extension to ISA-TAB. We conduct active communication and close contacts with ISA development team from Oxford, the National Cancer Institute (NCI), National Cancer Informatics Program (NCIP) and Nanotechnology Informatics Working Group (US Nano WG) in order to take into account the latest developments of ISA platform and to have most adequate data model for ISA mapping into eNanoMapper database. eNanoMapper ISA utilities are based on:
 - ISA-JSON version 1 which covers the original ISA-TAB model developed by S. Sansone’s group (published in 2008) on top of new ISA-JSON format;
- Material Schema - developed by eNanoMapper extension to ISA-JSON core which covers the data model developed within the frame of ISA-Tab-Nano project (NCI, NCIP and US NanoWG). 


ISA-JSON export software tools are available at:

https://svn.code.sf.net/p/ambit/code/trunk/ambit2-all/ambit2-apps/ambit2-export/



## `ISA-TAB Logic` templates

NANoREG released a large set of Excel templates at [http://www.nanoreg.eu/media-and-downloads/templates/269-templates-for-experimental-data-logging](http://www.nanoreg.eu/media-and-downloads/templates/269-templates-for-experimental-data-logging).
While not strictly following the [ISA-TAB](http://isa-tools.org/) and [ISA-TAB-Nano](https://wiki.nci.nih.gov/display/icr/isa-tab-nano) formats, the NANoREG templates have been designed around ISA-Tab-logic, i.e. structuring the data in investigation-study-assay related groups.

>NANoREG has produced a set of easy-to-use 'templates'. The templates have been built by experts in different fields (phys-chem, in vivo and in vitro toxicology) and are aimed at harmonising the logging of experimentally-produced data, and include meaningful and detailed information to support causal correlation analyses, modelling and Safe-by-Design (SbD).

We developed tools to read the NANoREG Excel templates and convert into eNanoMapper data model. This enable import into eNanoMapper database and conversion of the NANoREG Excel templates to ISA-JSON v1 compliant files.
The ISA-JSON v1 files can be converted to the legacy ISA-TAB files via the Python tools available at [ISA](http://isa-tools.org/).  