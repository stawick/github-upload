dojo.provide("com.ibm.sport.rtc.common.SPoRTPtfVolumeIdValidatorScript");
dojo.require("com.ibm.team.workitem.api.common.WorkItemAttributes");

dojo.require("com.ibm.team.workitem.api.common.Severity");
dojo.require("com.ibm.team.workitem.api.common.Status");

(function() {

var Severity = com.ibm.team.workitem.api.common.Severity;
var Status = com.ibm.team.workitem.api.common.Status;

var PtfVolumeIdValidator = dojo.declare("com.ibm.sport.rtc.common.SPoRTPtfVolumeIdValidatorScript", null, {

    validate: function(attributeId, workItem, configuration) {
    
        // Get the configuration parameters about the severity and error message of this validator
        var severity= configuration.getChild("parameters").getStringDefault("severity", Severity.ERROR.name);
        var message= configuration.getChild("parameters").getStringDefault("message", "");
        
        // Get the draft close code attribute and the volume ID attribute from the configuration
        var closecodeId = "com.ibm.sport.rtc.workItem.attribute.ptf.draftPtfCloseCode";
        var closecode = workItem.getValue(closecodeId);
        
        // Get the current attribute&apos;s value
        var volumeId= workItem.getValue(attributeId);
        
        // For COR close codes, volume ID must be 0000 or 1000
        if (closecode == "com.ibm.sport.rtc.workItem.enumeration.ptf.draftPtfCloseCode.literal.l4")
        {
          if ((volumeId == "0000") || (volumeId == "1000"))
          {
            return Status.OK_STATUS;
          }
          else
          {
            return new Status(Severity[severity], message);
          }
        }
        else if (closecode == "com.ibm.sport.rtc.workItem.enumeration.ptf.draftPtfCloseCode.literal.l6")
        {
          if ((volumeId == "0000") || (volumeId == "1000"))
          {
            return new Status(Severity[severity], message);
          }
          else
          { 
            if (volumeId.length != 4)
            {
              return new Status(Severity[severity], "Volume ID must be 4 characters long.");
            }
            else
            {
              return Status.OK_STATUS;
            }
          }
        }
        else
        {
          return Status.OK_STATUS;
        }
        
        
    },
   
   __sentinel: null
});
})();