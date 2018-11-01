import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.xs.*;
import org.apache.xerces.xs.*;

public class OTASchemaTraverser {

	private static StringBuilder offsetBldr = new StringBuilder();
	private static final String tabChar = "....";
	
	public static void traverseAttributeUse(XSAttributeUse attrUse) {
		XSAttributeDeclaration attrDecl = attrUse.getAttrDeclaration();
		System.out.printf("\n%s@%s, %s, %s", offsetBldr.toString(), attrDecl.getName(), ((attrUse.getRequired()) ? "required" : "optional"), attrDecl.getTypeDefinition().getName());
	}
	
	public static void traverseComplexType(XSComplexTypeDefinition cxTypeDef) {
		XSObjectList attrList = cxTypeDef.getAttributeUses();
		ListIterator<XSAttributeUse> attrIter = (ListIterator<XSAttributeUse>) attrList.listIterator();
		while (attrIter.hasNext()) {
			traverseAttributeUse(attrIter.next());
		}
		short contentType = cxTypeDef.getContentType();
		switch (contentType) {
			case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
				XSTypeDefinition contentTypeDef = cxTypeDef.getBaseType();
				if (contentTypeDef instanceof XSSimpleTypeDefinition) {
					traverseSimpleType((XSSimpleTypeDefinition) contentTypeDef);
				}
				break;
				
			case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
			case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
				//System.out.printf(", complexType");
				XSParticle xsPrtcl = cxTypeDef.getParticle();
				if (xsPrtcl != null) {
					XSTerm xsTerm = xsPrtcl.getTerm();
					if (xsTerm instanceof XSModelGroup) {
						traverseModelGroup((XSModelGroup) xsTerm);
					}
				}
				break;
		}
	}
	
	public static void traverseModelGroup(XSModelGroup xsModelGrp) {
		switch(xsModelGrp.getCompositor()) {
			case XSModelGroup.COMPOSITOR_ALL:
				System.out.printf("\n%sALL", offsetBldr.toString());
				break;
			case XSModelGroup.COMPOSITOR_CHOICE:
				System.out.printf("\n%sCHOICE", offsetBldr.toString());
				break;
			case XSModelGroup.COMPOSITOR_SEQUENCE:
				System.out.printf("\n%sSEQUENCE", offsetBldr.toString());
				break;
		}
		
		offsetBldr.append(tabChar);
		XSObjectList partObjList = xsModelGrp.getParticles();
		ListIterator<XSParticle> listIter = (ListIterator<XSParticle>) partObjList.listIterator();
		while (listIter.hasNext()) {
			XSParticle obj = listIter.next();
			XSTerm partTerm = obj.getTerm();
			if (partTerm instanceof XSElementDeclaration) {
				traverseElementDecl((XSElementDeclaration) partTerm, obj.getMinOccurs(), obj.getMaxOccurs(), obj.getMaxOccursUnbounded());
			}
			else if (partTerm instanceof XSModelGroup) {
				traverseModelGroup((XSModelGroup) partTerm);
			}
		}
		offsetBldr.replace(offsetBldr.lastIndexOf(String.valueOf(tabChar)), offsetBldr.length(), "");
		
	}
	
	public static void traverseSimpleType(XSSimpleTypeDefinition xsSmplType) {
		//System.out.printf(", %s", xsSmplType.getName());
	}
	
	public static void traverseElementDecl(XSElementDeclaration xsElemDecl, int minOccurs, int maxOccurs, boolean isUnbounded) {
		XSTypeDefinition elemTypeDef = xsElemDecl.getTypeDefinition();
		if (elemTypeDef instanceof XSComplexTypeDefinition) {
			System.out.printf("\n%s%s, %s, %s, %d, %s", 
					offsetBldr.toString(), 
					xsElemDecl.getName(), 
					((minOccurs < 1) ? "optional" : "required"), 
					"complexType",
					minOccurs, ((isUnbounded) ? "unbounded" : String.valueOf(maxOccurs)));

			offsetBldr.append(tabChar);
			traverseComplexType((XSComplexTypeDefinition) elemTypeDef);
			offsetBldr.replace(offsetBldr.lastIndexOf(String.valueOf(tabChar)), offsetBldr.length(), "");
		}
		else if (elemTypeDef instanceof XSSimpleTypeDefinition) {
			System.out.printf("\n%s%s, %s, %s, %d, %s", 
					offsetBldr.toString(), 
					xsElemDecl.getName(), 
					((minOccurs < 1) ? "optional" : "required"), 
					elemTypeDef.getName(),
					minOccurs, ((isUnbounded) ? "unbounded" : String.valueOf(maxOccurs)));

			traverseSimpleType((XSSimpleTypeDefinition) elemTypeDef);
		}
	}
	
	public static void exportSchemaToCSV(String schemaFile, QName schemaElem, String csvFile) throws Exception {
		XSLoader xsLoader = new XSLoaderImpl();
		XSModel xsModel = xsLoader.loadURI(schemaFile);
		XSElementDeclaration xsElemDecl = xsModel.getElementDeclaration(schemaElem.getLocalPart(), schemaElem.getNamespaceURI());
		PrintStream sysout = System.out;
		try {
			PrintStream outFileStream = new PrintStream(csvFile);
			System.setOut(outFileStream);
			traverseElementDecl(xsElemDecl, 1, 1, false);
			outFileStream.close();
		}
		finally {
			System.setOut(sysout);
		}
	}
	
	public static void main(String[] args) {
		String schemaFile = "C:/JDeveloper/soamds/apps/integration/common/Schemas/OTA/2015B/OTA_GolfRateRS.xsd";
		QName schemaElem = new QName("http://www.opentravel.org/OTA/2003/05", "OTA_GolfRatePlanRS");
		String csvFile = "D:/Temp/OTA_GolfRateRS.csv";
		try {
			exportSchemaToCSV(schemaFile, schemaElem, csvFile);
		}
		catch (Exception x) {
			x.printStackTrace();
		}
		System.exit(0);
	}

}
