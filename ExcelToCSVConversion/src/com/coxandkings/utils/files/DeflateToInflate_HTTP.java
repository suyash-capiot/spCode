package com.coxandkings.utils.files;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class DeflateToInflate_HTTP {

                public static String decompress(String url,String method,String payload,String proxy,String port,String header){
                                String response="";
                                try{
                                                System.setProperty("http.proxyHost", proxy);
                                                System.setProperty("http.proxyPort", port);
                                                //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
                                           
                                                java.net.URL wsURL = new URL (null,url,new sun.net.www.protocol.http.Handler());
//                                         
                                               HttpURLConnection conn = (HttpURLConnection) wsURL.openConnection();
                                               System.out.println("connection");
                                                conn.setConnectTimeout(60000);
                                                conn.setDoOutput(true);
                                                conn.setRequestMethod(method);
                                                System.out.println("connection1");
                                                //conn.
                                                if(header!=""){
                                                String[] headerProperty = header.split("&");
                                                for (String h : headerProperty) {
                                                                String[] tempHead = h.trim().split(":");
                                                                conn.setRequestProperty(tempHead[0],tempHead[1]);
                                                                
                                                }}
                                                DataOutputStream wr = new DataOutputStream (conn.getOutputStream ());
                                                System.out.println("connection2");
                                    wr.writeBytes (payload);
                                    wr.flush ();
                                    wr.close ();
                                    if ( conn.getResponseCode() != 200) {
                                                                throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
                                                }    
                                    
                                    
                                    BufferedReader in = new BufferedReader(new InputStreamReader(
                                            conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) 
                     response+=inputLine;
                in.close();
            
                                    
                                    
//                                                BufferedReader br = new BufferedReader(new InputStreamReader(new InflaterInputStream(conn.getInputStream(),new Inflater(true))));
//                                                String temp = "";
//                                                while ((temp = br.readLine()) != null) {
//                                                                response+=temp;
//                                                }
                                                conn.disconnect();
                                }catch (MalformedURLException e) {
                                                e.printStackTrace();
                               }catch (IOException e) {
                                                e.printStackTrace();        
                                }
                              //  System.out.println(response);
                                return response;
                }
                public static void main(String[] args) {
                     decompress("http://www.giftxoxo.com/index.php?route=clientapp/categoryapi/products&key=MLykrXCg9ZNswF&category_id=357&city_id=1319","GET","","172.21.200.37","3128","");
                }
                }
