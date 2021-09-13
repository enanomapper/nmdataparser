package net.enanomapper.parser.excel;

import java.io.File;

public class MiscUtils 
{
	public static File getFirstFile(File dirPath, String[] fileExtensions, boolean recursive) {
		File filesList[] = dirPath.listFiles();
		for(File file : filesList) {
			if(file.isFile()) {
				if (checkFileExtension(file, fileExtensions))
					return file;
			}	
			else {
				if (recursive) {
					File recRes =  getFirstFile(file, fileExtensions, true);
					if (recRes != null)
						return recRes;
				}			
			}	
		}
		return null;
	}
	
	public static boolean checkFileExtension(File file, String[] fileExtensions) {
		String filePath = file.getAbsolutePath().toLowerCase();
		for (String s : fileExtensions)
			if (filePath.endsWith(s.toLowerCase()))
				return true;
		return false;
	}
}
