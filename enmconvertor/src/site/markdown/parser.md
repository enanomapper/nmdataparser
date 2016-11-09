# Excel data parser

The parser enables import of the data, stored in the supported set of spreadsheet templates, and accommodates different row-based, column-based or mixed organizations of the data.

![parser-outline](./images/parser_outline.png "Excel parser outline")

See JSON Configuration syntax [documentation](./jsonconfig.html).

Predefined JSON configuration files for the [NANoREG templates](./index.html) are provided next to each Excel file, e.g. [INVITRO/GENOTOXICITY/COMET](https://github.com/enanomapper/nmdataparser/tree/master/enmconvertor/src/site/resources/templates/INVITRO/GENOTOXICITY/COMET).


## For developers 

Code available at [https://github.com/enanomapper/nmdataparser](https://github.com/enanomapper/nmdataparser)

* Maven dependency 

````
<dependency>
  <groupId>net.enanomapper</groupId>
  <artifactId>nmparser</artifactId>
  <version>1.0.3-SNAPSHOT</version>
</dependency>
````

Usage:

![parser-code](./images/parser_code.png "Excel parser code")