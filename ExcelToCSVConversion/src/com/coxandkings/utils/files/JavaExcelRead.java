package com.coxandkings.utils.files;

import jxl.*;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;

public class JavaExcelRead {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JavaExcelRead old = new JavaExcelRead();
		old.convertOLDExcelToCSV("D://ToCSVConvert//Temp//EODS_country_20160323151258(1).xls","D://ToCSVConvert//Temp//csv//");
	}
	public void convertOLDExcelToCSV(String inputfile, String outputFilePath){
		
		try {
			StringBuffer data = new StringBuffer();
			
			//Create a workbook object from the file at specified location. 
			//Change the path of the file as per the location on your computer. 
			Workbook wrk1 =  Workbook.getWorkbook(new File(inputfile));
			
			Sheet [] sheets = wrk1.getSheets();
			int numofsheets = sheets.length;
			System.out.println("number of sheets is " + numofsheets);
			for(int i =0; i<numofsheets; i++)
			{
				Sheet sheet1 = wrk1.getSheet(i);
				String csvFilename = outputFilePath + sheet1.getName() + ".csv";
				File outputFile = new File(csvFilename);
				FileOutputStream fos = new FileOutputStream(outputFile);
				//Obtain reference to the Cell using getCell(int col, int row) method of sheet
				int numofcols = sheet1.getColumns();
				int numofrows = sheet1.getRows();
				System.out.println("number of cols is " + numofcols);
				System.out.println("number of rows is " + numofrows);
				for (int j=0; j<numofrows; j++)
				{
					for(int k=0; k<numofcols; k++)
					{  

						DateCell dCell=null;
						NumberCell nCell=null;
						BooleanCell bCell=null;
						LabelCell lCell=null;

						Cell colArow2 = sheet1.getCell(k,j);
						
						
						if(colArow2.getType()== CellType.DATE)
						{   dCell = (DateCell)colArow2;
							data.append(dCell.getDate()+",");
						}
						else if(colArow2.getType()== CellType.NUMBER)
						{   nCell = (NumberCell)colArow2;
							data.append(nCell.getValue()+",");
						}
						else if(colArow2.getType()== CellType.BOOLEAN)
						{   bCell = (BooleanCell)colArow2;
							data.append(bCell.getValue()+",");
						}
						else if(colArow2.getType()== CellType.LABEL)
						{   lCell = (LabelCell)colArow2;
							data.append(StringEscapeUtils.escapeCsv(lCell.getString()).trim().replaceAll("\\r?\\n", "").replaceAll("\\s", " ")+",");
						}
						else
						{
							data.append(StringEscapeUtils.escapeCsv(colArow2.getContents().toString()).trim().replaceAll("\\r?\\n", "").replaceAll("\\s", " ")+",");
						}
						
						
						//System.out.println("Contents of cell " + k + " and " + j + ": \""+colArow2.getContents() + "\"");
				}
					data.append('\n');
					}
				fos.write(data.toString().getBytes());
				fos.close();
				System.out.println("output filename is : " + csvFilename);
			}
			//Obtain the reference to the first sheet in the workbook
			
			
			//Obtain reference to the Cell using getCell(int col, int row) method of sheet
			/*Cell colArow1 = sheet1.getCell(0, 0);
			Cell colBrow1 = sheet1.getCell(1, 0);
			Cell colArow2 = sheet1.getCell(0, 1);
			
			//Read the contents of the Cell using getContents() method, which will return 
			//it as a String
			String str_colArow1 = colArow1.getContents();
			String str_colBrow1 = colBrow1.getContents();
			String str_colArow2 = colArow2.getContents();
			
			//Display the cell contents
			System.out.println("Contents of cell Col A Row 1: \""+str_colArow1 + "\"");
			System.out.println("Contents of cell Col B Row 1: \""+str_colBrow1 + "\"");
			System.out.println("Contents of cell Col A Row 2: \""+str_colArow2 + "\"");
*/
			
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
