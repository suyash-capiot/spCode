package com.coxandkings.coherence.utils;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheFactoryBuilder;
import com.tangosol.net.Cluster;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultCacheFactoryBuilder;
import com.tangosol.net.NamedCache;
import java.util.*;

public class CoherenceUtil {
	
	private static Hashtable<String, ConfigurableCacheFactory> cacheFactories = new Hashtable<String, ConfigurableCacheFactory>();
	private static Hashtable<String, NamedCache<Object,Object>> configCaches = new Hashtable<String, NamedCache<Object, Object>>();
	private static Object lockObject = new Object();
	private static Cluster cohCluster =  CacheFactory.ensureCluster();
	private static ClassLoader clsLdr = cohCluster.getClass().getClassLoader();
	private static CacheFactoryBuilder cacheFctryBldr = new DefaultCacheFactoryBuilder();
	
	//private static NamedCache<Object, Object> getCache(String cacheConfig, String cacheName) {
	//	Cluster cohCluster = CacheFactory.ensureCluster();
	//	System.out.println("*** Got handle to cluster <" + cohCluster.getClusterName() + ">");
	//	ClassLoader clsLdr = cohCluster.getClass().getClassLoader();
	//	
	//	CacheFactoryBuilder cacheFctryBldr = new DefaultCacheFactoryBuilder();
	//	ConfigurableCacheFactory configCacheFctry = cacheFctryBldr.getConfigurableCacheFactory(cacheConfig, clsLdr);
	//	
	//	return configCacheFctry.ensureCache(cacheName, clsLdr);
	//}
	
	private static ConfigurableCacheFactory getCacheFactory(String cacheConfig) {
		ConfigurableCacheFactory cacheFactory = cacheFactories.get(cacheConfig);
		if (cacheFactory == null) {
			cacheFactory = cacheFctryBldr.getConfigurableCacheFactory(cacheConfig, clsLdr);
			cacheFactories.put(cacheConfig, cacheFactory);
		}
		
		return cacheFactory;
	}
	
	public static NamedCache<Object, Object> getCache(String cacheConfig, String cacheName) {
		NamedCache<Object, Object> nmdCache = null;
		String configCacheKey = String.format("%s|%s", cacheConfig, cacheName);
		if ( (nmdCache = configCaches.get(configCacheKey)) != null) {
			return nmdCache;
		}
		else {
			synchronized(lockObject) {
				if ( (nmdCache = configCaches.get(configCacheKey)) != null) {
					return nmdCache;
				}
				else {
					ConfigurableCacheFactory cacheFactory = getCacheFactory(cacheConfig);
					nmdCache = cacheFactory.ensureCache(cacheName, clsLdr);
					configCaches.put(configCacheKey, nmdCache);
				}
			}
		}
		
		return nmdCache;
	}

	public static String getFromCache(String cacheConfig, String cacheName, String cacheKey) {
		NamedCache<Object, Object> nmdCache = getCache(cacheConfig, cacheName);
		if (nmdCache != null) {
			return (String) nmdCache.get(cacheKey);
		}
		
		return "";
	}

	public static void putInCache(String cacheConfig, String cacheName, String cacheKey, String cacheVal) {
		NamedCache<Object, Object> nmdCache = getCache(cacheConfig, cacheName);
		if (nmdCache != null) {
			Object response = nmdCache.put(cacheKey, cacheVal);
			System.out.println("***** Received Put Response=<" + response + ">");
		}
	}

}
