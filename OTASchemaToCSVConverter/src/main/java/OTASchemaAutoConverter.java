import java.io.File;
import java.io.FilenameFilter;

import javax.xml.namespace.QName;

public class OTASchemaAutoConverter {

	private static final String DEFAULT_OTA_NS = "http://www.opentravel.org/OTA/2003/05";
	private static String pathSeparator = System.getProperty("file.separator");
	
	public static boolean isValidInput(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage:");
			System.out.println("\tOTASchemaAutoConverter <Directory_for_OTA_Schemas> <CSV_Output_Directory>");
			return false;
		}
		
		File sourceDir = new File(args[0]);
		if (sourceDir.exists() == false) {
			System.out.println("The directory for OTA schemas does not exist. Cannot proceed further!");
			return false;
		}
		if (sourceDir.isDirectory() == false) {
			System.out.println("The directory for OTA schemas is not a directory. Cannot proceed further!");
			return false;
		}
		
		File targetDir = new File(args[1]);
		if (targetDir.exists() == false) {
			System.out.println("The directory for CSV files does not exist. Cannot proceed further!");
			return false;
		}
		if (targetDir.isDirectory() == false) {
			System.out.println("The directory for CSV files is not a directory. Cannot proceed further!");
			return false;
		}

		return true;
	}
	
	public static String checkAndAddPathSeparatorToDir(String dirName) {
		return (dirName.endsWith(pathSeparator) == false) ? (dirName += pathSeparator) : dirName; 
	}
	
	public static void main(String[] args) {
		if (isValidInput(args)) {
			File sourceDir = new File(args[0]);
			String[] fileList = sourceDir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.toLowerCase().endsWith("rq.xsd") || name.toLowerCase().endsWith("rs.xsd"));
				}
			});
			
			String schemaFileDir = checkAndAddPathSeparatorToDir(args[0]);
			String outputDir = checkAndAddPathSeparatorToDir(args[1]);
			
			for (String fileName : fileList) {
				String filePath = schemaFileDir + fileName; 
				String schemaName = fileName.substring(0, fileName.toLowerCase().lastIndexOf(".xsd"));
				QName schemaQName = new QName(DEFAULT_OTA_NS, schemaName);
				String outputFile = outputDir + schemaName + ".csv";
				try {
					OTASchemaTraverser.exportSchemaToCSV(filePath, schemaQName, outputFile);
				}
				catch (Exception x) {
					System.out.println("Failed to export " + fileName + " to CSV.");
					x.printStackTrace();
				}
			}
			
		}
		
		System.exit(0);
	}

}
