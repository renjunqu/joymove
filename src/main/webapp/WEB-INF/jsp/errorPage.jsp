<%--
  Created by IntelliJ IDEA.
  User: qurj
  Date: 15/5/13
  Time: 上午11:11
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page
        import="java.util.*,org.springframework.context.ApplicationContext"%>
<%@ page
        import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="exception" value="${pageContext.exception}"></c:set>

<html>
<head>
    <title></title>
</head>
<body>



<%
  Exception ex = (Exception)pageContext.getAttribute("exception");
  out.println("show exception: "+ex.toString());
  StackTraceElement[] eles = ex.getStackTrace();
  for(StackTraceElement e:eles) {
      out.println(e.toString());
      out.println("++++++++++++++++++++++");
  }

%>




</body>
</html>
