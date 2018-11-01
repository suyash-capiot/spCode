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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class PipelineReportActivityModifier implements FileVisitor<Path> {
	
	private ArrayList<Pattern> exclPatterns;
	private Path sourcePath;
	private String sourceFullPath;
	private static final String PIPELINE_FILE_EXT = ".pipeline";
	private DocumentBuilder docBldr;
	private Transformer xformer;
	private static Pattern intfcSvcPattern = Pattern.compile(".*InterfaceService/.*");
	
	private static final String REF_FIELDS_DEF = "<root>\n" + 
                                                       "<SessionID>{$body/NS_PREFIX:SCHEMA_PREFIXBusinessServiceRQ/NS_PREFIX:RequestHeader/com:SessionID}</SessionID>\n" +
                                                       "<TxnID>{$body/NS_PREFIX:SCHEMA_PREFIXBusinessServiceRQ/NS_PREFIX:RequestHeader/com:TransactionID}</TxnID>\n" +
                                                       "<UserID>{$body/NS_PREFIX:SCHEMA_PREFIXBusinessServiceRQ/NS_PREFIX:RequestHeader/com:UserID}</UserID>\n" +
                                                       "<SuppID>{$body/NS_PREFIX:SCHEMA_PREFIXBusinessServiceRQ/NS_PREFIX:RequestHeader/com:SupplierCredentialsList/com:SupplierCredentials[1]/com:SupplierID}</SuppID>\n" +
                                                       "OPERATION_DEFINITIONS\n" +
                                                       "</root>";
	
	private static final String CS_OPERATIONS = 
            "<SUPPLIER_RQ>SUPPLIER_RQ</SUPPLIER_RQ>\n" +
            "<SUPPLIER_RS>SUPPLIER_RS</SUPPLIER_RS>\n" +
            "<SUPPLIER_ERR>SUPPLIER_ERR</SUPPLIER_ERR>\n" +
            "<CS_ERR>CS_ERR</CS_ERR>";

	private static final String IS_OPERATIONS = 
            "<FLOW_RQ>FLOW_RQ</FLOW_RQ>\n" +
            "<FLOW_RS>FLOW_RS</FLOW_RS>\n" +
            "<FLOW_CS_ERR>FLOW_CS_ERR</FLOW_CS_ERR>\n" +
            "<FLOW_ERR>FLOW_ERR</FLOW_ERR>";

	private PipelineReportActivityModifier(Path srcPath) {
		sourcePath = srcPath;
		sourceFullPath = sourcePath.toFile().getAbsolutePath();
		loadExclusionPatterns();
		try {
			DocumentBuilderFactory docBldrFact = DocumentBuilderFactory.newInstance();
			docBldrFact.setNamespaceAware(true);
			docBldr = docBldrFact.newDocumentBuilder();
			
			TransformerFactory xformerFact = TransformerFactory.newInstance();
			xformer = xformerFact.newTransformer();
		}
		catch (ParserConfigurationException pcx) {
			throw new RuntimeException("Could not instantiate XML document builder");
		} 
		catch (TransformerConfigurationException e) {
			throw new RuntimeException("Could not instantiate XML transformer");
		}
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
				System.out.println("FILE_NAME,STATUS,ASSIGN_COUNT,XQUERY_COUNT,REPORT_COUNT,NS_PREFIX,ERROR_MESSAGE");
				Files.walkFileTree(srcPath, new PipelineReportActivityModifier(srcPath));
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
			InputStream inStrm = clsLdr.getResourceAsStream("PipelineReportActivityModifierExclusions.txt");
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
		String status = "", nsPrefix = "", errorMsg = "";
		int assignCount=0, reportCount=0, xqueryCount = 0;
		
		File currFile = file.toFile();
		String currFilePath = currFile.getAbsolutePath();
		String currFileName = currFile.getName();
		
		if (currFileName.toLowerCase().endsWith(PIPELINE_FILE_EXT)) {
			if (isExcluded(file)) {
				status = "EXCLUDED";
			}
			else {
				try {
					String refFldsName = "ReferenceFields";
					Document pipelineDoc = docBldr.parse(currFile);
					Element rootElem = (Element) pipelineDoc.getDocumentElement();
					NodeList assignList = rootElem.getElementsByTagNameNS("http://www.bea.com/wli/sb/stages/transform/config", "assign");
					for (int i=0; i < assignList.getLength(); i++) {
						Element assignElem = (Element) assignList.item(i);
						String varNameAttr = assignElem.getAttribute("varName");
						if (varNameAttr.equals("ReferenceFields") || varNameAttr.equals("RefFields")) {
							refFldsName = varNameAttr;
							assignCount++;
							NodeList xqryTextList = assignElem.getElementsByTagNameNS("http://www.bea.com/wli/sb/stages/config", "xqueryText");
							xqueryCount = xqryTextList.getLength();
							for (int j=0; j < xqryTextList.getLength(); j++) {
								Element xqryTextElem = (Element) xqryTextList.item(j);
								String[] prefixes = getProductNSAndSchemaPrefix(currFilePath);
								nsPrefix = prefixes[0];
								String schemaPrefix = prefixes[1]; 
								String opDefs = (intfcSvcPattern.matcher(currFilePath.replaceAll("\\\\", "/")).matches()) ? IS_OPERATIONS : CS_OPERATIONS;
								String newVal = REF_FIELDS_DEF.replaceAll("NS_PREFIX", nsPrefix).replaceAll("SCHEMA_PREFIX", schemaPrefix).replaceAll("OPERATION_DEFINITIONS", opDefs);
								xqryTextElem.getFirstChild().setNodeValue(newVal);
							}
						}
					}
	
					NodeList reportList = rootElem.getElementsByTagNameNS("http://www.bea.com/wli/sb/stages/logging/config", "report");
					reportCount = reportList.getLength();
					for (int i=0; i < reportList.getLength(); i++) {
						Element reportElem = (Element) reportList.item(i);
						String baseIndent = reportElem.getLastChild().getNodeValue();
						
						Element labElem = getLabelsElementForSuppID(pipelineDoc, baseIndent, refFldsName);
						reportElem.insertBefore(getIndentText(pipelineDoc, baseIndent, 1), reportElem.getLastChild());
						reportElem.insertBefore(labElem, reportElem.getLastChild());
					}
					
					if (assignCount > 0 || reportCount > 0) {
						DOMSource domSrc = new DOMSource(pipelineDoc);
						StreamResult strmRes = new StreamResult(currFile);
						xformer.transform(domSrc, strmRes);
						status = "CHANGED";
					}
					else {
						status = "UNCHANGED";
					}
	
				}
				catch (Exception x) {
					x.printStackTrace();
					status = "ERROR";
					errorMsg = x.getMessage();
				}
			}
			
			System.out.printf("%s,%s,%d,%d,%d,%s,%s\n", currFilePath, status, assignCount, xqueryCount, reportCount, nsPrefix, errorMsg);
		}
		
		
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
		return FileVisitResult.CONTINUE;
	}
	
	private String[] getProductNSAndSchemaPrefix(String currFilePath) {
		String fileWOSrcDir = currFilePath.substring(sourceFullPath.length() + 1).toLowerCase();
		if (fileWOSrcDir.startsWith("acco")) {
			return new String[] {"acco", "Acco"};
		}
		if (fileWOSrcDir.startsWith("act")) {
			return new String[] {"sig", "Sightseeing"};
		}
		if (fileWOSrcDir.startsWith("air")) {
			return new String[] {"air", "Air"};
		}
		if (fileWOSrcDir.startsWith("bus")) {
			return new String[] {"bus", "Bus"};
		}
		if (fileWOSrcDir.startsWith("car")) {
			return new String[] {"car", "CarRentals"};
		}
		if (fileWOSrcDir.startsWith("cruise")) {
			return new String[] {"cru", "Cruise"};
		}
		if (fileWOSrcDir.startsWith("flighttracker")) {
			return new String[] {"air", "Air"};
		}
		if (fileWOSrcDir.startsWith("insur")) {
			return new String[] {"ins", "Insurance"};
		}
		if (fileWOSrcDir.startsWith("pkgs")) {
			return new String[] {"pac", "Packages"};
		}
		if (fileWOSrcDir.startsWith("private")) {
			return new String[] {"acco", "Acco"};
		}
		if (fileWOSrcDir.startsWith("sight")) {
			return new String[] {"sig", "Sightseeing"};
		}
		if (fileWOSrcDir.startsWith("trans")) {
			return new String[] {"tran", "Transfers"};
		}
		if (fileWOSrcDir.startsWith("visa")) {
			return new String[] {"visa", "Visa"};
		}
		
		return new String[] {"unk", "Unknown"};
	}

	
	private Element getLabelsElementForSuppID(Document pipelineDoc, String baseIndent, String varNameAttr) {

		Element labelsElem = pipelineDoc.createElementNS("http://www.bea.com/wli/sb/stages/logging/config", "labels");
		
		labelsElem.setPrefix("con3");
		Element keyElem = pipelineDoc.createElementNS("http://www.bea.com/wli/sb/stages/logging/config", "key");
		keyElem.setPrefix("con3");
		Text keyElemValNode = pipelineDoc.createTextNode("SuppID");
		keyElem.appendChild(keyElemValNode);
		Element varNameElem = pipelineDoc.createElementNS("http://www.bea.com/wli/sb/stages/logging/config", "varName");
		varNameElem.setPrefix("con3");
		Text varNameElemValNode = pipelineDoc.createTextNode(varNameAttr);
		varNameElem.appendChild(varNameElemValNode);
		Element valueElem = pipelineDoc.createElementNS("http://www.bea.com/wli/sb/stages/logging/config", "value");
		valueElem.setPrefix("con3");
		Element xpathTextElem = pipelineDoc.createElementNS("http://www.bea.com/wli/sb/stages/config", "xpathText");
		xpathTextElem.setPrefix("con2");
		Text xpathTextElemValNode = pipelineDoc.createTextNode("/root/SuppID");
		xpathTextElem.appendChild(xpathTextElemValNode);
		valueElem.appendChild(getIndentText(pipelineDoc, baseIndent, 3));
		valueElem.appendChild(xpathTextElem);
		valueElem.appendChild(getIndentText(pipelineDoc, baseIndent, 2));
		
		labelsElem.appendChild(getIndentText(pipelineDoc, baseIndent, 2));
		labelsElem.appendChild(keyElem);
		labelsElem.appendChild(getIndentText(pipelineDoc, baseIndent, 2));
		labelsElem.appendChild(varNameElem);
		labelsElem.appendChild(getIndentText(pipelineDoc, baseIndent, 2));
		labelsElem.appendChild(valueElem);
		labelsElem.appendChild(getIndentText(pipelineDoc, baseIndent, 1));

		return labelsElem;
	}
	
	private Text getIndentText(Document pipelineDoc, String baseIndent, int addIndent) {
		StringBuilder indentBldr = new StringBuilder(baseIndent);
		for (int i=0; i < addIndent; i++) {
			indentBldr.append("    ");
		}
		return pipelineDoc.createTextNode(indentBldr.toString());
	}
}
