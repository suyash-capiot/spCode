package com.coxandkings.utils.files.unused;

	import java.security.MessageDigest; 
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
	 
	public class SimpleSHA1 { 
		
	    public static String encrypt(String x) throws Exception {
	        MessageDigest d = null;
	        d = java.security.MessageDigest.getInstance("SHA-1");
	        d.reset();
	        d.update(x.getBytes());
	        return d.digest().toString();
	      }
	    
	    public static byte[] encryptbyte(String x) throws Exception {
	        MessageDigest d = null;
	        d = java.security.MessageDigest.getInstance("SHA1");
	        //d.reset();
	        //d.update(x.getBytes("ASCII"));
	        System.out.println("digest is : " + d.digest(x.getBytes("ASCII")));
	        return d.digest(x.getBytes("ASCII"));
	      }
	   
	    
	    public static String enAm() throws Exception{
	    	
	    	String characters  = "Bo97XgLt85Nwi3TF12345620160725MASTERCARDAUD21014430000RAILEZEEGO";
	    	
	    	int max = 10;
	    	int min = 0;
	    	Random randm = new Random();
	    	int ran = randm.nextInt((characters.length()-min))+min;
	    	String uuid = "";    	
	    	
	    	for (int i = 0; i < max; i++) {
	    		ran = randm.nextInt((characters.length()-min))+min;
	    		uuid = uuid + characters.charAt(ran);
	    	}
	    	
	    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ");
	    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	        String ctime = sdf.format(new Date());
	        ctime = ctime.split("\\+")[0]+"Z";
	        System.out.println(ctime);
	        System.out.println(uuid);
	        String pwd = "AMADEUS";
	        
	    	//String f=uuid+ctime+encryptbyte(pwd);
	    	
	    	String encodeNonce = Base64.getEncoder().encodeToString(uuid.getBytes("ASCII"));
	    	System.out.println(encodeNonce);
	    	    	
	    	String hashpwd = Base64.getEncoder().encodeToString(encryptbyte(uuid+ctime+encryptbyte(pwd)));
	    	System.out.println(hashpwd);

	    	return hashpwd + "_" + encodeNonce + "_" + ctime;
	    	
	    }
	    
	    public static void main(String [] args) {
	    	//SimpleSHA1 sa = new SimpleSHA1();
	    	try {
				String s = SimpleSHA1.enAm();
				System.out.println("s is : "+ s);
				System.out.println("Nothing");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    
	    }
	    
	} 

