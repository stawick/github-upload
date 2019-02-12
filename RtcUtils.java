package com.ibm.sport.rtc.common;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.ArtifactTechnology.common.Constants;
import com.ibm.ArtifactTechnology.common.DateTimeString;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.links.common.registry.ILinkType;
import com.ibm.team.process.common.IProcessConfigurationData;
import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.IItemType;
import com.ibm.team.repository.common.LogFactory;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.IAuditableCommonProcess;
import com.ibm.team.workitem.common.IQueryCommon;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.expression.AttributeExpression;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.expression.IQueryableAttributeFactory;
import com.ibm.team.workitem.common.expression.QueryableAttributes;
import com.ibm.team.workitem.common.expression.Term;
import com.ibm.team.workitem.common.expression.Term.Operator;
import com.ibm.team.workitem.common.internal.util.SeparatedStringList;
import com.ibm.team.workitem.common.model.AttributeOperation;
import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IAttributeHandle;
import com.ibm.team.workitem.common.model.ICategory;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IDeliverable;
import com.ibm.team.workitem.common.model.IDeliverableHandle;
import com.ibm.team.workitem.common.model.IEnumeration;
import com.ibm.team.workitem.common.model.ILiteral;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.common.model.WorkItemLinkTypes;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;
import com.ibm.team.workitem.common.workflow.IWorkflowAction;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;

// _T266500C The SeparatedStringList is apparently an internal RTC
// type, and access is discouraged. But we need to use it for
// stringList attribute value access. So suppress the warnings for the
// import and reference(s).
@SuppressWarnings("restriction")
public class RtcUtils
{
   public static final boolean SEARCH_ON_FIRST_STRING_ONLY_FALSE = false;
   public static final boolean SEARCH_ON_FIRST_STRING_ONLY_TRUE = true;
   public static final boolean SEARCH_ON_FIRST_STRING_ONLY_DEFAULT = false;
   // _T276100A Known deprecated attribute IDs.
   // These are all currently identified as deprecated in the process
   // template. Keep this up to date when old attributes get
   // deprecated.
   // A test is done to see if an attempt is made to modify any of
   // these and an exception gets thrown.
   protected final static ArrayList<String> KNOWN_DEPRECATED_ATTRIBUTE_IDS = new ArrayList<String>();
   static
   {
      /**
       * APAR related
       */
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.applicableComponentLvls" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.draftApplicableComponentLvls" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.draftApplicableComponentLvls1" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.draftApplicableComponentLvlsSelector" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.draftModulesMacros" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.apar.draftSrls" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.flagSetReasonCode" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.apar.hiperKeyword" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.apar.hiperRelief" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.apar.modulesMacros" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.apar.operand" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.productSpecific" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.pmr.retainPriority" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.apar.srls" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.storeApplicableComponentLvls1" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.symptomKeyword" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.toApplicableComponentLevels" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.toApplicableComponentLevels1" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.apar.toApplicableComponentLevelsSelector" );

      /**
       * PMR related
       */
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.pmr.callList" );

      /**
       * PTF related
       */
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.ptf.feedbackInput" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.ptf.ptfType" );

      /**
       * Legal checklist related
       */
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q1text" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q1atext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q1btext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q2text" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q2atext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q2btext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q2ctext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q3text" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q3atext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q3btext" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q4text" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q5text" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q6text" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workitem.attribute.legalchecklist.q7text" );

      /**
       * Call Queue related
       */
      KNOWN_DEPRECATED_ATTRIBUTE_IDS
            .add( "com.ibm.sport.rtc.workItem.attribute.callqueue.callList" );
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add(
            "com.ibm.sport.rtc.workItem.attribute.callqueue.callQueueCenterID" );

      // For future enhancement.
      /*
      KNOWN_DEPRECATED_ATTRIBUTE_IDS.add( "" );
      */
   }

   public static boolean attributeIsEnumeration(
         String attributeTypeIdentifier )
   {
      return AttributeTypes
            .isEnumerationAttributeType( attributeTypeIdentifier );
   }

   public static boolean calledFromSportPlugin( String startClass,
         String startMethod )
   {
      boolean calledFromSportPlugin = false;
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      boolean foundStartMethod = false;
      for (StackTraceElement stackTraceElement : stackTrace)
      {
         String stackTraceClass = stackTraceElement.getClassName();
         String stackTraceMethod = stackTraceElement.getMethodName();
         if (!foundStartMethod)
         {
            if (stackTraceClass.equals( startClass )
                  && stackTraceMethod.equals( startMethod ))
               foundStartMethod = true;
         }
         else
         {
            if (stackTraceClass
                  .startsWith( CommonConstants.SPORT_RTC_PLUGIN_PREFIX ))
            {
               calledFromSportPlugin = true;
               break;
            }
         }
      }
      return calledFromSportPlugin;
   }

   /**
    * Return the action name corresponding to an action ID.
    * 
    * @param actionId a <code>String</code> specifying the action ID for which
    *        the action name is desired.
    * 
    * @param workItem a {@link IWorkItem} object representing the work item
    *        for which the action is being initiated.
    * 
    * @param sportCommonData a {@link SportCommonData} object caching the
    *        classes needed to perform the operation.
    * 
    * @return a <code>String</code> naming the action associated with
    *         <code>actionId</code>, or <code>null</code> if the action ID
    *         could not be found in the repository.
    * @throws TeamRepositoryException
    */
   public static final String findActionName( String actionId,
         IWorkItem workItem, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkflowInfo workflowInfo = sportCommonData.getWorkItemCommon()
            .findWorkflowInfo( workItem, sportCommonData.getMonitor() );

      return findActionName( workflowInfo, actionId );
   }

   public static String findActionName( IWorkflowInfo workflowInfo,
         String actionIdString )
   {
      Identifier<IWorkflowAction>[] actionIds = workflowInfo
            .getAllActionIds();

      for (Identifier<IWorkflowAction> actionId : actionIds)
      {
         if (actionId.getStringIdentifier().equals( actionIdString ))
         {
            return workflowInfo.getActionName( actionId );
         }
      }
      return null;
   }

   public static IAttribute findAttribute( IWorkItem workItem,
         String attributeId, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IProjectAreaHandle projectArea = sportCommonData.getProjectArea();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();

      Log log = sportCommonData.getLog();
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      StringBuilder message = new StringBuilder( "Custom attributes for \""
            + workItem.getWorkItemType() + "\":\n" );
      List<IAttributeHandle> customAttributes = workItem
            .getCustomAttributes();
      for (IAttributeHandle customAttribute : customAttributes)
      {
         message.append( "  " + ((IAttribute)auditableCommon.resolveAuditable(
               customAttribute,
               ItemProfile.<IAttribute> computeProfile(
                     (IAttribute)IAttribute.ITEM_TYPE.createItem() ),
               monitor )).getIdentifier() + "\n" );
      }
      log.debug( message.toString() );

      IAttribute attribute = workItemCommon.findAttribute( projectArea,
            attributeId, monitor );
      if ((attribute != null) && !workItem.hasAttribute( attribute ))
         attribute = null;
      return attribute;
   }

   // Modified to call overloaded method with hard-coded (default)
   // boolean value of 'false'
   public static ILiteral findEnumerationLiteral(
         IEnumeration<? extends ILiteral> enumeration, String value )
   {
      return findEnumerationLiteral( enumeration, value,
            RtcUtils.SEARCH_ON_FIRST_STRING_ONLY_DEFAULT );
   }

   // Added boolean parameter (searchOnFirstStringOnly)
   // searchOnFirstStringOnly will cause this method to return the first
   // value in the enumeration whose first string (up to the first space)
   // matches the passed in 'value'. (ie. value passed in = "C" would match
   // "C - common problem".
   // The full string value will be returned.
   public static ILiteral findEnumerationLiteral(
         IEnumeration<? extends ILiteral> enumeration, String value,
         boolean searchOnFirstStringOnly )
   {
      List<? extends ILiteral> literals = enumeration
            .getEnumerationLiterals();
      for (ILiteral literal : literals)
      {
         if (searchOnFirstStringOnly)
         {
            String firstString;
            String[] subStrings = literal.getName().split( " " );
            if (subStrings != null && subStrings.length >= 1)
            {
               firstString = subStrings[0];
            }
            else
            {
               firstString = literal.getName();
            }

            if (value.startsWith( firstString.trim() ))
            {
               return literal;
            }
         }
         else
         {
            if (literal.getName().equals( value ))
               return literal;
         }
      }
      return null;
   }

   public static IQueryableAttribute findQueryableAttribute(
         String attributeId, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IProjectAreaHandle projectArea = sportCommonData.getProjectArea();
      IQueryableAttributeFactory queryFactory = QueryableAttributes
            .getFactory( IWorkItem.ITEM_TYPE );
      return queryFactory.findAttribute( projectArea, attributeId,
            auditableCommon, monitor );
   }

   public static Identifier<IState> findWorkflowStateId( IWorkItem workItem,
         String workflowState, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      IWorkflowInfo workflowInfo = workItemCommon.findWorkflowInfo( workItem,
            monitor );
      Identifier<IState>[] workflowStateIds = workflowInfo.getAllStateIds();
      for (Identifier<IState> currentWorkflowStateId : workflowStateIds)
      {
         String currentWorkflowState = workflowInfo
               .getStateName( currentWorkflowStateId );
         if (workflowState.equals( currentWorkflowState ))
            return currentWorkflowStateId;
      }
      return null;
   }

   public static final IContributor findContributor(
         IContributorHandle contributorHandle,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      ItemProfile<IContributor> profile = ItemProfile
            .createFullProfile( IContributor.ITEM_TYPE );
      return (IContributor)auditableCommon.resolveAuditable(
            contributorHandle, profile, sportCommonData.getMonitor() );
   }

   public static String getWorkItemErrorPrefix( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      String workItemTypeName = RtcUtils.getWorkItemTypeName( workItem,
            sportCommonData );
      return workItemTypeName + " " + workItem.getId() + ": ";
   }

   public static String getWorkItemState( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      return getWorkflowStateName( workItem.getState2(), workItem,
            sportCommonData );
   }

   public static Identifier<IState> getStateId( String stateId,
         IWorkItem workItem, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      IWorkflowInfo workflowInfo = workItemCommon.findWorkflowInfo( workItem,
            monitor );
      Identifier<IState>[] stateIds = workflowInfo.getAllStateIds();

      if (stateIds != null)
      {
         for (Identifier<IState> id : stateIds)
         {
            if (id.getStringIdentifier().equals( stateId ))
            {
               return id;
            }
         }
      }

      return null;
   }

   public static String getWorkflowStateName(
         Identifier<IState> workflowStateId, IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      IWorkflowInfo workflowInfo = workItemCommon.findWorkflowInfo( workItem,
            monitor );
      return workflowInfo.getStateName( workflowStateId );
   }

   public static String getWorkItemTypeName( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IProjectAreaHandle projectArea = workItem.getProjectArea();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      IWorkItemType workItemType = workItemCommon.findWorkItemType(
            projectArea, workItem.getWorkItemType(), monitor );
      if (workItemType == null)
      {
         String errorPrefix = "Work Item " + workItem.getId() + ": ";
         throw new SportRuntimeException(
               errorPrefix + "unable to find work item type \""
                     + workItem.getWorkItemType() + "\"" );
      }
      return workItemType.getDisplayName();
   }

   public static Boolean getAttributeValueAsBoolean( IWorkItem workItem,
         String attributeId, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAttribute attribute = findAttribute( workItem, attributeId,
            sportCommonData );
      if (attribute == null)
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException( errorPrefix + "work item type \""
               + workItem.getWorkItemType()
               + "\" does not have the attribute \"" + attributeId + "\"" );
      }
      if (!attribute.getAttributeType().equals( "boolean" ))
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException( errorPrefix + "attribute \""
               + attributeId + "\" is not type Boolean" );
      }
      return (Boolean)(workItem.getValue( attribute ));
   }

   public static ILiteral getAttributeValueAsEnumLiteral( IWorkItem workItem,
         String attributeId, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAttribute attribute = findAttribute( workItem, attributeId,
            sportCommonData );
      if (attribute == null)
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException( errorPrefix + "work item type \""
               + workItem.getWorkItemType()
               + "\" does not have the attribute \"" + attributeId + "\"" );
      }
      if (!RtcUtils.attributeIsEnumeration( attribute.getAttributeType() ))
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException( errorPrefix + "attribute \""
               + attributeId + "\" is not enumeration type" );
      }

      Object attributeValue = workItem.getValue( attribute );
      ILiteral attributeLiteral = sportCommonData.getWorkItemCommon()
            .resolveEnumeration( attribute, sportCommonData.getMonitor() )
            .findEnumerationLiteral(
                  unsafeCastToIdentifierExtendsILiteral( attributeValue ) );
      return attributeLiteral;
   }

   // TODO May need to support multiple values being returned
   public static String getAttributeValueAsString( IWorkItem workItem,
         String attributeId, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAttribute attribute = findAttribute( workItem, attributeId,
            sportCommonData );
      if (attribute == null)
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException( errorPrefix + "work item type \""
               + workItem.getWorkItemType()
               + "\" does not have the attribute \"" + attributeId + "\"" );
      }
      String attributeStringValue = getAttributeValueAsString( workItem,
            attribute, sportCommonData );

      return attributeStringValue;
   }

   public static String getAttributeValueAsString( IWorkItem workItem,
         IAttribute attribute, SportCommonData sportCommonData )
         throws TeamRepositoryException, SportRuntimeException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();

      if (attribute == null)
      {
         throw new IllegalArgumentException( "attribute cannot be null" );
      }
      if (!workItem.hasAttribute( attribute ))
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException(
               errorPrefix + "work item type \"" + workItem.getWorkItemType()
                     + "\" does not have the attribute \""
                     + attribute.getIdentifier() + "\"" );
      }
      String attributeType = attribute.getAttributeType();
      Object attributeValue = workItem.getValue( attribute );
      String attributeStringValue;
      if (attributeValue == null)
         attributeStringValue = null;
      else if (attributeType.equals( AttributeTypes.BOOLEAN ))
         attributeStringValue = (Boolean)attributeValue ? "true" : "false";
      else if (attributeType.equals( AttributeTypes.INTEGER ))
         attributeStringValue = ((Integer)attributeValue).toString();
      else if (attributeType.equals( AttributeTypes.LARGE_STRING ))
         attributeStringValue = (String)attributeValue;
      else if (attributeType.equals( AttributeTypes.SMALL_STRING ))
         attributeStringValue = (String)attributeValue;
      else if (attributeType.equals( AttributeTypes.MEDIUM_HTML ))
         attributeStringValue = (String)attributeValue;
      else if (attributeType.equals( AttributeTypes.LARGE_HTML ))
         attributeStringValue = (String)attributeValue;
      else if (attributeType.equals( AttributeTypes.STRING_LIST ))
      {
         // T266500A Multiple selections requires this type used.
         // toString returns "[<1stvalue>{<comma><space><nextvalue>}*]"
         SeparatedStringList stringListValue = (SeparatedStringList)attributeValue;
         attributeStringValue = stringListValue.toString();
      }
      else if (attributeType.equals( AttributeTypes.TIMESTAMP ))
         attributeStringValue = attributeValue.toString();
      else if (RtcUtils.attributeIsEnumeration( attributeType ))
      {
         ILiteral attributeLiteral = workItemCommon
               .resolveEnumeration( attribute, monitor )
               .findEnumerationLiteral( unsafeCastToIdentifierExtendsILiteral(
                     attributeValue ) );
         if (attributeLiteral == null)
         {
            String errorPrefix = getWorkItemErrorPrefix( workItem,
                  sportCommonData );
            throw new SportRuntimeException(
                  errorPrefix + "unable to resolve attribute value \""
                        + ((Identifier<?>)attributeValue)
                              .getStringIdentifier()
                        + "\" for attribute \"" + attribute.getIdentifier()
                        + "\"" );
         }
         attributeStringValue = attributeLiteral.getName();
      }
      else if (attributeType.equals( AttributeTypes.CATEGORY ))
      {
         ICategory category = getCategory( (ICategoryHandle)attributeValue,
               sportCommonData );
         attributeStringValue = category.getItemId().getUuidValue();
      }
      else if (attributeType.equals( AttributeTypes.DELIVERABLE ))
      {
         IDeliverable deliverable = getDeliverable(
               (IDeliverableHandle)attributeValue, sportCommonData );
         attributeStringValue = deliverable.getItemId().getUuidValue();
      }
      else if (attributeType.equals( AttributeTypes.WIKI ))
      {
         // _B68102A Handle a Wiki attribute type
         attributeStringValue = (String)attributeValue;
      }
      else
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException( errorPrefix
               + "unsupported attribute value class \""
               + attributeValue.getClass().getName() + "\" (from attribute \""
               + attribute.getIdentifier() + "\") encountered" );
      }

      return attributeStringValue;
   }

   public static <T extends IItemHandle> List<T> getAttributeValueAsList(
         Class<T> listItemType, IWorkItem workItem, String attributeId,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportRuntimeException
   {
      IAttribute attribute = findAttribute( workItem, attributeId,
            sportCommonData );
      if (attribute == null)
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         throw new SportRuntimeException(
               (new StringBuilder( String.valueOf( errorPrefix ) ))
                     .append( "work item type \"" )
                     .append( workItem.getWorkItemType() )
                     .append( "\" does not have the attribute \"" )
                     .append( attributeId ).append( "\"" ).toString() );
      }
      else
      {
         return getAttributeValueAsList( listItemType, workItem, attribute,
               sportCommonData );
      }
   }

   @SuppressWarnings("unchecked")
   public static <T extends IItemHandle> List<T> getAttributeValueAsList(
         Class<T> listItemType, IWorkItem workItem, IAttribute attribute,
         SportCommonData sportCommonData )
         throws TeamRepositoryException, SportRuntimeException
   {
      String attrType = AttributeTypes
            .getAttributeType( attribute.getAttributeType() ).getIdentifier();
      if (!AttributeTypes.isListAttributeType( attrType ))
      {
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         StringBuilder sb = new StringBuilder( errorPrefix );
         sb.append( "work item type \"" );
         sb.append( workItem.getWorkItemType() );
         sb.append( "\" attribute type (" );
         sb.append( attrType );
         sb.append( " ) " );
         sb.append( attribute.getIdentifier() );
         sb.append( " is not a list type attribute" );
         throw new SportRuntimeException( sb.toString() );
      }
      List<?> attributeValues = (List<?>)workItem.getValue( attribute );
      if (attributeValues != null)
      {
         List<T> attrValList = new ArrayList<T>( attributeValues.size() );
         for (Object attrVal : attributeValues)
         {
            if (!listItemType.isAssignableFrom( attrVal.getClass() ))
            {
               String errorPrefix = getWorkItemErrorPrefix( workItem,
                     sportCommonData );
               StringBuilder sb = new StringBuilder( errorPrefix );
               sb.append( "work item type \"" );
               sb.append( workItem.getWorkItemType() );
               sb.append( "\" attribute " );
               sb.append( attribute.getIdentifier() );
               sb.append(
                     " value list contains an item that is not an instance of  " );
               sb.append( listItemType.getClass().getName() );
               throw new SportRuntimeException( sb.toString() );
            }

            T itemHandle = (T)attrVal;
            attrValList.add( itemHandle );
         }

         return attrValList;
      }
      else
      {
         return null;
      }
   }

   public static String[] getResolvedAction( IWorkItem workItem,
         String[] actions, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      IWorkflowInfo workflowInfo = workItemCommon.findWorkflowInfo( workItem,
            monitor );
      String[] resolvedActions = new String[actions.length];
      for (int i = 0; i < actions.length; ++i)
      {
         resolvedActions[i] = actions[i];
         String[] actionParts = actions[i].split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "action" ))
         {
            resolvedActions[i] = "action/"
                  + RtcUtils.findActionName( workflowInfo, actionParts[1] );
         }
      }
      return resolvedActions;
   }

   public static IContributor getUser( SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      return auditableCommon.resolveAuditable( auditableCommon.getUser(),
            ItemProfile.CONTRIBUTOR_DEFAULT, monitor );
   }

   public static SbsConfigurationData getSbsConfigurationData(
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      SbsConfigurationData configData = new SbsConfigurationData();
      IProcessConfigurationData processConfigData = getProcessConfigurationData(
            "com.ibm.sport.rtc.configuration.sbs", sportCommonData );
      if (processConfigData != null)
      {
         for (IProcessConfigurationElement configElement : processConfigData
               .getElements())
         {
            updateSbsConfigurationData( configData, configElement );
         }
      }
      return configData;
   }

   private static void updateSbsConfigurationData(
         SbsConfigurationData configData,
         IProcessConfigurationElement configElement )
   {
      String processConfigElementName = configElement.getName();
      if (processConfigElementName.equals( "sbsUser" ))
      {
         configData
               .setSbsUserName( configElement.getAttribute( "userName" ) );
         configData
               .setSbsPassword( configElement.getAttribute( "password" ) );
      }
      else if (processConfigElementName.equals( "sbsProvider" ))
      {
         configData
               .setSbsProviderName( configElement.getAttribute( "name" ) );
         configData
               .setSbsProviderType( configElement.getAttribute( "type" ) );
         configData
               .setSbsProviderClass( configElement.getAttribute( "class" ) );
      }
      else if (processConfigElementName.equals( "sbsServer" ))
      {
         configData.setSbsServerHostName(
               configElement.getAttribute( "hostName" ) );
         try
         {
            configData.setSbsServerPortNumber( Integer
                  .parseInt( configElement.getAttribute( "portNumber" ) ) );
         }
         catch (NumberFormatException e)
         {
            configData.setSbsServerPortNumber( null );
         }

         try
         {
            configData.setSbsServerSslPortNumber( Integer.parseInt(
                  configElement.getAttribute( "sslPortNumber" ) ) );
         }
         catch (NumberFormatException e)
         {
            configData.setSbsServerSslPortNumber( null );
         }

         // try
         // {
         // configData.setUseSsl( UseSsl.valueOf( configElement
         // .getAttribute( "useSsl" ) ) );
         // }
         // catch (IllegalArgumentException e)
         // {
         // configData.setUseSsl( UseSsl.AUTH );
         // }

         configData.setSbsServerContextRoot(
               configElement.getAttribute( "contextRoot" ) );
      }
      else if (processConfigElementName.equals( "sportRtcIdCompCTMapping" ))
      {
         configData.setComponentsAndChangeTeamsForRtcId(
               configElement.getAttribute( "id" ),
               configElement.getAttribute( "components" ),
               configElement.getAttribute( "changeTeams" ) );
      }
      else if (processConfigElementName.equals( "aparReopenConfiguration" ))
      {
         configData.setReopenConfig( configElement.getAttribute( "merge" ) );
      }
      else if (processConfigElementName
            .equals( "subscribedPMRsConfiguration" ))
      {
         configData.setPullInSubscribedPMRsConfig(
               configElement.getAttribute( "pullin" ) );
      }
      else
      {
         for (IProcessConfigurationElement child : configElement
               .getChildren())
         {
            updateSbsConfigurationData( configData, child );
         }
      }
   }

   public static ICategory getCategory( ICategoryHandle categoryHandle,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      return auditableCommon.resolveAuditable( categoryHandle,
            ICategory.FULL_PROFILE, monitor );
   }

   public static IDeliverable getDeliverable(
         IDeliverableHandle deliverableHandle,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      return auditableCommon.resolveAuditable( deliverableHandle,
            IDeliverable.FULL_PROFILE, monitor );
   }

   public static IQueryResult<IResolvedResult<IWorkItem>> queryWorkItemsByAttributes(
         Map<IQueryableAttribute, Object> attributeValues,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IProjectAreaHandle projectArea = sportCommonData.getProjectArea();
      IQueryCommon queryCommon = sportCommonData.getQueryCommon();
      Term term = new Term( Operator.AND );
      // TODO determine if we really need to add a project area expression
      IQueryableAttribute projectAreaAttribute = RtcUtils
            .findQueryableAttribute( IWorkItem.PROJECT_AREA_PROPERTY,
                  sportCommonData );
      AttributeExpression projectAreaExpression = new AttributeExpression(
            projectAreaAttribute, AttributeOperation.EQUALS, projectArea );
      term.add( projectAreaExpression );
      for (Map.Entry<IQueryableAttribute, Object> entry : attributeValues
            .entrySet())
      {
         IQueryableAttribute attribute = entry.getKey();
         Object attributeValue = entry.getValue();
         AttributeExpression expression = new AttributeExpression( attribute,
               AttributeOperation.EQUALS, attributeValue );
         term.add( expression );
      }
      return queryCommon.getResolvedExpressionResults( projectArea, term,
            IWorkItem.FULL_PROFILE );
   }

   public static void setAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      setAttributeValue( workItem, attribute, value, sportCommonData,
            RtcUtils.SEARCH_ON_FIRST_STRING_ONLY_DEFAULT );
   }

   public static void setAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData,
         boolean searchOnFirstStringOnly )
         throws TeamRepositoryException
   {
      String attributeType = attribute.getAttributeType();
      if (attributeType.equals( AttributeTypes.BOOLEAN ))
         setBooleanAttributeValue( workItem, attribute, value,
               sportCommonData );
      else if (attributeIsEnumeration( attributeType ))
         setEnumerationAttributeValue( workItem, attribute, value,
               sportCommonData, searchOnFirstStringOnly );
      else if (attributeType.equals( AttributeTypes.SMALL_STRING ))
         setSmallStringAttributeValue( workItem, attribute, value );
      else if (attributeType.equals( AttributeTypes.LARGE_STRING ))
         setLargeStringAttributeValue( workItem, attribute, value );
      else if (attributeType.equals( AttributeTypes.STRING_LIST ))
      {
         // _T266500A Multi-select attributes use stringList type.
         setStringListAttributeValue( workItem, attribute, value );
      }
      else if (attributeType.equals( AttributeTypes.MEDIUM_HTML ))
         setMediumHtmlAttributeValue( workItem, attribute, value );
      else if (attributeType.equals( AttributeTypes.LARGE_HTML ))
         setLargeHtmlAttributeValue( workItem, attribute, value );
      else if (attributeType.equals( AttributeTypes.TIMESTAMP ))
         setTimestampAttributeValue( workItem, attribute, value,
               sportCommonData );
      else if (attributeType.equals( AttributeTypes.INTEGER ))
         setIntegerAttributeValue( workItem, attribute, value,
               sportCommonData );
      else if (attributeType.equals( AttributeTypes.CATEGORY ))
      {
         setCategoryAttributeValue( workItem, attribute, value,
               sportCommonData );
      }
      else if (attributeType.equals( AttributeTypes.DELIVERABLE ))
      {
         setDeliverableAttributeValue( workItem, attribute, value,
               sportCommonData );
      }
      else if (attributeType.equals( AttributeTypes.WIKI ))
      {
         // _B68102A Handle a Wiki attribute type
         setWikiAttributeValue( workItem, attribute, value );
      }
      else
      {
         String attributeId = attribute.getIdentifier();
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         String message = errorPrefix + "attribute \"" + attributeId
               + "\" has an unsuuported type \""
               + attribute.getAttributeType() + "\", attribute value not set";
         Log log = LogFactory.getLog( attributeId );
         log.error( message );
      }
   }

   public static void setBooleanAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if ((value != null) && value.equalsIgnoreCase( "false" ))
         workItem.setValue( attribute, Boolean.FALSE );
      else if ((value != null) && value.equalsIgnoreCase( "true" ))
         workItem.setValue( attribute, Boolean.TRUE );
      else
      {
         String attributeId = attribute.getIdentifier();
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         String message = errorPrefix + "\"" + value + "\" is not a valid \""
               + attributeId + "\" boolean value, attribute value not set";
         Log log = LogFactory.getLog( attributeId );
         log.error( message );
      }
   }

   public static void setEnumerationAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      setEnumerationAttributeValue( workItem, attribute, value,
            sportCommonData, RtcUtils.SEARCH_ON_FIRST_STRING_ONLY_DEFAULT );
   }

   public static void setEnumerationAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData,
         boolean searchOnFirstStringOnly )
         throws TeamRepositoryException
   {
      IProgressMonitor monitor = sportCommonData.getMonitor();
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      IEnumeration<? extends ILiteral> enumeration = workItemCommon
            .resolveEnumeration( attribute, monitor );
      ILiteral literal;
      if ((value == null) || value.equals( "" ))
      {
         literal = enumeration.findNullEnumerationLiteral();
         if (literal == null)
         {
            String attributeId = attribute.getIdentifier();
            String errorPrefix = getWorkItemErrorPrefix( workItem,
                  sportCommonData );
            String message = errorPrefix + "attribute \"" + attributeId
                  + "\" has no unassigned enumeration value, "
                  + "attribute value not set";
            Log log = LogFactory.getLog( attributeId );
            log.error( message );
            return;
         }
      }
      else
      {
         literal = findEnumerationLiteral( enumeration, value,
               searchOnFirstStringOnly );
         if (literal == null)
         {
            String attributeId = attribute.getIdentifier();
            String errorPrefix = getWorkItemErrorPrefix( workItem,
                  sportCommonData );
            String message = errorPrefix + "\"" + value
                  + "\" is not a valid \"" + attributeId
                  + "\" enumeration value, " + "attribute value not set";
            Log log = LogFactory.getLog( attributeId );
            log.error( message );
            return;
         }
      }

      workItem.setValue( attribute, literal.getIdentifier2() );
   }

   public static void setLargeStringAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value )
         throws TeamRepositoryException
   {

      String setValue = null;
      if (value == null)
         setValue = "";
      else
      {
         String attributeId = attribute.getIdentifier();
         Log log = LogFactory.getLog( attributeId );
         setValue = StringUtils.truncateLargeString( value );
         try
         {
            if (setValue.getBytes( "UTF-8" ).length < value
                  .getBytes( "UTF-8" ).length)
               log.warn( "truncation executed for workitem "
                     + workItem.getId() + " with attributeId = " + attributeId
                     + " value has len = " + value.getBytes( "UTF-8" ).length
                     + " setValue has len = "
                     + setValue.getBytes( "UTF-8" ).length );
         }
         catch (UnsupportedEncodingException e)
         {
            setValue = "";
         }
      }
      workItem.setValue( attribute, setValue );
   }

   // _T266500A Multi-select attributes use stringList type.
   /**
    * Multi-select attributes use stringList type. Internally this is
    * a SeparatedStringList. The toString value format of this type is:
    * <br>"[v1, v2, ..., vN]" (comma AND space delimited and square
    * bracketed).
    * <br>Parse the string value into "selected" items in the work item
    * value. This must reflect what the string interpretation of the
    * work item value is determined to be, and reverse that process.
    * 
    * @param workItem
    * @param attribute
    * @param value - expected form = "[v1, v2, ..., vN]"
    * (comma AND space delimited and square bracketed, but really
    * brackets are optional, comma and space delimiter required as
    * toString on RTC type prescribes, will also handle newline
    * separated entries).
    * @throws TeamRepositoryException
    */
   public static void setStringListAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value )
         throws TeamRepositoryException
   {
      workItem.setValue( attribute,
            convertStringToStringList( workItem, attribute, value ) );
   }

   // _T266500A Multi-select attributes use stringList type.
   /**
    * This is a general use method to convert a string value into the
    * internal object type that RTC uses for stringList type
    * attributes.
    * Multi-select attributes use stringList type. Internally this is
    * a SeparatedStringList. The toString value format of this type is:
    * <br>"[v1, v2, ..., vN]" (comma AND space delimited and square
    * bracketed).
    * <br>Parse the input string value into "selected" items in the
    * stringList object result. This must reflect what the string
    * interpretation of the stringList type is determined to be, and
    * reverse that process.
    * <br>For flexibility, the brackets are optional. The delimiter may
    * either be comma AND space, or newlines (for flexibility in SBS
    * mapping results).
    * 
    * @param workItem
    * @param attribute
    * @param value
    * @return - a SeparatedStringList object, empty, or populated
    *           with values parsed out of the input. Does not return
    *           null since that throws an exception when setting as
    *           an attribute value.
    * @throws TeamRepositoryException
    */
   public static SeparatedStringList convertStringToStringList(
         IWorkItem workItem, IAttribute attribute, String value )
         throws TeamRepositoryException
   {
      // Initially empty -
      SeparatedStringList stringListValue = new SeparatedStringList(); 
      if (value != null && value.length() > 0)
      {
         // Parse through the values and add to the work item value.
         // Remove optional brackets. Leaving just
         // "v1, v2, ..., vN"
         value = value.replaceAll( CommonConstants.SQUARE_BRACKET_LEFT, "" );
         value = value.replaceAll( CommonConstants.SQUARE_BRACKET_RIGHT, "" );
         // In case newline separated, treat as comma+space delimited
         // Pattern === "(\r?\n)"
         value = value.replaceAll( CommonConstants.LINE_SPLIT_PATTERN, ", " );
         // Split at comma and space, as toString on RTC type
         // prescribes.
         // Pattern === "(,\\s)+", comma AND space - 1 or more.
         // Eliminates "dead" (empty) entries (ex v1, , , , vI are 2
         // values v1 and vI).
         String attributeSelections[] = value
               .split( CommonConstants.COMMA_AND_SPACE_SPLIT_PATTERN );
         if (attributeSelections != null && attributeSelections.length > 0)
         {
            for (String attributeSelection : attributeSelections)
            {
               // Do not add empty values. (Test to be sure.)
               if (attributeSelection != null
                     && attributeSelection.trim().length() > 0)
               {
                  stringListValue.add( attributeSelection.trim() );
               }
            }
         }

      }
      return stringListValue;
   }

   // _T266500A Multi-select attributes use stringList type.
   /**
    * This is a general use method to convert a stringList value into
    * a format usable for the specific context called from.
    * Multi-select attributes use stringList type. Internally this is
    * a SeparatedStringList. The toString value format of this type is:
    * <br>"[v1, v2, ..., vN]" (comma AND space delimited and square
    * bracketed).
    * <br>For flexibility, the brackets are automatically removed. The
    * delimiters may be optionally altered by use of the testPattern
    * and replaceString arguments.
    * 
    * @param workItem
    * @param attribute
    * @param testPattern - If not null or empty, a test pattern for
    *        transforming delimiter(s). Its used as a regular
    *        expression, so remember to escape special characters
    *        (ex. "\\[", "\\]", also with {} () . $ ^ * + ? \
    *        etc )
    * @param replaceString - When testPattern used, what to transform
    *        any delimiters to.
    * @return - a string, null, empty, or populated with values, with
    *           delimiters replaced per the additional arguments.
    * @throws TeamRepositoryException
    */
   public static String convertStringListToString( IWorkItem workItem,
         IAttribute attribute, String testPattern, String replaceString )
         throws TeamRepositoryException
   {
      String convertedValue = workItem.getValue( attribute ).toString();
      if (convertedValue == null || convertedValue.length() == 0)
         return convertedValue;
      // Remove optional brackets. Leaving just
      // "v1, v2, ..., vN"
      convertedValue = convertedValue
            .replaceAll( CommonConstants.SQUARE_BRACKET_LEFT, "" );
      convertedValue = convertedValue
            .replaceAll( CommonConstants.SQUARE_BRACKET_RIGHT, "" );
      if (testPattern != null && testPattern.length() > 0
            && replaceString != null)
      {
         // Caller requested transformation of content, oblige.
         convertedValue = convertedValue.replaceAll( testPattern,
               replaceString );
      }
      return convertedValue;
   }

   public static void setIntegerAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (value != null && !value.equals( "" ))
      {
         Integer setValue = Integer.valueOf( value );
         workItem.setValue( attribute, setValue );
      }
      else
      {
         String attributeId = attribute.getIdentifier();
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         String message = errorPrefix + "attribute \"" + attributeId
               + "\" has no integer value, " + "attribute value not set";
         Log log = LogFactory.getLog( attributeId );
         log.error( message );

      }
   }

   public static void setMediumHtmlAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value )
         throws TeamRepositoryException
   {
      String setValue = (value == null) ? "" : value;
      workItem.setValue( attribute, setValue );
   }

   public static void setLargeHtmlAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value )
         throws TeamRepositoryException
   {
      String setValue = null;
      if (value == null)
         setValue = "";
      else
      {
         String attributeId = attribute.getIdentifier();
         Log log = LogFactory.getLog( attributeId );
         setValue = StringUtils.truncateLargeString( value );
         try
         {
            if (setValue.getBytes( "UTF-8" ).length < value
                  .getBytes( "UTF-8" ).length)
               log.warn( "truncation executed for workitem "
                     + workItem.getId() + " with attributeId = " + attributeId
                     + " value has len = " + value.getBytes( "UTF-8" ).length
                     + " setValue has len = "
                     + setValue.getBytes( "UTF-8" ).length );
         }
         catch (UnsupportedEncodingException e)
         {
            setValue = "";
         }
      }
      workItem.setValue( attribute, setValue );
   }

   public static void setSmallStringAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value )
         throws TeamRepositoryException
   {
      String setValue = (value == null) ? "" : value;
      workItem.setValue( attribute, setValue );
   }

   public static void setCategoryAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (value == null)
      {
         workItem.setValue( attribute, value );
      }
      else
      {
         ICategoryHandle category = findCategory( value, sportCommonData );
         workItem.setCategory( category );
      }
   }

   public static void setDeliverableAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      if (value == null)
      {
         workItem.setValue( attribute, value );
      }
      else
      {
         IDeliverable deliverable = findDeliverable( value, sportCommonData );
         workItem.setValue( attribute, deliverable );
      }
   }

   public static void setTimestampAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      try
      {
         if ((value == null) || (value.equals( "" )))
            workItem.setValue( attribute, null );
         else
         {
            SimpleDateFormat df = new SimpleDateFormat( "yyyy/MM/dd HH:mm" ); // APAR
            // date
            // time
            // format
            Date date = df.parse( value );
            Date validDate = df.parse( "1900/01/01 00:00" );
            if (date.before( validDate ))
               df = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" ); // PMR
            // lastAlterTime
            // format

            Timestamp timestamp = new Timestamp(
                  df.parse( value ).getTime() );
            workItem.setValue( attribute, timestamp );
         }
      }
      catch (ParseException e)
      {
         String attributeId = attribute.getIdentifier();
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         String message = errorPrefix + "\"" + value + "\" is not a valid \""
               + attributeId + "\" timestamp value, attribute value not set";
         Log log = LogFactory.getLog( attributeId );
         log.error( message );
      }
   }

   // _B68102A
   /**
    * Set the value of a Wiki attribute type. Truncate and log if over 32K.
    * (Modeled after large HTML.)
    * 
    * @param workItem
    * @param attribute
    * @param value
    * @throws TeamRepositoryException
    */
   public static void setWikiAttributeValue( IWorkItem workItem,
         IAttribute attribute, String value )
         throws TeamRepositoryException
   {
      String setValue = null;
      if (value == null)
         setValue = "";
      else
      {
         String attributeId = attribute.getIdentifier();
         Log log = LogFactory.getLog( attributeId );
         setValue = StringUtils.truncateLargeString( value );
         if (setValue.length() < value.length())
            log.warn( "truncation executed for workitem " + workItem.getId()
                  + " with attributeId = " + attributeId + " value has len = "
                  + value.length() + " setValue has len = "
                  + setValue.length() );
      }
      workItem.setValue( attribute, setValue );
   }

   // Although IWorkItem.setState2 is deprecated, it is OK for us to use it.
   // See RTC Enhancement 157893 at
   // https://jazz.net/jazz/web/projects/Rational%20Team%20Concert#action=com.ibm.team.workitem.viewWorkItem&id=157893
   // for more details.
   @SuppressWarnings("deprecation")
   public static void setWorkflowState( IWorkItem workItem,
         String workflowState, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      Identifier<IState> workflowStateId = findWorkflowStateId( workItem,
            workflowState, sportCommonData );
      if (workflowStateId == null)
      {
         String workItemType = workItem.getWorkItemType();
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         String message = errorPrefix + "\"" + workflowState
               + "\" is not a valid workflow state, workflow state not set";
         Log log = LogFactory.getLog( workItemType );
         log.error( message );
         return;
      }
      workItem.setState2( workflowStateId );
   }

   // _T1083A Centralized spot to set work item customized attributes.
   // (Built-in attributes seem to have their own built-in access methods.
   // They will produce a null attribute on search.)
   // Attempts to reuse working copy if obtained, else sets source workitem.
   public static void setWorkItemAttributeToValue( String targetAttributeId,
         String toValue, IWorkItem sourceWorkItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      setWorkItemAttributeToValue( targetAttributeId, toValue, sourceWorkItem,
            sportCommonData, RtcUtils.SEARCH_ON_FIRST_STRING_ONLY_DEFAULT );
   }

   public static void setWorkItemAttributeToValue( String targetAttributeId,
         String toValue, IWorkItem sourceWorkItem,
         SportCommonData sportCommonData, boolean searchOnFirstStringOnly )
         throws TeamRepositoryException
   {
      // Get update-able work item or source work item
      // (Do not want to create a working copy if not already created)
      IWorkItem updateWorkItem = sportCommonData.getWorkItemForUpdate();
      // If working copy not obtained, use initial work item
      updateWorkItem = (updateWorkItem == null ? sourceWorkItem
            : updateWorkItem);
      // Get target attribute
      IAttribute targetAttr = null;
      if (sourceWorkItem != null)
      {
         targetAttr = RtcUtils.findAttribute( sourceWorkItem,
               targetAttributeId, sportCommonData );
      }
      // Check for customized attribute, and not throw an exception
      if (targetAttr != null)
      {
         // Set attribute value
         RtcUtils.setAttributeValue( updateWorkItem, targetAttr, toValue,
               sportCommonData, searchOnFirstStringOnly );
      }
   }

   /**
    * Indicates whether the work item has the attribute value indicating the
    * work item save operation was initiated by SBS.
    * 
    * @param workItem the {@link IWorkItem} object being saved.
    * 
    * @param sportCommonData the {@link SportCommonData} object caching the
    *        objects used in the save operation.
    * 
    * @return a <code>boolean</code> value indicating:
    *         <ul>
    *         <li><code>true</code> if the work item has the attribute value
    *         indicating the work item save operation was initiated by SBS;
    *         <li><code>true</code> if the work item <b>DOES NOT</b> have the
    *         attribute value indicating the work item save operation was
    *         initiated by SBS.
    * 
    * @throws TeamRepositoryException if repository access fails
    */
   public static final boolean isSbsSave( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      boolean isSbsSave = false;
      String workItemType = workItem.getWorkItemType();
      String sbsSaveAttributeId = CommonUtils
            .getSbsSavedAttributeId( workItemType );
      IAttribute sbsSaveAttribute = findAttribute( workItem,
            sbsSaveAttributeId, sportCommonData );

      if (sbsSaveAttribute != null)
      {
         isSbsSave = getAttributeValueAsBoolean( workItem, sbsSaveAttributeId,
               sportCommonData );
      }

      return isSbsSave;
   }

   /**
    * Find and return the project area with the specified name.
    * 
    * @param projectAreaName a <code>String</code> naming a project area.
    * 
    * @param sportCommonData the {@link SportCommonData} object caching the
    *        objects that provide access to RTC repository objects.
    * 
    * @return the {@link IProjectArea} object representing the project area
    *         named by <code>projectAreaName</code>, or <code>null</code> if
    *         the project area could not be found or does not exist.
    * 
    * @throws TeamRepositoryException if a fatal error occurs accessing the
    *         repository.
    */
   public static IProjectArea findProjectArea( String projectAreaName,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      ItemProfile<IProjectArea> profile = ItemProfile
            .createFullProfile( IProjectArea.ITEM_TYPE );
      List<IProjectArea> projecAreas = auditableCommon
            .findAuditables( profile, sportCommonData.getMonitor() );

      for (IProjectArea nextProjectArea : projecAreas)
      {
         if (nextProjectArea.getName().equals( projectAreaName ))
         {
            return nextProjectArea;
         }
      }

      return null;
   }

   /**
    * Return the project area object for the specified project area handle.
    * 
    * @param projAreaHandle an {@link IProjectAreaHandle} object for the
    *        project area being queried.
    * 
    * @param sportCommonData the {@link SportCommonData} object containing the
    *        repository accessor objects
    * 
    * @return the {@link IProjectArea} object describing the project area for
    *         the project area handle
    * 
    * @throws TeamRepositoryException if an error occurs accessing the
    *         repository
    */
   public static IProjectArea getProjectArea(
         IProjectAreaHandle projAreaHandle, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      ItemProfile<IProjectArea> profile = ItemProfile
            .createFullProfile( IProjectArea.ITEM_TYPE );
      IProjectArea projArea = auditableCommon.resolveAuditable(
            projAreaHandle, profile, sportCommonData.getMonitor() );

      return projArea;
   }

   /**
    * Find and return the category handle for the specified category name
    * path.
    * 
    * @param categoryNamePath a <code>String</code> specifying a hierarchy of
    *        category names delimited by
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static ICategoryHandle findCategory( String categoryNamePath,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      List<String> categoryNamePathElements = CategoryNamePath
            .parseIntoList( categoryNamePath );
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();
      ICategoryHandle categoryHandle = workItemCommon.findCategoryByNamePath(
            sportCommonData.getProjectArea(), categoryNamePathElements,
            sportCommonData.getMonitor() );

      return categoryHandle;
   }

   /**
    * Find and return the category handle for the specified category name
    * path.
    * 
    * @param deliverableName a <code>String</code> specifying a name of the
    *        deliverable
    * @param sportCommonData
    * @return
    * @throws TeamRepositoryException
    */
   public static IDeliverable findDeliverable( String deliverableName,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IWorkItemCommon workItemCommon = sportCommonData.getWorkItemCommon();

      return workItemCommon.findDeliverableByName(
            sportCommonData.getProjectArea(), deliverableName,
            IDeliverable.FULL_PROFILE, sportCommonData.getMonitor() );
   }

   /**
    * Return the process configuration for the specified project area
    * configuration data point.
    * 
    * @param configurationDataPoint a <code>String</code> identifying the
    *        configuration point identifying the desired process configuration
    *        data.
    * @param sportCommonData a {@link SportCommonData} object populated with
    *        an IAuditableCommon object for retrieving the process
    *        configuration data.
    * @return an {@link IProcessConfigurationData} object containing the
    *         desired process configuration data or <code>null</code>.
    * @throws TeamRepositoryException
    */
   public static IProcessConfigurationData getProcessConfigurationData(
         String configurationDataPoint, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAuditableCommon auditableCommon = sportCommonData.getAuditableCommon();
      IProjectAreaHandle projAreaHandle = sportCommonData.getProjectArea();

      return getProcessConfigurationData( projAreaHandle, auditableCommon,
            configurationDataPoint );
   }

   public static IProcessConfigurationData getProcessConfigurationData(
         IProjectAreaHandle projAreaHandle, IAuditableCommon auditableCommon,
         String configurationDataPoint )
         throws TeamRepositoryException
   {
      IProcessConfigurationData processCfgData = null;
      IAuditableCommonProcess auditableCommonProcess = auditableCommon
            .getProcess( projAreaHandle, null );

      if (auditableCommonProcess != null)
      {
         processCfgData = auditableCommonProcess
               .findProcessConfiguration( configurationDataPoint, null );
      }

      return processCfgData;
   }

   /**
    * Return the process configuration for the specified project area
    * configuration data point as an XML string.
    * 
    * @param configurationDataPoint a <code>String</code> identifying the
    *        configuration point identifying the desired process configuration
    *        data.
    * @param sportCommonData a {@link SportCommonData} object populated with
    *        an IAuditableCommon object for retrieving the process
    *        configuration data.
    * @return a <code>String</code> object containing the desired process
    *         configuration data or <code>null</code>.
    * @throws TeamRepositoryException
    */
   public static final String getProcessConfigurationDataAsString(
         String configurationDataPoint, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      StringBuilder buffer = new StringBuilder();
      IProcessConfigurationData cfgData = getProcessConfigurationData(
            configurationDataPoint, sportCommonData );

      if (cfgData != null)
      {
         IProcessConfigurationElement[] elements = cfgData.getElements();

         for (IProcessConfigurationElement element : elements)
         {
            appendProcessConfigurationElement( element, buffer, "" );
         }
      }

      return buffer.toString();
   }

   private static void appendProcessConfigurationElement(
         IProcessConfigurationElement element, StringBuilder buffer,
         String indent )
   {
      IProcessConfigurationElement[] children = element.getChildren();

      if (children != null)
      {
         for (IProcessConfigurationElement child : children)
         {
            appendProcessConfigurationElement( child, buffer,
                  indent + "   " );
         }
      }

      buffer.append( "<" ).append( element.getName() );
      String[] attrNames = element.getAttributeNames();
      String charData = element.getCharacterData();

      for (String attrName : attrNames)
      {
         String attrValue = element.getAttribute( attrName );
         buffer.append( " " ).append( attrName ).append( "=\"" );
         buffer.append( attrValue ).append( "\"" );
      }

      if (charData != null)
      {
         buffer.append( ">\n" );
         buffer.append( indent ).append( "  " );
         buffer.append( charData ).append( "\n" );
         buffer.append( indent ).append( "<" ).append( element.getName() );
      }

      buffer.append( "/>\n" );
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Return the process configuration for the specified project area
    * configuration data point.
    * 
    * @param configData an <code>IProcessConfigurationData</code> object
    *        identifying the presumed process configuration containing the
    *        element
    * @param findElementName a <code>String</code> identifying the desired
    *        process configuration data sub element.
    * @param matchAttributes a
    *        <code>HashMap&ltattribute-name, attribute-value&gt</code>
    *        identifying any attributes that should be applied to a more
    *        granular match. Null / empty map, or null / empty attributes
    *        ignores the granularity.
    * @param sportCommonData a {@link SportCommonData} object populated with
    *        an IAuditableCommon object for retrieving the process
    *        configuration data.
    * @return an {@link IProcessConfigurationElement} object containing the
    *         desired process configuration element or <code>null</code>.
    */
   public static IProcessConfigurationElement findProcessConfigurationElement(
         IProcessConfigurationData configData, String findElementName,
         HashMap<String, String> matchAttributes,
         SportCommonData sportCommonData )
   {
      if (configData != null && findElementName != null
            && findElementName.trim().length() > 0)
      {
         IProcessConfigurationElement[] configChildren = configData
               .getElements();
         if (configChildren != null)
         {
            for (IProcessConfigurationElement childElement : configChildren)
            {
               if (attributeMatchProcessConfigurationElement( childElement,
                     findElementName, matchAttributes,
                     sportCommonData ) != null)
               {
                  // Found it, so return the matching element
                  return childElement;
               }
            }
         }
      }
      // Not found -
      return null;
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Return the process configuration for the specified project area
    * configuration element.
    * 
    * @param parentElement an <code>IProcessConfigurationElement</code> object
    *        identifying the presumed parent configuration element
    * @param findElementName a <code>String</code> identifying the desired
    *        process configuration data sub element.
    * @param matchAttributes a
    *        <code>HashMap&ltattribute-name, attribute-value&gt</code>
    *        identifying any attributes that should be applied to a more
    *        granular match. Null / empty map, or null / empty attributes
    *        ignores the granularity.
    * @param sportCommonData a {@link SportCommonData} object populated with
    *        an IAuditableCommon object for retrieving the process
    *        configuration data.
    * @return an {@link IProcessConfigurationElement} object containing the
    *         desired process configuration element or <code>null</code>.
    */
   public static IProcessConfigurationElement findProcessConfigurationElement(
         IProcessConfigurationElement parentElement, String findElementName,
         HashMap<String, String> matchAttributes,
         SportCommonData sportCommonData )
   {
      if (parentElement != null && findElementName != null
            && findElementName.trim().length() > 0)
      {
         IProcessConfigurationElement[] childElements = parentElement
               .getChildren();
         if (childElements != null)
         {
            for (IProcessConfigurationElement childElement : childElements)
            {
               if (attributeMatchProcessConfigurationElement( childElement,
                     findElementName, matchAttributes,
                     sportCommonData ) != null)
               {
                  // Found it, so return the matching element
                  return childElement;
               }
            }
         }
      }
      // Not found -
      return null;
   }

   // _F155269A FIXCAT, Preventative Service Program (PSP)
   // PSP Keywords / APAR Close
   /**
    * Determine if a) the current process configuration element matches the
    * search argument b) if attribute granularity is to be applied (non-null
    * arguments), match all of the attributes supplied. Return the process
    * configuration for the specified project area configuration element on a
    * match, otherwise null.
    * 
    * @param configElement an <code>IProcessConfigurationElement</code> object
    *        identifying a configuration element
    * @param findElementName a <code>String</code> identifying the desired
    *        process configuration data element.
    * @param matchAttributes a
    *        <code>HashMap&ltattribute-name, attribute-value&gt</code>
    *        identifying any attributes that should be applied to a more
    *        granular match. Null / empty map, or null / empty attributes
    *        ignores the granularity.
    * @param sportCommonData a {@link SportCommonData} object populated with
    *        an IAuditableCommon object for retrieving the process
    *        configuration data.
    * @return an {@link IProcessConfigurationElement} object containing the
    *         process configuration element on a match or <code>null</code>.
    */
   public static IProcessConfigurationElement attributeMatchProcessConfigurationElement(
         IProcessConfigurationElement configElement, String findElementName,
         HashMap<String, String> matchAttributes,
         SportCommonData sportCommonData )
   {
      if (configElement != null && findElementName != null
            && findElementName.trim().length() > 0)
      {
         if (configElement.getName().equals( findElementName.trim() ))
         {
            // In this test, exit with a null result when there is not
            // a match on any "valid" attribute. Otherwise, return a
            // match.
            if (matchAttributes != null && !(matchAttributes.isEmpty()))
            {
               // In this loop, exit with a null result when there is not
               // a match on any attribute. Otherwise, return a match.
               for (String findAttributeName : matchAttributes.keySet())
               {
                  // Key / Attribute should have a value.
                  // Not sure about attribute value yet.
                  String findAttributeValue = matchAttributes
                        .get( findAttributeName );
                  // if (findAttributeName == null
                  // || findAttributeName.length() == 0
                  if (findAttributeValue != null
                        && findAttributeValue.trim().length() > 0)
                  {
                     // Valid attribute with value
                     // Attribute-specific match?
                     String configAttrValue = configElement
                           .getAttribute( findAttributeName );
                     if (configAttrValue == null
                           || configAttrValue.trim().length() == 0
                           || !(configAttrValue.trim()
                                 .equals( findAttributeValue.trim() )))
                     {
                        // Element either does not contain attribute
                        // or it did not match, return null
                        return null;
                     }
                  }
               }
            }
            // Found the main element -
            // No attribute granularity failure, so return the
            // matching element
            return configElement;
         }
      }
      // Not found -
      return null;
   }

   /**
    * Return the link types for the specified source and target work item
    * types.
    * 
    * @return an array of {@link ILinkType} objects.
    */
   public static final ILinkType[] getWorkItemToWorkItemLinkTypes()
   {
      List<IEndPointDescriptor> allEndpointDescriptors = WorkItemLinkTypes
            .getUserWritableDescriptors();
      List<ILinkType> linkTypes = new ArrayList<ILinkType>(
            allEndpointDescriptors.size() );

      for (IEndPointDescriptor descriptor : allEndpointDescriptors)
      {
         ILinkType linkType = descriptor.getLinkType();

         if (isWorkItemTypeDescriptor(
               linkType.getSourceEndPointDescriptor() )
               && isWorkItemTypeDescriptor(
                     linkType.getTargetEndPointDescriptor() ))
         {
            linkTypes.add( linkType );
         }
      }

      return linkTypes.toArray( new ILinkType[linkTypes.size()] );
   }

   private static boolean isWorkItemTypeDescriptor(
         IEndPointDescriptor descriptor )
   {
      boolean isValid = false;

      if (!descriptor.isItemReference())
      {
         isValid = true;
      }
      else
      {
         IItemType itemType = descriptor.getReferencedItemType();
         isValid = itemType == IWorkItem.ITEM_TYPE;
      }

      return isValid;
   }

   @SuppressWarnings("unchecked")
   private static Identifier<? extends ILiteral> unsafeCastToIdentifierExtendsILiteral(
         Object obj )
   {
      return (Identifier<? extends ILiteral>)obj;
   }

   // _T155539A Initially needed for conversion of artifact
   // pmrOpenDate field.
   /**
    * This function was copied from the RTC action performer, as used from a
    * common full-function general purpose toRTCValue method which tests an
    * attribute type, and performs attribute-type specific artifact field
    * value conversion. This was the first, and only conversion needed and
    * will be tested. Any other type conversions can be copied (and modified,
    * some extensively) if / when ever needed.
    * 
    * @param absTimeString
    * @param sportCommonData
    * @return // @throws TeamRepositoryException
    */
   public static Timestamp toTimestamp( String absTimeString,
         SportCommonData sportCommonData )
   // throws TeamRepositoryException
   {
      // RTC timestamp does not have seconds, so we have to tell
      // the utlility used what format RTC uses.
      // The default previously used assumed :ss at the end.
      // Also need to handle empty value.
      Timestamp returnTS = null;
      Log sportLog = sportCommonData.getLog();
      if ((absTimeString != null) && (absTimeString.length() > 0))
      {
         try
         {
            returnTS = new Timestamp( DateTimeString
                  .fromString( absTimeString, null,
                        CommonConstants.RTC_TIMESTAMP_FORMAT_PATTERN )
                  .getTime() );
         }
         catch (Exception e)
         {
            if (sportLog != null)
               sportLog.warn( "absTimeString " + absTimeString
                     + " was not well formatted" + e.getMessage() );
            /*
             * Log.defLogWarning( "absTimeString " + absTimeString +
             * " was not well formatted" + e.getMessage() );
             */
         }
      }
      return (returnTS);
   }

   /**
    * _B233387A
    * Default (RTC) method, w/o any other arguments, to scrub a default
    * set of undesirable characters from a work item attribute string
    * value. The idea is, as the known set of bad values grows, just
    * add new String replace calls here.
    * 
    * @param sourceValue
    * @return
    */
   public static String scrubChars( String sourceValue )
   {
      String resultValue = sourceValue;
      // Avoid an exception, or extraneous handling.
      if (resultValue == null)
         return resultValue;

      // Initially need to turn &nbsp values to blanks
      resultValue = resultValue.replace( Constants.NON_BREAK_SPACE_CHAR,
            Constants.SPACE_CHAR );
      // Add other calls when needed.
      /*
       * ex.
       * resultValue = resultValue.replace( sourceChar, convertChar );
       */

      return resultValue;
   }

   // _F234119M
   /**
    * Given a work item and 2 attribute names, and the value of the
    * 2nd attribute already obtained (for reuse), return a string
    * comprised of only unique blank-separated values. In essence,
    * merge the 2 attribute values.
    * When the 1st attribute is a stringList type, the result
    * delimiter will be a combined comma+space.
    * If a single-value is specified, ensure it only appears by itself
    * or gets removed from mixed values.
    * 
    * @param workItem
    * @param wiAttr1Name - value to be derived from work item.
    * @param attr2Name
    * @param attr2Value - parameterized value already obtained for 2nd
    *                     attribute.
    * @param optionalSingleValue - if not null, ensure only 1
    *                              occurrence results, or remove if
    *                              mixed values specified.
    * @param sportCommonData
    * @return merged blank-separated values. (StringList type attribute
    *         gets comma+space delimiter.)
    * @throws TeamRepositoryException 
    */
   public static String getUniqueValues( IWorkItem workItem,
         String wiAttr1Name, String attr2Name, String attr2Value,
         String optionalSingleValue, SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      // Get each attribute value from the work item.
      IAttribute wiAttr1 = RtcUtils.findAttribute( workItem, wiAttr1Name,
            sportCommonData );
      /*
      IAttribute attr2 = RtcUtils.findAttribute( workItem, attr2Name,
            sportCommonData );
      */
      
      String wiAttr1Value = RtcUtils.getAttributeValueAsString( workItem,
            wiAttr1, sportCommonData );

      // Merge the values -
      if (wiAttr1Value == null)
         wiAttr1Value = "";
      String localAttr2Value = attr2Value;
      if (localAttr2Value == null)
         localAttr2Value = "";
      // _T269754C
      // The tokenizer handles whitespace delimited values. We could
      // either get fancy and modify this to recognize a stringList
      // comma+space (combined) delimiter, or just modify the input.
      // Lets go with the latter.
      // _T269754C Initially reconstitute merged values with space
      // delimiter.
      String resultDelimiter = " ";
      if (wiAttr1.getAttributeType().equals( "stringList" ))
      {
         // StringList data delimiter will be comma+space. Convert
         // to form recognized by the tokenizer used (space only).
         // Also remove any optional brackets.
         // This will be done for BOTH attributes' data.
         wiAttr1Value = wiAttr1Value
               .replaceAll( CommonConstants.SQUARE_BRACKET_LEFT, "" );
         wiAttr1Value = wiAttr1Value
               .replaceAll( CommonConstants.SQUARE_BRACKET_RIGHT, "" );
         wiAttr1Value = wiAttr1Value.replaceAll(
               CommonConstants.COMMA_AND_SPACE_SPLIT_PATTERN, " " );
         localAttr2Value = localAttr2Value
               .replaceAll( CommonConstants.SQUARE_BRACKET_LEFT, "" );
         localAttr2Value = localAttr2Value
               .replaceAll( CommonConstants.SQUARE_BRACKET_RIGHT, "" );
         localAttr2Value = localAttr2Value.replaceAll(
               CommonConstants.COMMA_AND_SPACE_SPLIT_PATTERN, " " );
         // Now these should be in proper form for tokenizing.
         // StringList will need this delimiter -
         resultDelimiter = ", ";
      }
      StringTokenizer st = new StringTokenizer( wiAttr1Value + " "
            + localAttr2Value );
      // Using a HashSet is how we do not include duplicate values.
      HashSet<String> mergedValues = new HashSet<String>();
      boolean containsSingleValue = false;
      while (st.hasMoreTokens())
      {
         String nextToken = st.nextToken().trim();
         if (optionalSingleValue != null
               && optionalSingleValue.trim().length() > 0)
         {
            // Omit the optional single occurrence for now -
            if (nextToken.equals( optionalSingleValue ))
               containsSingleValue = true;
            else
               // Will not add duplicate values.
               mergedValues.add( nextToken );
         }
         else
            // Add any value. Will not add duplicate values.
            mergedValues.add( nextToken );
      }

      // Collect the unique values.
      StringBuilder sb = new StringBuilder();
      // _T269754A Initially empty for 1st entry.
      String returnBufferDelimiter = "";
      for (String mergedValue : mergedValues)
      {
         // _T269754C
         sb.append( returnBufferDelimiter + mergedValue );
         // Further entries -
         returnBufferDelimiter = resultDelimiter;
      }

      // If the value contains more than just the optional single
      // value, but also contains the optional single value, it
      // will have been omitted. If it is the only value, reset it.
      if (containsSingleValue)
      {
         // _T269754C Include any delimiter if not only value.
         if (sb.toString().trim().length() <= 0)
            sb.append( returnBufferDelimiter + optionalSingleValue.trim() );
      }

      return (sb.toString().trim());

   }

   // _T276100A Check use of deprecated attribute(s).
   /**
    * Given the list of RTC actions in play, and a predefined list of
    * known deprecated attributes in a process template, look for an
    * entry that indicates an attempt to modify a known deprecated
    * attribute.
    * "modify/[attr-id]"
    * If detected, throw a SPoRT exception!
    * 
    * @param workItem
    * @param resolvedActions
    * @param sportCommonData
    * @throws TeamRepositoryException
    * @throws SportRuntimeException
    */
   public static void illegalUseOfDeprecatedAttribute( IWorkItem workItem,
         String[] resolvedActions, SportCommonData sportCommonData )
         throws TeamRepositoryException, SportRuntimeException
   {
      ArrayList<String> flaggedAttrs = new ArrayList<String>();  
      for (String action : resolvedActions)
      {
         String[] actionParts = action.split( "/" );
         if ((actionParts.length == 2) && actionParts[0].equals( "modify" ))
         {
            String attrID = actionParts[1];
            if (KNOWN_DEPRECATED_ATTRIBUTE_IDS.contains( attrID ))
            {
               // Encountered a deprecated attribute, bad! Take names.
               flaggedAttrs.add( attrID );
            }
         }
      }
      if (!flaggedAttrs.isEmpty())
      {
         // Encountered deprecated attribute(s), bad!
         // Report them all. Throw the flag!
         String errorPrefix = getWorkItemErrorPrefix( workItem,
               sportCommonData );
         // When called from an operation advisor,
         // SportUserActionFailException exception type gets some
         // additional handling that it turns out we dont want.
         // It appends -
         // Please correct the problem. If the problem persists,
         // contact your SPoRT Administrator for assistance.
         // This exception type looks a little nastier in the UI, and
         // conveys a little more urgency to the user to do something
         // to fix it.
         throw new SportRuntimeException(
               errorPrefix + "work item type \"" + workItem.getWorkItemType()
                     + "\". "
                     + Constants.NEWLINE
                     + "An attempt was made to update deprecated attribute(s): "
                     + flaggedAttrs.toString() + "."
                     + Constants.NEWLINE
                     + "The underlying application (e.g. zBuild) will need to be upgraded to be compatible with the latest SPoRT features."
                     );
      }
   }

}