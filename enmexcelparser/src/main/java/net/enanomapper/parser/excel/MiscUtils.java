package net.enanomapper.parser.excel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MiscUtils 
{
	public static interface IHandleFile {
		public void handle(File file) throws Exception;
	}
	
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
		if (fileExtensions == null || fileExtensions.length == 0)
			return true;
		
		String filePath = file.getAbsolutePath().toLowerCase();
		for (String s : fileExtensions)
			if (filePath.endsWith(s.toLowerCase()))
				return true;
		return false;
	}
	
	
	public static void iterateFiles(File dirPath, String[] fileExtensions, 
				boolean recursive, IHandleFile handler, boolean handleFolders) throws Exception 
	{
		if (!dirPath.isDirectory())
		{	
			//Just a single file is processed
			if (checkFileExtension(dirPath, fileExtensions))
				handler.handle(dirPath);
			return;
		}	
		
		File filesList[] = dirPath.listFiles();
		
		if (handleFolders)
			handler.handle(dirPath);
			
		for(File file : filesList) {
			if(file.isFile()) {
				if (checkFileExtension(file, fileExtensions))
					handler.handle(file);
			}	
			else {
				if (recursive) 
					iterateFiles(file, fileExtensions, true, handler, handleFolders);							
			}	
		}
	}
	
	public static void iterateFiles_BreadthFirst(File dirPath, String[] fileExtensions, 
			boolean recursive, IHandleFile handler, boolean handleFolders) throws Exception 
	{
		if (!dirPath.isDirectory()) 
		{
			//Just a single file is processed
			if (checkFileExtension(dirPath, fileExtensions))
				handler.handle(dirPath);
			return;
		}	
		
		File filesList[] = dirPath.listFiles();
		//All folders are stored here and processed later
		List<File> recFiles = null; 
		
		if (recursive)
			recFiles = new ArrayList<File>();
		
		if (handleFolders)
			handler.handle(dirPath);
		
		for(File file : filesList) {
			if(file.isFile()) {
				if (checkFileExtension(file, fileExtensions))
					handler.handle(file);
			}	
			else {
				if (recursive) 
					recFiles.add(file);						
			}
		}
		
		if (recursive)
		{
			for (File file : recFiles)
				iterateFiles_BreadthFirst(file, fileExtensions, true, handler, handleFolders);
		}
	}
	
	
}
