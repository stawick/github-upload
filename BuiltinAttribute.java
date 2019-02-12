/**
 * 
 */
package com.ibm.sport.rtc.common;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * The <code>BuiltinAttribute</code> class enumerates the built-in attribute
 * names and provides the ID for each.
 */
public enum BuiltinAttribute
{
   ARCHIVED("archived", "Archived"),
   FILED_AGAINST("category", "Filed Against"),
   RANK(
         "com.ibm.team.apt.attribute.planitem.priority._pm7NmRYUEd6L1tNIGdz5qQ",
         "Rank"), MAXIMAL_ESTIMATE("com.ibm.team.apt.estimate.maximal",
         "Maximal Estimate"), MINIMAL_ESTIMATE(
         "com.ibm.team.apt.estimate.minimal", "Minimal Estimate"),
   RESTRICTED_ACCESS("contextId", "Restricted Access"), CORRECTED_ESTIMATE(
         "correctedEstimate", "Corrected Estimate"), CREATION_DATE(
         "creationData", "Creation Date"),
   CREATED_BY("creator", "Created By"), CUSTOM_ATTRIBUTES("customAttributes",
         "Custom Attributes"), DESCRIPTION("description", "Description"),
   DUE_DATE("dueDate", "Due Date"), ESTIMATE("duration", "Estimate"),
   FOUND_IN("foundIn", "Found In"), ID("id", "Id"), APPROVAL_DESCRIPTORS(
         "internalApprovalDescriptors", "Approval Descriptors"), APPROVALS(
         "internalApprovals", "Approvals"), COMMENTS("internalComments",
         "Comments"), RESOLUTION("internalResolution", "Resolution"),
   SEQUENCE_VALUE("internalSequenceValue", "Sequence Value"), STATUS(
         "internalState", "Status"), STATE_TRANSITIONS(
         "internalStateTransitions", "State Transitions"), SUBSCRIBED_BY(
         "internalSubscriptions", "Subscribed By"), TAGS("internalTags",
         "Tags"), MODIFIED_DATE("modified", "Modified Date"), MODIFIED_BY(
         "modifiedBy", "Modified By"), OWNED_BY("owner", "Owned By"),
   PROJECT_AREA("projectArea", "Project Area"), RESOLUTION_DATE(
         "resolutionDate", "Resolution Date"), RESOLVED_BY("resolver",
         "Resolved By"), START_DATE("startDate", "Start Date"), SUMMARY(
         "summary", "Summary"), PLANNED_FOR("target", "Planned For"),
   TIME_SPENT("timeSpent", "Time Spent"), TYPE("workItemType", "Type"),
   PRIORITY("internalPriority", "Priority"), SEVERITY("internalSeverity",
         "Severity"), ACCEPTANCE_TEST(
         "com.ibm.team.apt.attribute.acceptance", "Acceptance Test"),
   AFFECTED_TEAMS("com.ibm.team.rtc.attribute.affectedTeams",
         "Affected Teams"), STORY_POINTS(
         "com.ibm.team.apt.attribute.complexity", "Story Points"), IMPACT(
         "impact", "Impact");

   private String attrId = null;
   private String attrName = null;

   private BuiltinAttribute( String attrId, String attrName )
   {
      this.attrId = attrId;
      this.attrName = attrName;
   }

   public String getAttributeId()
   {
      return this.attrId;
   }

   public String getAttributeName()
   {
      return this.attrName;
   }

   public String getAttributeValueAsString( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAttribute attr = sportCommonData.getWorkItemCommon().findAttribute(
            workItem.getProjectArea(), this.attrId,
            sportCommonData.getMonitor() );
      String attrVal = RtcUtils.getAttributeValueAsString( workItem,
            attr, sportCommonData );

      return attrVal;
   }

   public boolean hasAttributeValue( IWorkItem workItem,
         SportCommonData sportCommonData )
         throws TeamRepositoryException
   {
      IAttribute attr = sportCommonData.getWorkItemCommon().findAttribute(
            workItem.getProjectArea(), this.attrId,
            sportCommonData.getMonitor() );

      return workItem.hasBuiltInAttribute( attr );
   }
}
