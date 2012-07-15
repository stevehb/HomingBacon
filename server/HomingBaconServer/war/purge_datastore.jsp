<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="com.google.appengine.api.datastore.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
<%
DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
Query q = new Query();
PreparedQuery pq = datastore.prepare(q);
for(Entity e : pq.asIterable()) {
    %>
    Deleting entity with key=<%= e.getKey() %><br />
    <%
    try {
      datastore.delete(e.getKey());
    } catch(Exception ex) {
        %>EXCEPTION=<%= ex.getLocalizedMessage() %><br />
        <%
    }
}
%>
</body>
</html>