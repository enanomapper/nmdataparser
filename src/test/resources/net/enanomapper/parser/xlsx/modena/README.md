

Phys chem import
`-m true clear` measurements   clear previous measurements, add physchem
`-t true clear` compositions   composition is read from the json and xlsx
````
-i "MODENA-EC50_EC25.xlsx" -j "MODENA-modelling-pchem.json" -m true -t true  -c ambit.properties
````

Tox import
`-m false` don't clear measurements  just add the tox properties
`-t false` don't clear compositions  
````
-i "MODENA-EC50_EC25.xlsx" -j "MODENA-modelling-tox.json" -m false -t false  -c ambit.properties
````