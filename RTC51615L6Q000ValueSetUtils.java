package com.ibm.sport.rtc.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IConfiguration;
import com.ibm.team.workitem.common.internal.attributeValueProviders.IFilteredValueSetProvider;
// import com.ibm.team.workitem.common.internal.attributeValueProviders.IValueSetProvider;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/** 
 * _T21519A RETAIN Component browse to obtain dynamic choices
 * for release, change team, and applicable releases (with some
 * result formatting - <release> + <Y|N>)
 * @author stawick
 *
 */
public class RTC51615L6Q000ValueSetUtils
      implements IFilteredValueSetProvider<Object>
      // implements IValueSetProvider<Object>
{
   protected static String noComponentErrMsg1 = "*** Values to select are derived from the component.";
   protected static String noComponentErrMsg2 = "*** Please specify a component and try again.";
   protected static String notifySPoRTExcpMsg = "*** SPoRT SBS error obtaining values! Check SPoRT SBS logs. Try again later.";
   protected static String notifyOtherExcpMsg = "*** Error encountered obtaining values! Check logs. Try again later.";

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
      // Test multi-choice for string list attribute.
      List returnValues = getValueSet( attribute, workItem, workItemCommon,
            configuration, monitor );
      // No filtering for this test.
      // List<String> filteredValues = returnValues;
      
      // /*
      if (filter == null || filter.trim().length() == 0)
      {
         // No filter regex, use all choices
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
      // */

      return (filteredValues == null ? new ArrayList<String>()
            : filteredValues);
   }

   @SuppressWarnings("unchecked")
   public List getValueSet( IAttribute attribute, IWorkItem workItem,
         IWorkItemCommon workItemCommon, IConfiguration configuration,
         IProgressMonitor monitor )
         throws TeamRepositoryException
   {
      List<String> returnValues = new ArrayList<String>();

      // For test purposes, simply return x choices.
      returnValues.add( "010Y" );
      returnValues.add( "010N" );
      returnValues.add( "011Y" );
      returnValues.add( "011N" );
      returnValues.add( "020Y" );
      returnValues.add( "020N" );
      returnValues.add( "030Y" );
      returnValues.add( "030N" );
      returnValues.add( "040Y" );
      returnValues.add( "040N" );
      returnValues.add( "050Y" );
      returnValues.add( "050N" );
      returnValues.add( "060Y" );
      returnValues.add( "060N" );
      returnValues.add( "070Y" );
      returnValues.add( "070N" );
      returnValues.add( "080Y" );
      returnValues.add( "080N" );
      returnValues.add( "090Y" );
      returnValues.add( "090N" );
      returnValues.add( "123Y" );
      returnValues.add( "123N" );
      returnValues.add( "145Y" );
      returnValues.add( "145N" );
      returnValues.add( "167Y" );
      returnValues.add( "167N" );
      returnValues.add( "189Y" );
      returnValues.add( "189N" );
      returnValues.add( "456Y" );
      returnValues.add( "456N" );
      returnValues.add( "789Y" );
      returnValues.add( "789N" );
      /*
      returnValues.add( "test choices to follow" );
      returnValues.add( "test value 1" );
      returnValues.add( "test value 2" );
      returnValues.add( "test value 3" );
      returnValues.add( "test value 4" );
      returnValues.add( "test value 5" );
      returnValues.add( "test value 6" );
      returnValues.add( "test value 7" );
      returnValues.add( "test value 8" );
      returnValues.add( "test value 9" );
      returnValues.add( "test value 10" );
      returnValues.add( "test value x" );
      returnValues.add( "test value y" );
      returnValues.add( "test value a" );
      returnValues.add( "test value z" );
      returnValues.add( "test value 09" );
      */

      // Prevent null pointer exception in value-set picker presentation
      // when list returned from cache is empty.
      // Return empty list in this case.
      // return returnValues;
      return (returnValues == null ? new ArrayList<String>() : returnValues);
   }

}