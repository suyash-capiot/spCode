import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class BusinessServicesChecker implements FileVisitor<Path> {
	
	private ArrayList<Pattern> exclPatterns;
	private Path sourcePath;
	private static final String BS_FILE_EXT = ".bix";
	private static final String HTTP_PROXY_DEFN = "<http:proxy-server ref=\"System/Proxy Servers/CNK_ProxyServer\"/>";
	
	private BusinessServicesChecker(Path srcPath) {
		sourcePath = srcPath;
		loadExclusionPatterns();
	}

	public static boolean isValidInput(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage:");
			System.out.println("\tjava BusinessServicesChecker <Source_Dir_for_OSB>");
			return false;
		}
		
		File sourceDir = new File(args[0]);
		if (sourceDir.exists() == false) {
			System.out.println("The source directory for OSB does not exist. Cannot proceed further!");
			return false;
		}
		if (sourceDir.isDirectory() == false) {
			System.out.println("The source directory for OSB is not a directory. Cannot proceed further!");
			return false;
		}
		
		return true;
	}

	public static void main(String[] args) {
		if (isValidInput(args)) {
			Path srcPath = FileSystems.getDefault().getPath(args[0]);

			try {
				Files.walkFileTree(srcPath, new BusinessServicesChecker(srcPath));
			}
			catch (Exception x) {
				x.printStackTrace();
			}
		}
		System.exit(0);
	}

	@Override
	public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
		return FileVisitResult.CONTINUE;	
	}

	public boolean isExcluded(Path file) {
		String currFilePath = file.toFile().getAbsolutePath().replaceAll("\\\\", "/");
		for (Pattern ptn : exclPatterns) {
			if (ptn.matcher(currFilePath).matches()) {
				return true;
			}
		}
		
		return false;
	}
	
	private void loadExclusionPatterns() {
		exclPatterns = new ArrayList<Pattern>();
		try {
			BufferedReader buffRdr = null;
			ClassLoader clsLdr = this.getClass().getClassLoader();
			InputStream inStrm = clsLdr.getResourceAsStream("BusinessServicesCheckerExclusions.txt");
			if (inStrm != null) {
				try {
					buffRdr = new BufferedReader(new InputStreamReader(inStrm));
					String line = null;
					while ( (line = buffRdr.readLine()) != null ) {
						if (line.length() > 0 && line.startsWith("#") == false) {
							exclPatterns.add(Pattern.compile(line));
						}
					}
				}
				catch (Exception x) {
					x.printStackTrace();
				}
				finally {
					if (buffRdr != null) {
						try { buffRdr.close(); } 
						catch (Exception x) { }
					}
				}
			}
		}
		catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes fileAttrs) throws IOException {
		File currFile = file.toFile();
		String currFilePath = currFile.getAbsolutePath();
		String currFileName = currFile.getName();
		if (currFileName.toLowerCase().endsWith(BS_FILE_EXT) && isExcluded(file) == false) {
			BufferedReader buffRdr = new BufferedReader(new FileReader(currFilePath));
			char[] charBuff = new char[(int) fileAttrs.size()];
			try {
				if (buffRdr.read(charBuff, 0, (int) fileAttrs.size()) > 0) {
					String fileContents = new String(charBuff);
					if ( fileContents.indexOf(HTTP_PROXY_DEFN) == -1) {
						System.out.println(currFilePath);
					}
				}
			}
			catch (Exception x) {
				x.printStackTrace();
			}
			finally {
				if (buffRdr != null) {
					try { buffRdr.close(); }
					catch (Exception ex) { }
				}
			}
		}
		
		
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
		return FileVisitResult.CONTINUE;
	}

}
