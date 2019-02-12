package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.ibm.team.repository.common.util.ExtensionRegistryReader;

public class SportExtensionsUtil
{
   /**
    * Return the extensions for the specified extension point.
    * 
    * @param extensionPointId a <code>String</code> identifying an extension
    *        point.
    * 
    * @return an array of {@link IExtension} objects providing the
    *         configuration information for the extensions contributed by
    *         plug-ins.
    */
   public static final IExtension[] getExtensions( String extensionPointId )
   {
      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IExtensionPoint extensionPoint = registry
            .getExtensionPoint( extensionPointId );

      return extensionPoint.getExtensions();
   }

   /**
    * Return all of the configuration elements for the specified extension
    * point.
    * 
    * @param extensionPointId a <code>String</code> identifying an extension
    *        point.
    * 
    * @return an <code>IConfigurationElement[]</code> containing all of the
    *         configuration elements for the specified extension point.
    */
   public static final IConfigurationElement[] getConfigurationElements(
         String extensionPointId )
   {
      List<IConfigurationElement> cfgElements = new ArrayList<IConfigurationElement>();

      for (IExtension extension : getExtensions( extensionPointId ))
      {
         IConfigurationElement[] elements = extension
               .getConfigurationElements();

         for (IConfigurationElement element : elements)
         {
            cfgElements.add( element );
         }
      }

      return cfgElements.toArray( new IConfigurationElement[cfgElements
            .size()] );
   }

   public static void main( String[] args )
   {
      AttributeValueProviderRegistryReader registry = new AttributeValueProviderRegistryReader();
      IConfigurationElement[] configElements = registry
            .getConfigurationElements( "attributeValueProvider" );

      for (IConfigurationElement element : configElements)
      {
         System.out.println( element.toString() );
      }
   }
   private static class AttributeValueProviderRegistryReader
         extends ExtensionRegistryReader<Object>
   {
      public AttributeValueProviderRegistryReader()
      {
         super( "com.ibm.sport.rtc.process.common",
               "com.ibm.sport.rtc.process.operationParticipants.attributeValueProviders" );
      }

      public IConfigurationElement[] getConfigurationElements(
            String elementName )
      {
         List<IConfigurationElement> configElements = this
               .getConfigurationElements();
         List<IConfigurationElement> requestedCfgElements = new ArrayList<IConfigurationElement>();

         for (IConfigurationElement element : configElements)
         {
            String name = element.getName();

            if (name.equals( elementName ))
            {
               requestedCfgElements.add( element );
            }
         }

         return requestedCfgElements.toArray( new IConfigurationElement[0] );
      }
   }
}
