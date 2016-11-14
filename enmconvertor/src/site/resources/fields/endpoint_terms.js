var t_endpoints =
{
	"epithelial barrier integrity": {
		"file": ["INVITRO_BARRIERCROSSING.xlsx"],
		"term": {
			"enm": {
				"score": 22.87,
				"label": "Barrier integrity",
				"uri": "http://purl.enanomapper.org/onto/ENM_0000044"
			},
			"chmo": {
				"score": 10.34,
				"label": "potential barrier field-flow fractionation",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001631"
			}
		},
		"abbr": "BARRIERCROSSING",
		"value": "epithelial barrier integrity"
	},
	"biodistribution": {
		"file": ["INVIVO_BIODISTRIBUTION.xlsx"],
		"term": {
		},
		"abbr": "BIODISTRIBUTION",
		"value": "Biodistribution"
	},
	"pulmonary deposition": {
		"file": ["INVIVO_PULMONARY.xlsx"],
		"term": {
			"ncit": {
				"score": 12.31,
				"label": "Adipose Tissue Deposition",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C113303"
			},
			"enm": {
				"score": 11.40,
				"label": "sputter deposition",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1937"
			},
			"chmo": {
				"score": 6.92,
				"label": "gas jet deposition",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001351"
			}
		},
		"abbr": "PULMONARY",
		"value": "Pulmonary deposition"
	},
	"crystalline phase": {
		"file": ["PCHEM_CRYSTALLINEPHASE.xlsx"],
		"term": {
			"ncit": {
				"score": 11.48,
				"label": "Crystalline Lens Dislocation",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C118864"
			},
			"enm": {
				"score": 11.90,
				"label": "crystalline state",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1512"
			},
			"chmo": {
				"score": 9.09,
				"label": "crystallisation",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001477"
			}
		},
		"abbr": "CRYSTALLINEPHASE",
		"value": "Crystalline phase"
	},
	"systemic genotoxicity": {
		"file": ["INVIVO_TOXLONGTERM.xlsx"],
		"term": {
			"ncit": {
				"score": 16.85,
				"label": "Genotoxicity: In Vitro",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C79378"
			},
			"enm": {
				"score": 12.73,
				"label": "genotoxicity assay",
				"uri": "http://www.bioassayontology.org/bao#BAO_0002167"
			}
		},
		"abbr": "TOXLONGTERM",
		"value": "systemic genotoxicity"
	},
	"ecotoxicity": {
		"file": ["INVIVO_ECOTOX.xlsx"],
		"abbr": "ECOTOX",
		"value": "Ecotoxicity"
	},
	"viability": {
		"file": ["INVITRO_VIABILITY.xlsx"],
		"term": {
			"ncit": {
				"score": 10.80,
				"label": "Colorimetric Cell Viability Assay",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C95027"
			},
			"enm": {
				"score": 11.99,
				"label": "percent cell viability",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1816"
			}
		},
		"abbr": "VIABILITY",
		"value": "Viability"
	},
	"elemental composition and chemical purity": {
		"file": ["PCHEM_COMPOSITIONCHEMICAL.xlsx"],
		"term": {
			"ncit": {
				"score": 15.66,
				"label": "Purity",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C62352"
			},
			"enm": {
				"score": 16.61,
				"label": "percent purity",
				"uri": "http://www.bioassayontology.org/bao#BAO_0002132"
			},
			"chmo": {
				"score": 14.99,
				"label": "elemental analysis",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001075"
			}
		},
		"abbr": "COMPOSITIONCHEMICAL",
		"value": "elemental composition and chemical purity"
	},
	"nanomaterial deposition / aerosol characterisation": {
		"file": ["INVITRO_NMDEPOSITION.xlsx"],
		"term": {

			"chmo": {
				"score": 15.06,
				"label": "aerosol-assisted chemical vapour deposition",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001315"
			}
		},
		"abbr": "NMDEPOSITION",
		"value": "Nanomaterial deposition / aerosol characterisation"
	},
	"immunotoxicity": {
		"file": ["INVIVO_IMMUNOTOX.xlsx"],
		"term": {
			"ncit": {
				"score": 16.86,
				"label": "Immunotoxicity",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C63814"
			},
			"enm": {
				"score": 14.75,
				"label": "immunotoxicity",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1339"
			}
		},
		"abbr": "IMMUNOTOX",
		"value": "Immunotoxicity"
	},
	"occular irritation": {
		"file": ["INVITRO_OCCULARIRRITATON.xlsx"],
		"term": {
			"enm": {
				"score": 9.56,
				"label": "irritation AE",
				"uri": "http://purl.obolibrary.org/obo/OAE_0002023"
			}
		},
		"abbr": "OCCULARIRRITATON",
		"value": "Occular irritation"
	},
	"cytotoxicity": {
		"file": ["INVITRO_CYTOTOXICITY.xlsx"],
		"term": {
			"ncit": {
				"score": 9.03,
				"label": "Cell-Mediated Cytotoxicity",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17709"
			},
			"enm": {
				"score": 11.62,
				"label": "cytotoxicity",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1340"
			}
		},
		"abbr": "CYTOTOXICITY",
		"value": "Cytotoxicity"
	},
	"organ burden": {
		"file": ["INVIVO_ORGANBURDEN.xlsx"],
		"term": {
			"enm": {
				"score": 10.07,
				"label": "in vivo assay measuring T cell epitope specific protection from infectious challenge based on pathogen burden",
				"uri": "http://purl.obolibrary.org/obo/OBI_0001475"
			}
		},
		"abbr": "ORGANBURDEN",
		"value": "Organ burden"
	},
	"isoelectric point zeta potential": {
		"file": ["PCHEM_IEP.xlsx"],
		"term": {
			"ncit": {
				"score": 17.05,
				"label": "Isoelectric Focusing",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C16758"
			},
			"enm": {
				"score": 23.74,
				"label": "isoelectric point",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1204"
			},
			"chmo": {
				"score": 17.59,
				"label": "zeta-potential measurement",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0002123"
			}
		},
		"abbr": "IEP",
		"value": "IsoElectric Point Zeta potential"
	},
	"surface chemistry": {
		"file": ["PCHEM_SURFACECHEMISTRY.xlsx"],
		"term": {
			"ncit": {
				"score": 14.77,
				"label": "Surface Chemistry",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C64351"
			},
			"enm": {
				"score": 16.36,
				"label": "surface chemistry assay",
				"uri": "http://purl.enanomapper.org/onto/ENM_9000009"
			}
		},
		"abbr": "SURFACECHEMISTRY",
		"value": "Surface chemistry"
	},
	"density": {
		"file": ["PCHEM_DENSITY.xlsx"],
		"term": {
			"enm": {
				"score": 8.59,
				"label": "density modifier",
				"uri": "http://purl.enanomapper.org/onto/ENM_8000183"
			},
			"chmo": {
				"score": 7.62,
				"label": "density-gradient centrifugation",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0002017"
			}
		},
		"abbr": "DENSITY",
		"value": "Density"
	},
	"air liquid interface": {
		"file": ["INVIVO_ALI.xlsx"],
		"term": {
			"enm": {
				"score": 10.06,
				"label": "barrier",
				"uri": "http://purl.enanomapper.org/onto/ENM_8000173"
			},
			"chmo": {
				"score": 11.37,
				"label": "foam fractionation",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001636"
			}
		},
		"abbr": "ALI",
		"value": "Air liquid interface"
	},
	"pattern distribution in organs": {
		"file": ["INVIVO_PATTERNDISTRIBUTION.xlsx"],
		"term": {
			"ncit": {
				"score": 12.46,
				"label": "Substance Distribution",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71876"
			},
			"enm": {
				"score": 9.04,
				"label": "volume of distribution",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1524"
			},
			"chmo": {
				"score": 5.77,
				"label": "sample dispersion",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001525"
			}
		},
		"abbr": "PATTERNDISTRIBUTION",
		"value": "pattern distribution in organs"
	},
	"zeta potential": {
		"file": ["PCHEM_ZETAPOTENTIAL.xlsx"],
		"term": {
			"enm": {
				"score": 22.92,
				"label": "zeta potential",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1302"
			},
			"chmo": {
				"score": 17.59,
				"label": "zeta-potential measurement",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0002123"
			}
		},
		"abbr": "ZETAPOTENTIAL",
		"value": "zeta potential"
	},
	"broncho alveolar lavage": {
		"file": ["INVIVO_BAL.xlsx"],
		"term": {
			"ncit": {
				"score": 16.60,
				"label": "Broncho-Esophageal Fistula",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C35327"
			},
			"enm": {
				"score": 6.54,
				"label": "hypoventilation AE",
				"uri": "http://purl.obolibrary.org/obo/OAE_0000587"
			}
		},
		"abbr": "BAL",
		"value": "Broncho alveolar Lavage"
	},
	"potentiometry": {
		"file": ["PCHEM_POTENTIOMETRY.xlsx"],
		"term": {
			"chmo": {
				"score": 8.04,
				"label": "controlled-current potentiometry",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0000021"
			}
		},
		"abbr": "POTENTIOMETRY",
		"value": "Potentiometry"
	},
	"particle size distribution (granulometry)": {
		"file": ["PCHEM_SIZE.xlsx"],
		"term": {
			"ncit": {
				"score": 25.58,
				"label": "Particle Count and Size Analyzer",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C62330"
			},
			"enm": {
				"score": 26.54,
				"label": "particle size distribution",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1699"
			},
			"chmo": {
				"score": 20.31,
				"label": "granulometry",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0002119"
			}
		},
		"abbr": "SIZE",
		"value": "Particle size distribution (granulometry)"
	},
	"batch dispersion/stability": {
		"file": ["PCHEM_BATCHDISPERSION.xlsx"],
		"term": {
			"ncit": {
				"score": 14.27,
				"label": "Dispersion",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53321"
			},
			"enm": {
				"score": 13.57,
				"label": "dispersion",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1969"
			},
			"chmo": {
				"score": 9.91,
				"label": "batch injection calorimetry",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001227"
			}
		},
		"abbr": "BATCHDISPERSION",
		"value": "batch dispersion/stability"
	},
	"volume specific surface area (porosity)": {
		"file": ["PCHEM_VSSA.xlsx"],
		"term": {
			"ncit": {
				"score": 17.44,
				"label": "Initial Volume of Distribution Normalized by Surface Area",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C102374"
			},
			"enm": {
				"score": 21.02,
				"label": "specific surface area",
				"uri": "http://semanticscience.org/resource/CHEMINF_000515"
			}
		},
		"abbr": "VSSA",
		"value": "Volume specific surface area (porosity)"
	},
	"cell transformation": {
		"file": ["INVITRO_CTA.xlsx"],
		"term": {
			"ncit": {
				"score": 12.61,
				"label": "Transformation",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C17212"
			},
			"chmo": {
				"score": 9.59,
				"label": "sample transformation method-detection method",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0001792"
			}
		},
		"abbr": "CTA",
		"value": "Cell transformation"
	},
	"genotoxicity": {
		"file": ["INVIVO_GENOTOX.xlsx"],
		"term": {
			"ncit": {
				"score": 16.85,
				"label": "Genotoxicity: In Vitro",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C79378"
			},
			"enm": {
				"score": 12.73,
				"label": "genotoxicity assay",
				"uri": "http://www.bioassayontology.org/bao#BAO_0002167"
			}
		},
		"abbr": "GENOTOX",
		"value": "Genotoxicity"
	},
	"dustiness": {
		"file": ["PCHEM_DUSTINESS.xlsx"],
		"term": {
			"enm": {
				"score": 11.97,
				"label": "dustiness",
				"uri": "http://purl.enanomapper.org/onto/ENM_9000003"
			}
		},
		"abbr": "DUSTINESS",
		"value": "Dustiness"
	},
	"genotoxicity - inhalation": {
		"file": ["INVIVO_INHALATION.xlsx"],
		"term": {
			"ncit": {
				"score": 16.85,
				"label": "Genotoxicity: In Vitro",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C79378"
			},
			"enm": {
				"score": 12.73,
				"label": "genotoxicity assay",
				"uri": "http://www.bioassayontology.org/bao#BAO_0002167"
			},
			"chmo": {
				"score": 10.25,
				"label": "inhalation of poisonous fumes",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0010018"
			}
		},
		"abbr": "INHALATION",
		"value": "genotoxicity - inhalation"
	},
	"physiologically based pharmacokinetic modelling (pbpk)": {
		"file": ["INVIVO_PBPK.xlsx"],
		"term": {
			"enm": {
				"score": 13.80,
				"label": "pharmacokinetic parameter",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_1530"
			}

		},
		"abbr": "PBPK",
		"value": "Physiologically based pharmacokinetic modelling (PBPK)"
	},
	"solubility": {
		"file": ["PCHEM_SOLUBILITY.xlsx"],
		"term": {
			"ncit": {
				"score": 12.45,
				"label": "Solubility",
				"uri": "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C60821"
			},
			"enm": {
				"score": 11.98,
				"label": "solubility in water",
				"uri": "http://purl.bioontology.org/ontology/npo#NPO_500"
			},
			"chmo": {
				"score": 8.34,
				"label": "solubility",
				"uri": "http://purl.obolibrary.org/obo/CHMO_0002815"
			}
		},
		"abbr": "SOLUBILITY",
		"value": "Solubility"
	}
}
;