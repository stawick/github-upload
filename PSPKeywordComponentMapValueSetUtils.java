package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.team.process.common.IProcessConfigurationData;
import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IConfiguration;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IFilteredValueSetProvider;
// import com.ibm.team.workitem.common.internal.attributeValueProviders.IValueSetProvider;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/* @formatter:off */
/** 
 * _F155269A SPA FIXCAT, Preventative Service Program (PSP) value set
 * provider java extension to RTC. (Extension defined in plugin.xml
 * for this package.)
 * (Old, widely recognized nomenclature = PSP related, and is used
 * throughout here since that is what the SPA function was using
 * up to this point. FIXCAT is new terminology, but still the same
 * function.)
 * 
 * This function is used in an APAR work item on a Close action to
 * generate PSP keyword choices based on the APAR component defined.
 * The choices are supplied by a master (text) file, and read in and
 * parsed by a SPoRT administrator project area customization feature
 * which defines a PSP keyword / Component association. The file is
 * parsed into a project area customization section of the form:<br>
 * 
 * The SBS process configuration structure -<br>
 * {@code
 *  <configuration-data id="com.ibm.sport.rtc.configuration.sbs">
 *        ...
 *        <sportPSPKeywordComponentMapping> [0,1]
 *             <sportPSPKeywordComponentData componentName="..."> [0,*]
 *                  <sportPSPKeyword name="..." description="...", etc .../> [0,*]
 *                  (more attributes may get added later, after description)
 *             </sportPSPKeywordComponentData>
 *        </sportPSPKeywordComponentMapping>
 *  </configuration-data>
 *  }
 * 
 * @author stawick
 *
 */
/* @formatter:on */
public class PSPKeywordComponentMapValueSetUtils
      implements IFilteredValueSetProvider<Object>
      // implements IValueSetProvider<Object>
{
   // SBS mapping, which appends the PSP Keyword selection(s) to the
   // proper fields, keys off of the "***" as an invalid PSP Keyword,
   // and ignores these error messages. To avoid killing the action,
   // these are displayed in the choice value.
   protected static String noComponentErrMsg1 = "*** Values to select are derived from the component.";
   protected static String noComponentErrMsg2 = "*** Please specify a component and try again.";
   protected static String noPSPKeywordsDefinedMsg = "*** The PSP Keyword choices were not imported into the project area.";
   protected static String noPSPKeywordsForComponentDefinedMsg = "*** No PSP Keyword choices were defined for the APAR Component.";
   protected static String contactSPoRTAdminMsg = "*** Contact a SPoRT Administrator.";

   protected static String sportSBSProjectAreaConfigID = "com.ibm.sport.rtc.configuration.sbs";
   protected static String pspKeywordComponentMapXMLElement = "sportPSPKeywordComponentMapping";
   protected static String pspKeywordComponentEntryXMLElement = "sportPSPKeywordComponentData";
   protected static String pspKeywordComponentEntryXMLAttribute = "componentName";
   protected static String pspKeywordEntryXMLElement = "sportPSPKeyword";
   protected static String pspKeywordNameXMLAttribute = "name";
   protected static String pspKeywordDescriptionXMLAttribute = "description";

   /** 
    * These next 2 methods are derived from the implemented class.
    * We must provide these.
    * TODO Not sure whether we intend to filter the results? (TBD)
    * 
    * @param attribute
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param filter
    * @param monitor
    * @return
    * @throws TeamRepositoryException
    */
   @SuppressWarnings("unchecked")
   public List getFilteredValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         String filter, IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      // Get initial values without filter
      List<String> returnValues = getValueSet( attribute, workItem, workItemCommon,
            configuration, monitor );
      
      if (filter == null || filter.trim().length() == 0
            || returnValues == null || returnValues.isEmpty())
      {
         // No filter regex or no choices, use all choices
         return returnValues;
      }
      // TODO Filter values to be returned tbd? ...
      // return null;
      List<String> filteredValues = null;
      // Insure filter in usable regex format
      String processedFilter = CommonUtils.processFilterToRegex( filter );
      try
      {
         for (String returnChoice : (List<String>)returnValues)
         {
            if (returnChoice.matches( processedFilter ))
            {
               if (filteredValues == null)
               {
                  filteredValues = new ArrayList<String>();
               }
               filteredValues.add( returnChoice );
            }
         }
      }
      catch (Exception e)
      {
         // Catch a potential regex exception and just indicate
         // regex error as choice. Have user try again.
         if (filteredValues == null)
         {
            filteredValues = new ArrayList<String>();
         }
         filteredValues.add( "Exception encountered with filter: "
               + e.getMessage() );
      }

      // Prevent null pointer exception in value-set picker presentation
      // when list returned from cache is empty.
      // Return empty list in this case.
      return (filteredValues == null ? new ArrayList<String>()
            : filteredValues);
   }

   @SuppressWarnings("unchecked")
   public List getValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      List<String> returnValues = null;

      SportCommonData sportCommonData = CommonUtils.getLimitedUseSPoRTDataObject( workItem, workItemCommon,
            this.getClass().getPackage().getName(), monitor );

      String populateChoiceAttrId = attribute.getIdentifier();
      String aparComponentValue = getComponentValue( populateChoiceAttrId,
            workItem, sportCommonData );

      // When requesting component list, might not have value yet - OK
      if ((aparComponentValue == null || aparComponentValue.trim().length() == 0)
            && componentNeeded( populateChoiceAttrId ))
      {
         // No component to use (ex. not specified yet on Create)
         returnValues = new ArrayList<String>();
         returnValues.add( noComponentErrMsg1 );
         returnValues.add( noComponentErrMsg2 );
         return returnValues;
      }

      // Choices from Project area customization imported from file.
      if (pspSelectorAttribute( populateChoiceAttrId ))
      {
         returnValues = getPSPSelectorValueSet( attribute, workItem,
               workItemCommon, configuration, aparComponentValue,
               sportCommonData, monitor );
      }

      // Choices from collector attribute, to be unset.
      if (pspUnSelectorAttribute( populateChoiceAttrId ))
      {
         returnValues = getPSPUnSelectorValueSet( attribute, workItem,
               workItemCommon, configuration, aparComponentValue,
               sportCommonData, monitor );
      }
      // Should not be any other uses.

      // Now remove any choices already selected in a "collector" 
      // attribute to prevent duplicate selections.
      returnValues = preventDuplicateChoices( returnValues,
            populateChoiceAttrId, workItem, sportCommonData );
      // Note: RTC requirement open to use a string list type attribute
      // plus multi-select presentation.

      // Prevent null pointer exception in value-set picker presentation
      // when list returned from cache is empty.
      // Return empty list in this case.
      // return returnValues;
      return (returnValues == null ? new ArrayList<String>() : returnValues);
   }

   /* @formatter:off */
   /**
    * Obtain choices from project area SPoRT customization area.<br>
    * The choice selected will be set in a collector attribute
    * using a calculated value provider. Each choice will be of the
    * form: "[PSP keyword] - [Description]"<br>
    * The SBS process configuration structure -<br>
    * {@code
    *  <configuration-data id="com.ibm.sport.rtc.configuration.sbs">
    *        ...
    *        <sportPSPKeywordComponentMapping> [0,1]
    *             <sportPSPKeywordComponentData componentName="..."> [0,*]
    *                  <sportPSPKeyword name="..." description="...", etc .../> [0,*]
    *                  (more attributes may get added later, after description)
    *             </sportPSPKeywordComponentData>
    *        </sportPSPKeywordComponentMapping>
    *  </configuration-data>
    *  }
    * 
    * @param attribute
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param monitor
    * @return
    * @throws TeamRepositoryException
    */
   /* @formatter:on */
   public List<String> getPSPSelectorValueSet( IAttribute attribute,
         IWorkItem workItem, IWorkItemCommon workItemCommon,
         IConfiguration configuration, String aparComponentValue,
         SportCommonData sportCommonData, IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      List<String> returnValues = null;

      // Finds project area <configuration-data ... definition
      // for the ID given. This will be the parent xml element
      // to obtain the PSP Keyword configuration specifics from.
      IProcessConfigurationData processConfigData = RtcUtils
            .getProcessConfigurationData(
                  // "com.ibm.sport.rtc.configuration.sbs",
                  sportSBSProjectAreaConfigID,
                  sportCommonData );
      if (processConfigData == null)
      {
         // No PSP choices imported
         returnValues = new ArrayList<String>();
         returnValues.add( noPSPKeywordsDefinedMsg );
         returnValues.add( contactSPoRTAdminMsg );
         return returnValues;
      }

      // Processes xml sub elements of SPoRT project area
      // configuration data.
      
      // Check for PSP Keyword structure element(s)
      IProcessConfigurationElement pspMappingParentElement = RtcUtils
            .findProcessConfigurationElement( processConfigData,
                  pspKeywordComponentMapXMLElement, null, sportCommonData );
      if (pspMappingParentElement == null)
      {
         // No PSP choices imported
         returnValues = new ArrayList<String>();
         returnValues.add( noPSPKeywordsDefinedMsg );
         returnValues.add( contactSPoRTAdminMsg );
         return returnValues;
      }
      
      // Find Component entry
      HashMap<String, String> pspConfigurationAttributes = new HashMap<String, String>();
      pspConfigurationAttributes.put( pspKeywordComponentEntryXMLAttribute,
            aparComponentValue.trim() );
      IProcessConfigurationElement pspComponentParentElement = RtcUtils
            .findProcessConfigurationElement( pspMappingParentElement,
                  pspKeywordComponentEntryXMLElement,
                  pspConfigurationAttributes, sportCommonData );
      if (pspComponentParentElement == null)
      {
         // No PSP choices define for component
         returnValues = new ArrayList<String>();
         returnValues.add( noPSPKeywordsForComponentDefinedMsg );
         returnValues.add( contactSPoRTAdminMsg );
         return returnValues;
      }

      // Prepare to return PSP keywords defined to Component
      returnValues = new ArrayList<String>();
      // Find Component Keywords
      IProcessConfigurationElement[] pspKeywords = pspComponentParentElement
            .getChildren();
      if (pspKeywords != null)
      {
         // Collect choices
         for (IProcessConfigurationElement pspKeywordElement : pspKeywords)
         {
            //
            if (pspKeywordElement.getName()
                  .equals( pspKeywordEntryXMLElement ))
            {
               // Add choice
               // These should not really be null -
               String pspKWname = pspKeywordElement
                     .getAttribute( pspKeywordNameXMLAttribute );
               String pspKWDescr = pspKeywordElement
                     .getAttribute( pspKeywordDescriptionXMLAttribute );
               String pspKWChoice = (pspKWname == null || pspKWname.trim()
                     .length() == 0) ? "" : pspKWname.trim();
               if (pspKWChoice.length() > 0)
               {
                  // Have a keyword, add description of the form -
                  // "KW - Descr"
                  pspKWChoice += (pspKWDescr == null || pspKWDescr.trim()
                        .length() == 0) ? "" : " - " + pspKWDescr.trim();
                  returnValues.add( pspKWChoice );
               }
            }
         }
      }
      if (returnValues.isEmpty())
      {
         // Component was defined with no associated PSP keywords
         returnValues.add( noPSPKeywordsForComponentDefinedMsg );
         returnValues.add( contactSPoRTAdminMsg );
      }

      return returnValues;
   }
   
   /**
    * Obtain choices from a collector attribute.
    * The choice selected will be unset in the collector attribute
    * using a calculated value provider.
    * 
    * @param attribute
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param monitor
    * @return
    * @throws TeamRepositoryException
    */
   public List<String> getPSPUnSelectorValueSet( IAttribute attribute,
         IWorkItem workItem, IWorkItemCommon workItemCommon,
         IConfiguration configuration, String aparComponentValue,
         SportCommonData sportCommonData, IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      List<String> returnValues = null;
      // For the web client, need to obtain non-visible internal
      // editable attribute to get selection changes.
      String collectedChoices = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID, sportCommonData );
      /*
      String collectedChoices = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID, sportCommonData );
      */
      if (collectedChoices != null && collectedChoices.length() > 0)
      {
         // Split at newline - use regex that may include optional
         // \r carriage return.
         String[] unSelectChoices = collectedChoices
               .split( Constants.LINE_SPLIT_PATTERN );
               // collectedChoices.split( "\n" );
         if (unSelectChoices != null && unSelectChoices.length > 0)
         {
            returnValues = new ArrayList<String>();
            for (String unSelectChoice : unSelectChoices)
            {
               if (unSelectChoice != null
                     && unSelectChoice.trim().length() > 0)
               {
                  returnValues.add( unSelectChoice.trim() );
               }
            }
         }
      }
      return returnValues;
   }

   /** 
    * Method to obtain the component used based on attribute processing.
    * Returns the value upper cased in case user input is in lower case.
    * 
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String getComponentValue(
         String populateChoiceAttrId, 
         IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String componentValue = "";
      // Get base component attr from workItem argument
      componentValue = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.COMPONENT_ATTRIBUTE_ID, sportCommonData );
      // In case value is from user-input, ensure upper case used
      if (componentValue != null && componentValue.length() > 0)
      {
         componentValue = componentValue.toUpperCase();
      }

      return componentValue;
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean componentNeeded( String populateChoiceAttrId )
   {
      // Requesting choices for component-related attribute(s) -
      return (pspSelectorAttribute( populateChoiceAttrId ));
   }

   /** 
    * Method to remove choices already in some other "collector"
    * attribute.
    * Use attribute to determine data to check for duplicates.
    * Will only be applied to Selector, un-selector will always
    * contain all of the collector values.
    * Choices are already in a List of Strings, collector attribute
    * values are assumed to be newline separated.
    * 
    * @param initialPSPKeywordChoices
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static List<String> preventDuplicateChoices(
         List<String> initialPSPKeywordChoices,
         String populateChoiceAttrId, IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Strip out choices already in some "collector" attribute
      // Initially all are returned
      List<String> returnValues = initialPSPKeywordChoices;
      // String valueSeparator = "\n"; // Newline separated values within collector
      
      String collectorValues = null;
      // Attribute indicates it is multi-value capable
      // TODO use REAL selector attribute when implemented
      // Should only be applied to Selector, un-selector will always
      // contain all of the collector values.
      if (pspSelectorAttribute( populateChoiceAttrId ))
      {
         // Get collector attribute value
         // For the web client, need to obtain non-visible internal
         // editable attribute to get selection changes.
         collectorValues = RtcUtils.getAttributeValueAsString( workItem,
               AparConstants.DRAFT_PSP_KEYWORDS_EDITABLE_ATTRIBUTE_ID, sportCommonData );
         /*
         collectorValues = RtcUtils.getAttributeValueAsString( workItem,
               AparConstants.DRAFT_PSP_KEYWORDS_ATTRIBUTE_ID, sportCommonData );
         */
      }
      // If we have values already chosen
      if (collectorValues != null && collectorValues.trim().length() > 0)
      {
         // Split collected values at newline - use regex that may
         // include optional \r carriage return.
         String[] collectedValuesArray = collectorValues
               .split( Constants.LINE_SPLIT_PATTERN );
               // .split( valueSeparator );
         if (collectedValuesArray != null && collectedValuesArray.length > 0)
         {
            // New set to be determined
            // Since we are altering values, we dont want to modify the
            // obtained values - so need new list
            returnValues = new ArrayList<String>();
            returnValues.addAll( initialPSPKeywordChoices );
            // returnValues.remove( null );
            // Remove values already chosen
            for (String returnValue : collectedValuesArray)
            {
               if (returnValue != null && returnValue.trim().length() > 0)
               {
                  // Do not need to test if it exists, remove method
                  // handles that case.
                  returnValues.remove( returnValue.trim() );
                  /*
                  if (initialPSPKeywordChoices
                        .contains( returnValue.trim() ))
                  {
                     returnValues.remove( returnValue.trim() );
                  }
                  */
               }
            }
         }
      }
      
      return returnValues;
   }

   /**
    * Return status based on selector attribute being processed 
    * @return
    */
   public static boolean pspSelectorAttribute( String populateChoiceAttrId )
   {
      // Requesting choices to be selected - 
      return (populateChoiceAttrId
            .equals( AparConstants.DRAFT_PSP_KEYWORD_SELECTOR_ATTRIBUTE_ID ));
   }

   /**
    * Return status based on (un)selector attribute being processed 
    * @return
    */
   public static boolean pspUnSelectorAttribute( String populateChoiceAttrId )
   {
      // Requesting choices to be unselected - 
      return (populateChoiceAttrId
            .equals( AparConstants.DRAFT_PSP_KEYWORD_UNSELECTOR_ATTRIBUTE_ID ));
   }

}