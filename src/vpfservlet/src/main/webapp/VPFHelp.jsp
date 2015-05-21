<%@ page session="false" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head><title>VPF Help</title></head>
<body>
<h1>VPF Help</h1>

This is help text.
<% String helpTopic = request.getParameter("topic"); %>
<% if (helpTopic == null) { %>
No help topic was specified.
<ul>
<li><A HREF="VPFHelp.jsp?topic=table_schema">Table Schema</A>
</ul>
<% } else if ("test".equals(helpTopic)) { %>
<ul>
<li>Current time: <%= new java.util.Date() %>
<li>Hostname: <%= request.getRemoteHost() %>
<li>Param file: <%= request.getParameter("topic") %>
</ul>
<% } else if ("table_schema".equals(helpTopic)) { %>
<h2>Table Schema Help</h2>
This data is extracted from the VPF Standard Section 5. (MIL-STD-2407)
<ul>
<li><b>#</b>: The position of the column in the record
<li><b>Name</b>: The name of the column
<li><b>Type</b>: The field type of the column.  Valid values are
<table BORDER=1>
<tr><th>Type Code<th>Datatype Description</tr>
<tr><td>T<td>ASCII Text</tr>
<tr><td>L<td>ISO Latin-1 Text</tr>
<tr><td>M<td>ISO Full Latin Text</tr>
<tr><td>N<td>ISO Multilingual Text</tr>
<tr><td>S<td>2-byte integer</tr>
<tr><td>I<td>4-byte integer</tr>
<tr><td>F<td>4-byte floating point (float)</tr>
<tr><td>D<td>8-byte floating point (double)</tr>
<tr><td>C<td>Array of 2-coord (Lat,Lon) floats</tr>
<tr><td>B<td>Array of 2-coord (Lat,Lon) doubles</tr>
<tr><td>Z<td>Array of 3-coord (Lat,Lon,Elev) floats</tr>
<tr><td>Y<td>Array of 3-coord (Lat,Lon,Elev) double</tr>
<tr><td>D<td>Date Field</tr>
<tr><td>X<td>Null (no value) field</tr>
<tr><td>K<td>Cross-Tile identifier</tr>
<tr><td><td></tr>
</table>
<li><b>Count</b>: The number of values (* indicates variable number). 
	Types I,S,R,F,D,X,K are always 1.  (e.g type T count 12 would be a
	twelve character ascii string)
<li><b>Key Type</b>: 'P'-primary key, 'F'-foreign key, 'N'-not a key
<li><b>Description</b>: a description of the column
<li><b>VDT</b>: int.vdt or char.vdt if the value refers to additional info,
	--- otherwise
<li><b>Thematic Index</b>: the name of the thematic index for this column,
	--- otherwise
<li><b>Documentation File</b>: the name of the file with additional information
	about what the column is for, --- otherwise
</ul>
<% } else { %>
Other VPF help.
<% } %>
</body>
</html>
