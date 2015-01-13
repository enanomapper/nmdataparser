package net.enanomapper.parser;

import java.io.File;
import java.io.FileReader;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;
import net.idea.loom.nm.csv.CSV12Reader;
import net.idea.loom.nm.csv.CSV12SubstanceReader;
import ambit2.base.data.ILiteratureEntry._type;
import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;


/**
 * 
 * @author nina
 *
 */
public class eNMParser {
	static Logger logger = Logger.getLogger(eNMParser.class.getName());
	static final String loggingProperties = "config/logging.prop";
	
	CliOptions options;
	
	public eNMParser(CliOptions options) {
		this.options = options;
	
	}
	
	public long go(String command,String subcommand) throws Exception {
		if ("csv".equals(command)) {
			File file = new File(options.input);
			RawIteratingWrapper reader = null;
			try {
				LiteratureEntry entry = new LiteratureEntry("New test","http://example.com");
	    		entry.setType(_type.Dataset);
				CSV12Reader chemObjectReader = new CSV12Reader(new FileReader(file),entry,"TEST-");
				reader = new CSV12SubstanceReader(chemObjectReader);
				int r = 0;
				while (reader.hasNext()) {
					IStructureRecord mol = reader.nextRecord();
					SubstanceRecord substance = (SubstanceRecord)mol;
					System.out.println(substance.getPublicName());
					System.out.println(substance.getMeasurements());
					r++;
				}
			} finally {
				reader.close();
			}
		} else if ("nanowiki".equals(command)) {
			throw new InvalidCommand(command);
		} else if ("xlsx".equals(command)) {
			throw new InvalidCommand(command);			
		} else {
			throw new InvalidCommand(command);
		}

		return -1;
	}
	
	public static void main(String[] args) {
		long now = System.currentTimeMillis();
		try {
			CliOptions options = new CliOptions();
			if (options.parse(args)) {
				eNMParser navigator = new eNMParser(options);
				logger.info(String.format("Running %s (%s)\nSubcommand:\t%s:%s",
						options.getCommand().get("name"),options.getCommand().get("connection"),
						options.getSubcommand(),options.getCommand().get(options.getSubcommand().name())));
				navigator.go(options.getCmd(),options.getSubcommand().name());
			} else 
				System.exit(0);
		} catch (ConnectException x) {
			logger.log(Level.SEVERE,"Connection refused. Please verify if the remote server is responding.");
			System.exit(-1);
		} catch (SQLException x) {
			logger.log(Level.SEVERE,"SQL error",x);
			System.exit(-1);		
		} catch (Exception x ) {
			logger.log(Level.WARNING,x.getMessage(),x);
			System.exit(-1);
		} finally {
			logger.info(String.format("Completed in %s msec", (System.currentTimeMillis()-now)));
			logger.info("Done.");
		}
	}
	

}
