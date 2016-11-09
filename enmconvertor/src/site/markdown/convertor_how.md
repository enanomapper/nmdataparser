# How the conversion works

The conversion relies on a common data model. The input files are parsed into the internal data model. The data can then be exported into several different formats.

![Data model](./images/data_model.png "Common data model enables translation")


* Excel spreadsheets

eNanoMapper has developed [doi:10.3762/bjnano.6.165](http://www.beilstein-journals.org/bjnano/single/articleFullText.htm?publicId=2190-4286-6-165) a configurable Excel parser (https://github.com/enanomapper/nmdataparser), enabling import of NanoSafety Cluster Excel templates. The configuration metadata for the parser is defined in a separate file, mapping the custom spreadsheet structure into the internal eNanoMapper storage components. More details on the [parser](./parser.html), the [configuration file syntax](./jsonconfig.html) , how to [convert between data formats](./convertor.html) and [how to import into the database](./database.html).

* ISA-JSON

eNanoMapper has developed a working prototype of a utility to export assay data from the eNanoMapper database into ISA-JSON format version 1. Where export to the older ISA-TAB format is needed, ISAtools can be used to convert both to and from the ISA-JSON to ISA-TAB formats. More information on [ISA-JSON export](./isa.html).

* Semantic formats

RDF export following BioAssay Ontology data model is also available. See [how to convert between data formats](./convertor.html).

### For developers

### Data model 

![Java class diagram](./images/ambit2.base.data.study.gif "Java class diagram")

####Java classes for describing Substances

* [Substance](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/data/SubstanceRecord.html)

* [Substance composition](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/relation/composition/CompositionRelation.html)

####[Java classes](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/data/study/package-summary.html) for describing measurements

* [Protocol](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/data/study/Protocol.html)

* [Protocol parameters or conditions](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/index.html?ambit2/base/data/study/Params.html) 

* [Protocol application](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/data/study/ProtocolApplication.html)

* [EffectRecord](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/data/study/EffectRecord.html)

* [Value](http://ambit.sourceforge.net/AMBIT2-LIBS/apidocs/ambit2/base/data/study/Value.html)


 