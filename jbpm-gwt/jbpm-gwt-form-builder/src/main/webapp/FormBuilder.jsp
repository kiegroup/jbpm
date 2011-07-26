<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>GWT Form Builder (test page)</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/FormBuilder.css" title="Form Builder (test page)" type="text/css">
  </head>
  <body oncontextmenu="return false;">
    <!-- for google chart apis -->
    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {"packages" : ["corechart"] });
    </script>
    <!-- END for google chart apis -->
    <script language="javascript" src="<%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/org.jbpm.formbuilder.FormBuilder.nocache.js"></script>
    <div id="formBuilder" style="visibility: collapse;"><%=(String) request.getAttribute("jsonData")%></div>
  </body>
</html>
