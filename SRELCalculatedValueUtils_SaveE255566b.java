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
 * <br>Actually, according to a jazz.net forum post, java extensions of
 * a calculated value provider are only triggered on a Save. In this
 * case, on a valid save ALL required attributes must be specified.
 * So must now test the current attribute value, if not empty use
 * as-is, else attempt to determine default.
 * 
 * The objective is to use the Component Browse cached RETAIN data.
 * <p>Logic :<br>
 * WHEN invoked from RTC on a create (when triggered on a Save)
 *    IF current attribute is empty (not manually entered)
 *       Obtain SREL from Component cache (may induce Component browse
 *       to cache component data on 1st reference).
 *          IF SREL defined in Component data cache
 *             Return SREL value.
 *    ELSE current attribute is not empty (already specified)
 *       Return current attribute value as-is.
 *    When no SREL is defined for a component
 *       Return release attribute value (as typed in so far, or selected).
 * 
 * <p>Note: This appears to get invoked twice on a create/save. The 2nd
 * work item appears to be empty and have a -1 ID. Unknown why, but it
 * doesnt seem to affect setting a default value (it doesnt get
 * cleared). This appears to be a precondition function and any
 * REQUIRED rules are effective. Also, after a successful save the
 * default value appears in the UI, but not sure if that is due to this
 * provider or our follow up browse/refresh function?
 * 
 * @author stawick
 *
 */
public class SRELCalculatedValueUtils_SaveE255566b
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
    * create. However, this only gets invoked on a Save, so we can test
    * if the current value has been specified or not instead.
    * 
    * @param attribute = The attribute utilizing the value provider,
    *        in case that is necessary. (Should be system release.
    *        For sysroute it will be to SCP Release.)
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
            .getLimitedUseSPoRTDataObject( workItem, workItemCommon,
                  this.getClass().getPackage().getName(), monitor );

      String populateChoiceAttrId = attribute.getIdentifier();
      String currentAttrValue = RtcUtils.getAttributeValueAsString( workItem,
            populateChoiceAttrId, sportCommonData );
      // With the java extension, this provider is only invoked on a
      // Save (twice in fact?). On a valid save, ALL required
      // attributes must be specified, so now must test if the system
      // release attribute has a current value. If not, determine
      // default.
      if (currentAttrValue != null && currentAttrValue.length() > 0)
      {
         return currentAttrValue;
      }

      // Reuse utility function as it is also applicable here.
      // Static function, so class instance not needed here.
      String aparComponentValue = ComponentBrowseValueSetUtils_SaveE255566b
            .getComponentValue( populateChoiceAttrId, workItem,
                  sportCommonData );
      // String aparReleaseValue = getReleaseValue( populateChoiceAttrId,
      // _RTS?
      String aparReleaseValue = ComponentBrowseValueSetUtils_SaveE255566b.getReleaseValue(
            populateChoiceAttrId, workItem, sportCommonData );
      // Prime with reported release.
      if (aparReleaseValue != null && aparReleaseValue.length() > 0)
         returnValue = aparReleaseValue;

      // Component not specified, use the release.
      if (aparComponentValue == null || aparComponentValue.length() == 0)
         return returnValue;

      // Determine SREL from cached RETAIN data.
      String retainSRELvalue = getSRELValue( attribute, workItem,
            workItemCommon, configuration, monitor );
      if (retainSRELvalue != null && retainSRELvalue.length() > 0)
         returnValue = retainSRELvalue;

      // Return the release or component SREL
      return returnValue;
   }

   /** 
    * _RTS? - delete, use Comp Browse utils static method
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
      // Get base release attr from workItem argument
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
    * Determine SREL from cached RETAIN data.
    * 
    * <br>Reuse existing utility instance to obtain cached RETAIN data for
    * the specified component in the APAR work item. Utilize its method
    * to gather values based on the system release attribute applied to
    * this calculated value provider in RTC.
    * 
    * <p>Note: This may induce a RETAIN request via SBS to gather the
    * component data on 1st reference and cache it. In *most* cases a
    * component will be supplied, and a release may be selected, which
    * would induce this 1st reference and cache the data.
    * However, if a release was manually entered, the component data
    * might not yet be cached, so account for that here by using the
    * getValueSet "front door" which handles this.
    * 
    * <p>Assume only 1 SREL "S" type release will be defined, or just
    * use 1st definition.
    * 
    * @param attribute
    * @param workItem
    * @param workItemCommon
    * @param configuration
    * @param monitor
    * @return
    * @throws TeamRepositoryException 
    */
   public static String getSRELValue( IAttribute attribute,
         IWorkItem workItem, IWorkItemCommon workItemCommon,
         IConfiguration configuration, IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      
      String retainSRELvalue = "";
      // Create a utility instance to obtain cached RETAIN data.
      ComponentBrowseValueSetUtils_SaveE255566b retainComponentCacheData = new ComponentBrowseValueSetUtils_SaveE255566b();
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
      @SuppressWarnings("unchecked")
      List<String> componentSRELvalues = retainComponentCacheData.getValueSet(
            attribute, workItem, workItemCommon, configuration, monitor );
      // If no SREL defined, return empty value.
      if (componentSRELvalues == null || componentSRELvalues.isEmpty())
         return retainSRELvalue;
      // Otherwise get the 1st SREL value.
      // Assume only 1 SREL "S" type release will be defined, or just
      // use 1st definition.
      retainSRELvalue = componentSRELvalues.get( 0 );
      // There may be errors msgs in the returned results? Length <= 3
      // means it is a valid release, not error msg. On an error, treat
      // like no SREL defined.
      if (retainSRELvalue != null && retainSRELvalue.length() > 3)
         retainSRELvalue = "";

      return retainSRELvalue;
   }

}
