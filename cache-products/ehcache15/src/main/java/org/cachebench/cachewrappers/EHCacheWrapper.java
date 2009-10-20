package org.cachebench.cachewrappers;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cachebench.CacheWrapper;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;


/**
 * An implementation of SerializableCacheWrapper that uses EHCache as an underlying implementation.
 * <p/>
 * Pass in a -Dbind.address=IP_ADDRESS
 * ehcache propery files allows referencing system properties through syntax ${bind.address}.
 *
 * @author Manik Surtani (manik@surtani.org)
 * @version $Id: EHCacheWrapper.java,v 1.6 2007/05/21 16:17:56 msurtani Exp $
 */
public class EHCacheWrapper implements CacheWrapper
{
   private CacheManager manager;
   private Ehcache cache;
   private Log log = LogFactory.getLog("org.cachebench.cachewrappers.EHCacheWrapper");
   boolean localmode;

   /* (non-Javadoc)
   * @see org.cachebench.CacheWrapper#init(java.util.Properties)
   */
   public void init(Map parameters) throws Exception
   {
      if (log.isTraceEnabled()) log.trace("Entering EHCacheWrapper.init()");
      localmode = (Boolean.parseBoolean((String) parameters.get("localOnly")));
      log.debug("Initializing the cache with props " + parameters);
      URL url = getClass().getClassLoader().getResource((String) parameters.get("config"));
      log.debug("Config URL = " + url);
      Configuration c = ConfigurationFactory.parseConfiguration(url);
      c.setSource("URL of " + url);

      manager = new CacheManager(c);
      setUp();
      log.debug("Finish Initializing the cache");
   }

   /* (non-Javadoc)
   * @see org.cachebench.CacheWrapper#setUp()
   */
   public void setUp() throws Exception
   {
      log.info("Caches avbl:");
      for (String s : manager.getCacheNames()) log.info("    * " + s);
      cache = manager.getCache("cache");
      log.info("Using named cache " + cache);
      if (!localmode)
      {
         log.info("Bounded peers: " + manager.getCachePeerListener().getBoundCachePeers());
         log.info("Remote peers: " + manager.getCacheManagerPeerProvider().listRemoteCachePeers(cache));
      }
   }

   /* (non-Javadoc)
   * @see org.cachebench.CacheWrapper#tearDown()
   */
   public void tearDown() throws Exception
   {
      manager.shutdown();
   }

   /* (non-Javadoc)
   * @see org.cachebench.SerializableCacheWrapper#putSerializable(java.io.Serializable, java.io.Serializable)
   */
   public void putSerializable(Serializable key, Serializable value) throws Exception
   {
      Element element = new Element(key, value);
      cache.put(element);
   }

   /* (non-Javadoc)
   * @see org.cachebench.SerializableCacheWrapper#getSerializable(java.io.Serializable)
   */
   public Object getSerializable(Serializable key) throws Exception
   {
      return cache.get(key);
   }

   public void empty() throws Exception
   {
      cache.removeAll();
   }

   /* (non-Javadoc)
   * @see org.cachebench.CacheWrapper#put(java.lang.Object, java.lang.Object)
   */
   public void put(List<String> path, Object key, Object value) throws Exception
   {
      putSerializable((Serializable) key, (Serializable) value);
   }

   /* (non-Javadoc)
   * @see org.cachebench.CacheWrapper#get(java.lang.Object)
   */
   public Object get(List<String> path, Object key) throws Exception
   {
      Object s = getSerializable((Serializable) key);
      if (s instanceof Element)
      {
         return ((Element) s).getValue();
      }
      else return s;
   }

   public int getNumMembers()
   {

      return localmode ? 0 : manager.getCacheManagerPeerProvider().listRemoteCachePeers(cache).size();
   }

   public String getInfo()
   {
      return cache.getKeys().toString() + (localmode ? "" : (" remote peers: " + manager.getCachePeerListener().getBoundCachePeers()));
   }

   public Object getReplicatedData(List<String> path, String key) throws Exception
   {
      Object o = get(path, key);
      if (log.isTraceEnabled())
      {
         log.trace("Result for the key: '" + key + "' is value '" + o + "'");
      }
      return o;
   }

   public Object startTransaction()
   {
      throw new UnsupportedOperationException("Does not support JTA!");
   }

   public void endTransaction(boolean successful)
   {
      throw new UnsupportedOperationException("Does not support JTA!");
   }
}
