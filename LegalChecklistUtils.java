package com.ibm.sport.rtc.common;

import com.ibm.team.process.common.IProcessConfigurationElement;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IWorkItem;

public class LegalChecklistUtils
{
   public static void checkQ1( IWorkItem legalChecklist,
         boolean[] enabledQuestions, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (enabledQuestions[0])
      {
         String q1Value = RtcUtils.getAttributeValueAsString( legalChecklist,
               LegalChecklistConstants.Q1_ATTRIBUTE_ID, sportCommonData );
         String q1aValue = RtcUtils.getAttributeValueAsString(
               legalChecklist, LegalChecklistConstants.Q1A_ATTRIBUTE_ID,
               sportCommonData );
         if (q1Value.equals( LegalChecklistConstants.YES ))
         {
            if (!q1aValue.equals( LegalChecklistConstants.YES ))
            {
               throw new SportUserActionFailException(
                     "Since the answer to Question 1 is Yes, then the answer "
                           + "to Question 1a must also be Yes." );
            }
         }
      }
   }

   public static void checkQ2( IWorkItem legalChecklist,
         boolean[] enabledQuestions, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (enabledQuestions[1])
      {
         String q2Value = RtcUtils.getAttributeValueAsString( legalChecklist,
               LegalChecklistConstants.Q2_ATTRIBUTE_ID, sportCommonData );
         String q2aValue = RtcUtils.getAttributeValueAsString(
               legalChecklist, LegalChecklistConstants.Q2A_ATTRIBUTE_ID,
               sportCommonData );
         String q2bValue = RtcUtils.getAttributeValueAsString(
               legalChecklist, LegalChecklistConstants.Q2B_ATTRIBUTE_ID,
               sportCommonData );
         if (q2Value.equals( LegalChecklistConstants.YES ))
         {
            if ((!q2aValue.equals( LegalChecklistConstants.YES ))
                  || (!q2bValue.equals( LegalChecklistConstants.YES )))
            {
               throw new SportUserActionFailException(
                     "Since the answer to Question 2 is Yes, then the answers "
                           + "to Questions 2a and 2b must also be Yes." );
            }
         }
      }
   }

   public static void checkQ3( IWorkItem legalChecklist,
         boolean[] enabledQuestions, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (enabledQuestions[2])
      {
         String q3Value = RtcUtils.getAttributeValueAsString( legalChecklist,
               LegalChecklistConstants.Q3_ATTRIBUTE_ID, sportCommonData );
         String q3aValue = RtcUtils.getAttributeValueAsString(
               legalChecklist, LegalChecklistConstants.Q3A_ATTRIBUTE_ID,
               sportCommonData );
         if (q3Value.equals( LegalChecklistConstants.YES ))
         {
            if (!q3aValue.equals( LegalChecklistConstants.YES ))
            {
               throw new SportUserActionFailException(
                     "Since the answer to Question 3 is Yes, then the answer "
                           + "to Question 3a must also be Yes." );
            }
         }
      }
   }

   public static void checkQ6( IWorkItem legalChecklist,
         boolean[] enabledQuestions, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (enabledQuestions[5])
      {
         String q6Value = RtcUtils.getAttributeValueAsString( legalChecklist,
               LegalChecklistConstants.Q6_ATTRIBUTE_ID, sportCommonData );
         if (!q6Value.equals( LegalChecklistConstants.YES ))
         {
            throw new SportUserActionFailException(
                  "The answer to Question 6 must be Yes." );
         }
      }
   }

   public static void checkQ7( IWorkItem legalChecklist,
         boolean[] enabledQuestions, SportCommonData sportCommonData )
         throws SportUserActionFailException, TeamRepositoryException
   {
      if (enabledQuestions[6])
      {
         String q7Value = RtcUtils.getAttributeValueAsString( legalChecklist,
               LegalChecklistConstants.Q7_ATTRIBUTE_ID, sportCommonData );
         if (!q7Value.equals( LegalChecklistConstants.NO ))
         {
            throw new SportUserActionFailException(
                  "The answer to Question 7 must be No." );
         }
      }
   }

   public static boolean containsLegalChecklistAnswerAction(
         String[] actions, String workItemType )
   {
      // Initially not a create action
      boolean containsAnswerAction = false;

      // Determine if Legal Checklist create (possibly from SBS)
      if (workItemType.equals( LegalChecklistConstants.WORK_ITEM_TYPE_ID ))
      {
         // Now know its a legal checklist
         for (String action : actions)
         {
            String[] actionParts = action.split( "/" );
            String actionType = actionParts[0];
            if ((actionParts.length == 2) && actionType.equals( "action" ))
            {
               String actionName = actionParts[1];
               if (actionName
                     .equals( LegalChecklistConstants.RTC_ANSWER_ACTION ))
               {
                  // Processing a legal checklist answer
                  containsAnswerAction = true;
                  break;
               }
            }
         }
      }
      return containsAnswerAction;
   }

   public static boolean[] getEnabledQuestions(
         IProcessConfigurationElement configurationElement )
   {
      for (IProcessConfigurationElement child : configurationElement
            .getChildren())
      {
         if (child
               .getName()
               .equals(
                     LegalChecklistConstants.ENABLED_QUESTIONS_CONFIGURATION_ELEMENT_NAME ))
         {
            boolean[] enabledQuestions = new boolean[LegalChecklistConstants.NUMBER_OF_TOP_LEVEL_QUESTIONS];
            for (int i = 0; i < enabledQuestions.length; ++i)
               enabledQuestions[i] = false;
            String[] enabledQuestionStrings = child
                  .getAttribute(
                        LegalChecklistConstants.ENABLED_QUESTIONS_CONFIGURATION_ATTRIBUTE_NAME )
                  .split( " " );
            for (String enabledQuestionString : enabledQuestionStrings)
            {
               enabledQuestions[Integer.parseInt( enabledQuestionString ) - 1] = true;
            }
            return enabledQuestions;
         }
      }
      throw new SportRuntimeException(
            "LegalChecklist enabled question configuration not found" );
   }
}