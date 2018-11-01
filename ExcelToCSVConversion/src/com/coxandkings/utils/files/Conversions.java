package com.coxandkings.utils.files;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;


public class Conversions {
	//final static String XMLString = "<policy> <abc>1</abc><abc>2</abc><pqr><content isArray = \"1\">1</content></pqr></policy>";
	final static String XMLString = "<HotelAvailabilityRQ><HotelAvailability><City>Mumbai</City><checkInDate>22/05/2016</checkInDate><checkOutDate>24/05/2016</checkOutDate><adults>3</adults><children>2</children><rooms>2</rooms><room_combinations 	isArray=\"true\">3</room_combinations><room_combinations 	isArray=\"true\">2</room_combinations><child_1_age>5</child_1_age><child_2_age>8</child_2_age></HotelAvailability></HotelAvailabilityRQ>";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = XMLToJSONwithArrayString(XMLString);
		System.out.println("s is : " + s);

	}
	public static String XMLToJSONwithArrayString(String XMLText)
	{
		        String jsonPrettyPrintString = null;
				try {
					String XMLContent = XMLText.replaceAll("content", "cnk_content_java_gen");
					System.out.println(XMLContent);
					JSONObject xmlJSONObj = XML.toJSONObject(XMLContent);
					jsonPrettyPrintString = xmlJSONObj.toString(4);
		            System.out.println(jsonPrettyPrintString);
					xmlJSONObj = RenameTags(xmlJSONObj);
					jsonPrettyPrintString = xmlJSONObj.toString(4);
					jsonPrettyPrintString = jsonPrettyPrintString.replaceAll("cnk_content_java_gen", "content");
		            System.out.println(jsonPrettyPrintString);

				} 
				catch (JSONException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        System.out.println(jsonPrettyPrintString);	    
		        return jsonPrettyPrintString;
	}
	
	public static JSONObject RenameTags(JSONObject obj) throws JSONException
	  {
		 
		 int len = obj.length();
		 int ban = 0;
		 System.out.println("len is " + len);
		 System.out.println("obj for new call is " + obj.toString());
		 JSONArray njarr = new JSONArray();
		 JSONArray nja = new JSONArray();
		 if(obj.getClass().getName().contains("JSONObject"))
		 {
			 String sp = obj.names().toString();
			 sp = sp.substring(1, (sp.length() - 1));
			 System.out.println("sp is " + sp);
			 String[] token = sp.split(",");
			 for (int x=0; x<token.length; x++)
			 {	
				 	 String tkn = token[x].substring(1,token[x].length() -1 );
			        
				 	 String newtkn = tkn;
				 	 System.out.println(" Token is : " + tkn + " class of token is : " + obj.get(newtkn).getClass().getName());
			        if(tkn.contains("isArray"))
			        {
			       	 	System.out.println(" Removing token is : "+ tkn);
			       	    obj.remove(tkn);
			       	    System.out.println(" Obj now is : "+ obj.toString());
			       	    if(obj.toString().contains("{\"content\":"))
			       	    	njarr.put(obj.get("content"));
			       	    else
			       	    njarr.put(obj);
			       	    System.out.println(" njarr now is : "+ njarr.toString() );
			       	  
			        }
			        System.out.println(" Token removed" );
			        if(!newtkn.equals("isArray"))
			        {
			        	
			        	System.out.println(" Went for array check " + newtkn );
			        if(obj.get(newtkn).getClass().getName().contains("JSONArray"))
			        {
			       	 JSONArray ja = obj.getJSONArray(newtkn);
			       	 
			       	 int arrlen = ja.length();
			       	 System.out.println(" Array is " + ja.toString());
			       	 for(int y =0; y< arrlen; y++)
			       	 {   
			       		 //System.out.println("called from array for "+ newtkn + " for the value " + ja.getJSONObject(y).toString() );
			       		 try{JSONObject jap = new JSONObject();
			       			 try{
			       				System.out.println("called from array for "+ newtkn + " for the value " + ja.getJSONObject(y).toString() );
			       		 jap = ja.getJSONObject(y);
			       		jap = RenameTags(jap);
			       			 }
			       			 catch(Exception e)
			       			 {   System.out.println("called from array for "+ newtkn + " for the value " + "{"+ja.get(y)+"}" );
			       				 //jap = new JSONObject("{"+ja.get(y)+"}");
			       				 ban =1;
			       			 }
			       		 
			       		System.out.println(" ***************Now jap is : " + jap.toString());
			       		if(jap.toString().contains("CNK_ARR_REPLACER"))
			       		{   System.out.println("Jap ka array value is: " + jap.get("CNK_ARR_REPLACER").toString().substring(1, jap.get("CNK_ARR_REPLACER").toString().length()-1));
			       		    String cnk = jap.get("CNK_ARR_REPLACER").toString().substring(1, jap.get("CNK_ARR_REPLACER").toString().length()-1);
			       		    if(cnk.startsWith("{"))
			       		    {
			       		   JSONObject temp = new JSONObject(jap.get("CNK_ARR_REPLACER").toString().substring(1, jap.get("CNK_ARR_REPLACER").toString().length()-1));
			          		nja.put(temp);
			       		    }
			       		    else
			       		    {   if(NumberUtils.isNumber(cnk))
			       		    	nja.put(Integer.parseInt(cnk));
								else
								nja.put(cnk);
			       		    }
			          		System.out.println("nja ka array value is: " + nja.toString());
			       		}
			       		else
			       		{ if(ban == 1)
			       		{  String saml= ja.get(y).toString();
			       		if(NumberUtils.isNumber(saml))
			       			nja.put(Integer.parseInt(saml));
			       		else
			       			nja.put(saml);
			       		}
			       		else
			       			nja.put(ja.getJSONObject(y));
			       		}
			          		
			       	 }
			       		 catch(Exception ex){}
			       		 }
			       	 if(nja.toString().length()>2)
			       	 {
			       	System.out.println(" Now nja is : " + nja.toString());
			       	 obj.remove(newtkn);
			       	System.out.println(" obj after remove token " + newtkn + " is : " + obj.toString());
			       	 obj.put(newtkn, nja);
			       	System.out.println(" Now within array the obj is : " + obj.toString());
			       	 }
			        }
			        }
			        if(!newtkn.equals("isArray"))
			        {
			        	System.out.println(" Went for object check" );
			        if(obj.get(newtkn).getClass().getName().contains("JSONObject"))
			        	if(!obj.get(newtkn).getClass().getName().contains("JSONObject$Null"))
			        {       System.out.println("called for "+ newtkn);
			       	     JSONObject jap = obj.getJSONObject(newtkn);
			       		 jap = RenameTags(jap);
			       		System.out.println("jap ka string is before CNK_ARR_REPLACER condition : " + jap.toString());
			       		 if(jap.toString().contains("CNK_ARR_REPLACER"))
			       		 {
			       			 String njs = jap.toString().substring(21, jap.toString().length() -2);
			       			System.out.println("njs is " + njs);
			       			if(njs.startsWith("{")) 
			       			{
			       			JSONObject jsarr = new JSONObject(njs);
			       			 obj.remove(newtkn);
			       			 obj.append(newtkn, jsarr);
			       			}
			       			else
			       			{   if(NumberUtils.isNumber(njs))
			       			{
			       				obj.remove(newtkn);
				       			 obj.append(newtkn, Integer.parseInt(njs));
			       			}
			       			else{
			       				obj.remove(newtkn);
				       			 obj.append(newtkn, njs);
			       			}
			       			}
			       				
			       			System.out.println("Now obj after njs is " + obj.toString());
			       		 }
			       		 else
			       		 {
			       		 System.out.println("jap ka string is " + jap.toString());
			       	     obj.put(newtkn, jap);
			       		 }
			       		 System.out.println(" After append " + obj.toString());
			       	 }
			        }
			        
			 	}
			   
		 //}
			 }
			
			 System.out.println(" Now data with two elements are " + obj.toString());
			 if(njarr.toString().length()>2)
			 {   System.out.println("njarr is " +njarr.toString());
				 JSONObject nobj = new JSONObject("{ \"CNK_ARR_REPLACER\" : "+njarr.toString()+"}");
				 System.out.println("njarr is " +nobj.toString());
				 return nobj;
			 }
			 else
		return obj;
		 
	  }


}
