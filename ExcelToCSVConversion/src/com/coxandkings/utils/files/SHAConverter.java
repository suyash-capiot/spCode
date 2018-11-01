package com.coxandkings.utils.files;

import java.security.MessageDigest;


public class SHAConverter {

	public static String encryptSHAString(String base, String hash_algo) throws Exception {
	    try{
	    	
	    	MessageDigest digest = MessageDigest.getInstance(hash_algo);
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	        
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
      }
	
	/*public static void main(String args[]){
		String test="499|Sahil|dha@gmail.com|9000000000|order1|2016-09-10|10:00|+rNEQjHOqj+Z_clE2l~MBCSyh1htS9LyT*9_JmgX4GjOkWobMJqH3%TdM7Uu|MLykrXCg9ZNswF";
		String test_hash;
		try {
			test_hash = SHAConverter.encryptSHAString(test,"SHA-256");
			System.out.println(test_hash);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}*/
}


