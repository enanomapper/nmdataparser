## eNanoMapper data templates release 

### [Physicochemical characterisation](templates_pchem.html)

![Templates: phys-chem](images/templates_PCHEM.png "P-CHEM")

### [In-vitro assays](templates_invitro.html)

![Templates: in vitro](./images/templates_INVITRO.png "In-vitro")

### [In-vivo assays](templates_invivo.html)

![Templates: in vivo](./images/templates_INVIVO.png "In-vivo")

---

### About

The Excel templates are automatically regenerated based on fields defined in the [**NANoREG templates**](http://www.nanoreg.eu/media-and-downloads/templates). 
The generation process includes cleaning of the JRC fields and facilitates the automatic generation of [JSON configuration files](jsonconfig.html), necessary to enable import into an [eNanoMapper database instance](https://data.enanomapper.net)

While not strictly following the [ISA-TAB](http://isa-tools.org/) and [ISA-TAB-Nano](https://wiki.nci.nih.gov/display/icr/isa-tab-nano) formats, the NANoREG templates have been designed around `ISA-Tab-logic`, i.e. structuring the data in investigation-study-assay related groups.

See more about 

* ISA-TAB and the new ISA-JSON [here](isa.html) 

* New [(Nano)Material schema](isa.html) for ISA-JSON  

* [How to convert](convertor.html) Excel data files into ISA-JSON or RDF.

* [How to import](database.html) into [eNanoMapper database](http://ambit.sourceforge.net/enanomapper.html) instance.

* [How the conversion works](convertor_how.html)

> UNDER DEVELOPMENT <

---

### NANoREG templates structure

The NANoREG templates are developed by JRC with collaboration with [FP7 NANoREG](http://www.nanoreg.eu/) project partners and released under open license at [http://www.nanoreg.eu/media-and-downloads/templates](http://www.nanoreg.eu/media-and-downloads/templates).

Within eNanoMapper project (WP3 Database) the templates are analysed, cleaned and [JSON configuration](jsonconfig) files created. The number of [unique terms](terms.html) in the templates is over 800.
 
The NANoREG templates are organized as one spreadsheet per assay, multiple NM can be entered as rows. One Excel file may contain more than one assay, measuring the same endpoint.
The metadata is organised in several groups:

#### Sample information. 
Contains information about the NM (including names, ID, supplier, vial number and replicate number, as well as dispersant). The reporting organisation, operator and date of the experiment are also in this section.

#### Unnamed group listing the module, the endpoint, and the assay name 

* module (phys-chem, in-vitro or in-vivo), 

* endpoint (e.g. cell viability)

* assay name (e.g. “Alamar blue”).

#### Method and instrument information

*	A subgroup “size distribution” , providing placeholders for size distribution measured for the sample (including details on the dispersion protocol and dispersion medium). These fields are (almost) constant across all templates.

*	Any parameter describing the experiment, including cell lines, instrument, controls, time points, concentratons. These differ widely across different experiments.

#### Results

*	Several columns to specify measurement outcomes, along with uncertaintly.

#### SOP (reference to the protocol)


---






