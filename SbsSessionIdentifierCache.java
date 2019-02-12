package com.ibm.sport.rtc.common;

import java.util.HashMap;
import java.util.Map;

import com.ibm.ArtifactTechnology.common.Property;
import com.ibm.ArtifactTechnology.common.security.SessionIdentifier;
import com.ibm.ArtifactTechnology.common.security.SessionIdentifierPropertyElement;

public class SbsSessionIdentifierCache
{
   private static final SbsSessionIdentifierCache INSTANCE = new SbsSessionIdentifierCache();
   private Map<String, SessionIdentifier> sessionIdentifiers = new HashMap<String, SessionIdentifier>();

   private SbsSessionIdentifierCache()
   {
      
   }
   
   public synchronized static final SbsSessionIdentifierCache getInstance()
   {
      return INSTANCE;
   }

   public synchronized final SessionIdentifier getSessionIdentifier(
         String providerUserName )
   {
      return this.sessionIdentifiers.get( providerUserName );
   }

   public synchronized final SessionIdentifier cacheSessionIdentifier(
         String providerUserName, SessionIdentifier sessionId )
   {
      return this.sessionIdentifiers.put( providerUserName, sessionId );
   }
   
   public synchronized final SessionIdentifier removeSessionIdentifier(String providerUserName)
   {
      return this.sessionIdentifiers.remove( providerUserName );
   }
   
   public static final Property createProperty(SessionIdentifier sessionId)
   {
      return SessionIdentifierPropertyElement.create( sessionId );
   }
}
