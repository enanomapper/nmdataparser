package net.enanomapper.parser.excel;

import java.io.File;

public class MiscUtils 
{
	public static File getFirstFile(File dirPath, boolean recursive) {
		File filesList[] = dirPath.listFiles();
		for(File file : filesList) {
			if(file.isFile()) 
				return file;
			else {
				if (recursive) {
					File recRes =  getFirstFile(file, true);
					if (recRes != null)
						return recRes;
				}			
			}	
		}
		return null;
	}
}
