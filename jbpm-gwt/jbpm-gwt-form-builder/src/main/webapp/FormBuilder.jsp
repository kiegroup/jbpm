<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<% 
  String context = request.getContextPath(); 
  String jsonBody = org.apache.commons.io.IOUtils.toString(request.getReader());
%>
<html>
  <head>
    <title>GWT Form Builder (test page)</title>
    <link rel="stylesheet" href="FormBuilder.css" title="Form Builder (test page)" type="text/css">
  </head>
  <body oncontextmenu="return false;">
    <!-- for google chart apis -->
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {"packages" : ["corechart"] });
      var clientExportForm = {};
    </script>
    <!-- END for google chart apis -->
    <script language="javascript" src="org.jbpm.formbuilder.FormBuilder/org.jbpm.formbuilder.FormBuilder.nocache.js"></script>
    <div id="formBuilder" style="visibility: collapse;">{
      "packageName": "defaultPackage"
    }</div>
  </body>
</html>
