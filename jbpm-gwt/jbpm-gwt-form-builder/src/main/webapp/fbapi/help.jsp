<html>
  <head>
    <title>FormBuilder REST API help page</title>
  </head>
  <body>
    <h1>FormBuilder REST API</h1>
    <ul>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/menuItems/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: explain listMenuItems()</li>
		  <li style="color:red;"><strong>POST</strong>: explain saveMenuItem(body)</li>
		  <li style="color:red;"><strong>DELETE</strong>: explain deleteMenuItem(uri)</li>
		</ul>
	  </li>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/formItems/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: build and explain listFormItems()</li>
		  <li style="color:red;"><strong>POST</strong>: explain saveFormItem(body)</li>
		  <li style="color:red;"><strong>DELETE</strong>: explain deleteFormItem(uri)</li>
		</ul>
	  </li>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/forms/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: build and explain listForms()</li>
		  <li style="color:red;"><strong>POST</strong>: explain saveForm(body)</li>
		  <li style="color:red;"><strong>DELETE</strong>: explain deleteForm(uri)</li>
		</ul>
	  </li>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/menuOptions/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: explain listOptions()</li>
		</ul>
	  </li>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/tasks/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: explain listTasks()</li>
		</ul>
	  </li>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/validations/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: build and explain listTasks()</li>
		</ul>
	  </li>
      <li>
	    <h4><%=request.getContextPath()%>/org.jbpm.formbuilder.FormBuilder/fbapi/formPreview/</h4>
		<ul>
		  <li style="color:red;"><strong>GET</strong>: build and explain formPreview()</li>
		</ul>
	  </li>
    </ul>
  </body>
</html>
