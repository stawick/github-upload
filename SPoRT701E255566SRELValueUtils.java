package com.ibm.sport.rtc.common;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IConfiguration;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IValueProvider;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * Java extension of RTC Calculated Value Provider, invoked on a work
 * item create. Should adhere to "dependsOn" trigger specifications on
 * the utilizing attribute. All logic added within.
 * The objective is to use the Component Browse cached RETAIN data.
 * Logic :
 * WHEN invoked from RTC on a create (with dependsOn trigger details,
 * typically when the release value changes)
 *    IF symptom keyword attribute is empty (RTC work around, required
 *    on a create)
 *       Obtain SREL from Component cache (may induce Component browse
 *       to cache component data on 1st reference)
 *          IF SREL defined in Component data cache
 *             Return SREL value
 *    ELSE return current attribute value as-is.
 * Return release attribute value (as typed in so far, or selected).
 * 
 * @author stawick
 *
 */
public class SPoRT701E255566SRELValueUtils
      implements IValueProvider<Object>
{

   /**
    * Java extension to default system release in an APAR work item.
    * If component specified and SREL defined in cached RETAIN
    * component data, use that value as the default.
    * Else return the release as typed in so far, or selected.
    * Current javascript implementation checks the symptom keyword
    * value to be empty, so user can override any default. (RTC
    * workaround.) This works because that attribute is REQUIRED on a
    * create.
    * 
    * @param attribute = The attribute utilizing the value provider,
    *        in case that is necessary. (Should be system release.)
    * @param workItem = The applicable work item, can be used to derive
    *        other attributes for logic.
    * @param workItemCommon
    * @param configuration = The project area xml configuration of this
    *        provider (n/a with a java extension).
    * @param monitor
    * 
    * @return Calculated value (or "").
    */
   @Override
   public Object getValue( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      String returnValue = "";

      SportCommonData sportCommonData = CommonUtils
            .getLimitedUseSPoRTDataObject( workItem, workItemCommon, this
                  .getClass().getPackage().getName(), monitor );

      String populateChoiceAttrId = attribute.getIdentifier();
      String currentAttrValue = RtcUtils.getAttributeValueAsString( workItem,
            populateChoiceAttrId, sportCommonData );
      String aparWorkaroundValue = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.SYMPTOM_KEYWORD_ATTRIBUTE_ID, sportCommonData );
      /*
      String aparWorkaroundValue = getWorkaroundValue( populateChoiceAttrId,
            workItem, sportCommonData );
      */
      // If the workaround has already been specified, do not do any
      // default processing, return as-is. Allow user override.
      if (aparWorkaroundValue != null && aparWorkaroundValue.length() > 0)
      {
         if (currentAttrValue != null && currentAttrValue.length() > 0)
            returnValue = currentAttrValue;
         return returnValue;
      }

      String aparComponentValue = getComponentValue( populateChoiceAttrId,
            workItem, sportCommonData );
      String aparReleaseValue = getReleaseValue( populateChoiceAttrId,
            workItem, sportCommonData );
      if (aparReleaseValue != null && aparReleaseValue.length() > 0)
         returnValue = aparReleaseValue;

      // Component not specified, use the release.
      if (aparComponentValue == null || aparComponentValue.length() == 0)
         return returnValue;
      
      // Determine SREL from cached RETAIN data.
      String retainSRELvalue = "";
      // Create a utility instance to obtain cached RETAIN data.
      // Utilize its method to gather values based on the system
      // release attribute applied to this calculated value provider
      // in RTC.
      // Note: This may induce a RETAIN request via SBS to gather the
      // component data on 1st reference and cache it. In *most* cases
      // A component will be supplied, and a release may be selected,
      // which would induce this 1st reference and cache the data.
      // However, if a release was manually entered, the component data
      // might not yet be cached, so account for that here by using the
      // getValueSet "front door" which handles this.
      // Assume only 1 SREL "S" type release will be defined, or just
      // use 1st definition.
      ComponentBrowseValueSetUtils retainComponentCacheData = new ComponentBrowseValueSetUtils();
      @SuppressWarnings("unchecked")
      List<String> componentSRELvalues = retainComponentCacheData
            .getValueSet( attribute, workItem, workItemCommon, configuration,
                  monitor );
      // If no SREL defined, return the release value.
      if (componentSRELvalues == null || componentSRELvalues.isEmpty())
         return returnValue;
      // Otherwise get the 1st SREL value.
      retainSRELvalue = componentSRELvalues.get( 0 );
      // There may be errors msgs in the returned results? Length <= 3
      // means it is a valid release, not error msg. On an error, treat
      // like no SREL defined.
      if (retainSRELvalue != null && retainSRELvalue.length() != 0
            && retainSRELvalue.trim().length() <= 3)
         returnValue = retainSRELvalue;

      // Return the release or component SREL
      return returnValue;
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
    * Method to obtain the release attribute based on attribute
    * processing. Returns the value upper cased in case user input is
    * in lower case.
    * 
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static String getReleaseValue(
         String populateChoiceAttrId, 
         IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String releaseValue = "";
      // Get base component attr from workItem argument
      releaseValue = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.RELEASE_ATTRIBUTE_ID, sportCommonData );
      // In case value is from user-input, ensure upper case used
      if (releaseValue != null && releaseValue.length() > 0)
      {
         releaseValue = releaseValue.toUpperCase();
      }

      return releaseValue;
   }

   /** 
    * Method to obtain the workaround dependent attribute based on
    * attribute processing. Returns the value upper cased in case user
    * input is in lower case.
    * 
    * @param populateChoiceAttrId
    * @param workItem
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   /*
   public static String getWorkaroundValue(
         String populateChoiceAttrId, 
         IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String workaroundValue = "";
      // Get base component attr from workItem argument
      workaroundValue = RtcUtils.getAttributeValueAsString( workItem,
            AparConstants.SYMPTOM_KEYWORD_ATTRIBUTE_ID, sportCommonData );
      // In case value is from user-input, ensure upper case used
      if (workaroundValue != null && workaroundValue.length() > 0)
      {
         workaroundValue = workaroundValue.toUpperCase();
      }

      return workaroundValue;
   }
   */

}
