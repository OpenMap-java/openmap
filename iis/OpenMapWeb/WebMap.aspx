<%@ Page language="vb" AutoEventWireup="false" Codebehind="WebMap.aspx.vb" Inherits="OpenMapWeb.WebForm1" smartNavigation="True" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
	<HEAD>
		<title>OpenMap .NET Demo</title>
		<meta content="Microsoft Visual Studio.NET 7.0" name="GENERATOR">
		<meta content="Visual Basic 7.0" name="CODE_LANGUAGE">
		<meta content="JavaScript" name="vs_defaultClientScript">
		<meta content="http://schemas.microsoft.com/intellisense/ie3-2nav3-0" name="vs_targetSchema">
	</HEAD>
	<body bgColor="#ebffff" MS_POSITIONING="GridLayout">
		<TABLE height="838" cellSpacing="0" cellPadding="0" width="675" border="0" ms_2d_layout="TRUE">
			<TR vAlign="top">
				<TD width="675" height="838">
					<form id="Form1" method="post" runat="server">
						<TABLE height="658" cellSpacing="0" cellPadding="0" width="826" border="0" ms_2d_layout="TRUE">
							<TR vAlign="top">
								<TD width="1" height="8"></TD>
								<TD width="1"></TD>
								<TD width="2"></TD>
								<TD width="2"></TD>
								<TD width="3"></TD>
								<TD width="1"></TD>
								<TD width="20"></TD>
								<TD width="1"></TD>
								<TD width="22"></TD>
								<TD width="2"></TD>
								<TD width="2"></TD>
								<TD width="49"></TD>
								<TD width="2"></TD>
								<TD width="57"></TD>
								<TD width="106"></TD>
								<TD width="1"></TD>
								<TD width="52"></TD>
								<TD width="26"></TD>
								<TD width="54"></TD>
								<TD width="1"></TD>
								<TD width="3"></TD>
								<TD width="5"></TD>
								<TD width="19"></TD>
								<TD width="1"></TD>
								<TD width="76"></TD>
								<TD width="56"></TD>
								<TD width="2"></TD>
								<TD width="50"></TD>
								<TD width="2"></TD>
								<TD width="4"></TD>
								<TD width="1"></TD>
								<TD width="1"></TD>
								<TD width="94"></TD>
								<TD width="56"></TD>
								<TD width="1"></TD>
								<TD width="50"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="2" height="52"></TD>
								<TD colSpan="26"><asp:label id="Label13" runat="server" Design_Time_Lock="True" Width="611px" Height="30px" Font-Size="Large">OpenMap.NET Web Services Demo from BBN Technologies.  </asp:label></TD>
								<TD colSpan="8"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="16" height="1"></TD>
								<TD rowSpan="6"><asp:label id="Label9" runat="server" Design_Time_Lock="True" Font-Bold="True" Width="42px" Height="13px">Height:</asp:label></TD>
								<TD colSpan="19"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="3" height="1"></TD>
								<TD colSpan="4" rowSpan="5"><asp:imagebutton id="imgbNW" runat="server" Design_Time_Lock="True" Width="16px" Height="16px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/nw.gif"></asp:imagebutton></TD>
								<TD colSpan="9"></TD>
								<TD rowSpan="6"><asp:imagebutton id="imgHeightPlus" runat="server" Design_Time_Lock="True" Width="22px" Height="22px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/plus.png"></asp:imagebutton></TD>
								<TD rowSpan="6"><asp:imagebutton id="imgHeightMinus" runat="server" Design_Time_Lock="True" Width="22px" Height="22px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/minus.png"></asp:imagebutton></TD>
								<TD colSpan="17" rowSpan="6"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="3" height="2"></TD>
								<TD colSpan="4"></TD>
								<TD rowSpan="4"><asp:imagebutton id="imgbNE" runat="server" Design_Time_Lock="True" Width="16px" Height="16px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/ne.gif"></asp:imagebutton></TD>
								<TD colSpan="4" rowSpan="2"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="3" height="2"></TD>
								<TD rowSpan="3"></TD>
								<TD rowSpan="3"><asp:imagebutton id="imgbN" runat="server" Design_Time_Lock="True" Width="16px" Height="16px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/n.gif"></asp:imagebutton></TD>
								<TD colSpan="2" rowSpan="3"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="3" height="10"></TD>
								<TD colSpan="2" rowSpan="4"><asp:imagebutton id="btnZoomIn" runat="server" Design_Time_Lock="True" Width="19px" Height="19px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/zoomin.gif"></asp:imagebutton></TD>
								<TD colSpan="2"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="3" height="4"></TD>
								<TD rowSpan="7"><asp:button id="btnGetMap" runat="server" Design_Time_Lock="True" Width="76px" Height="28px" Text="Get Map"></asp:button></TD>
								<TD></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="4" height="3"></TD>
								<TD colSpan="3" rowSpan="4"><asp:imagebutton id="imgbW" runat="server" Design_Time_Lock="True" Width="16px" Height="16px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/w.gif"></asp:imagebutton></TD>
								<TD colSpan="5"></TD>
								<TD colSpan="2"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="4" height="8"></TD>
								<TD colSpan="2" rowSpan="3"></TD>
								<TD colSpan="3" rowSpan="4"><asp:imagebutton id="imgbE" runat="server" Design_Time_Lock="True" Width="16px" Height="16px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/e.gif"></asp:imagebutton></TD>
								<TD colSpan="21"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="4" height="4"></TD>
								<TD colSpan="2"></TD>
								<TD colSpan="2" rowSpan="4"><asp:label id="Label10" runat="server" Design_Time_Lock="True" Font-Bold="True" Width="42px" Height="13px">Width:</asp:label></TD>
								<TD rowSpan="4"><asp:imagebutton id="imgWidthPlus" runat="server" Design_Time_Lock="True" Width="22px" Height="22px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/plus.png"></asp:imagebutton></TD>
								<TD rowSpan="4"><asp:imagebutton id="imgWidthMinus" runat="server" Design_Time_Lock="True" Width="22px" Height="22px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/minus.png"></asp:imagebutton></TD>
								<TD colSpan="17" rowSpan="4"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="4" height="3"></TD>
								<TD rowSpan="3"></TD>
								<TD rowSpan="3"><asp:imagebutton id="btnZoomOut" runat="server" Design_Time_Lock="True" Width="19px" Height="19px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/zoomout.gif"></asp:imagebutton></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="7" height="3"></TD>
								<TD colSpan="2" rowSpan="2"><asp:imagebutton id="imgbS" runat="server" Design_Time_Lock="True" Width="16px" Height="17px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/s.gif"></asp:imagebutton></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="3" height="19"></TD>
								<TD colSpan="4"><asp:imagebutton id="imgbSW" runat="server" Design_Time_Lock="True" Width="16px" Height="17px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/sw.gif"></asp:imagebutton></TD>
								<TD></TD>
								<TD colSpan="2"><asp:imagebutton id="imgbSE" runat="server" Design_Time_Lock="True" Width="16px" Height="17px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/se.gif"></asp:imagebutton></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="29" height="1"></TD>
								<TD colSpan="5" rowSpan="3"><asp:label id="Label12" runat="server" Design_Time_Lock="True" Font-Bold="True" Width="135px" Height="20px">Used Layers:</asp:label></TD>
								<TD colSpan="2" rowSpan="4"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="22" height="4"></TD>
								<TD colSpan="4" rowSpan="2"><asp:label id="Label11" runat="server" Design_Time_Lock="True" Font-Bold="True" Width="145px" Height="21px">Available Layers:</asp:label></TD>
								<TD colSpan="3" rowSpan="2"></TD>
							</TR>
							<TR vAlign="top">
								<TD height="18"></TD>
								<TD colSpan="18" rowSpan="15"><asp:imagebutton id="ibtnImage" runat="server" Design_Time_Lock="True" Width="400px" Height="350px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/OMLogo.bmp" AlternateText="Please wait while the map is loading...  If it's been more than 10 seconds, click on the &quot;GetMap&quot; button again."></asp:imagebutton></TD>
								<TD colSpan="3"></TD>
							</TR>
							<TR vAlign="top">
								<TD height="8"></TD>
								<TD colSpan="7" rowSpan="4"><asp:listbox id="lstAllLayers" runat="server" Design_Time_Lock="True" Width="156px" Height="194px" SelectionMode="Multiple"></asp:listbox></TD>
								<TD colSpan="2"></TD>
								<TD colSpan="6" rowSpan="4"><asp:listbox id="lstShownLayers" runat="server" Design_Time_Lock="True" Width="156px" Height="194px"></asp:listbox></TD>
							</TR>
							<TR vAlign="top">
								<TD height="28"></TD>
								<TD></TD>
								<TD><asp:imagebutton id="btnRight" runat="server" Design_Time_Lock="True" Width="40px" Height="25px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/right.gif" ToolTip="Add Layer(s)"></asp:imagebutton></TD>
								<TD colSpan="2" rowSpan="2"><asp:imagebutton id="btnUp" runat="server" Design_Time_Lock="True" Width="25px" Height="30px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/up.gif" ToolTip="Move Layer Up"></asp:imagebutton></TD>
							</TR>
							<TR vAlign="top">
								<TD height="14"></TD>
								<TD colSpan="2" rowSpan="2"><asp:imagebutton id="btnLeft" runat="server" Design_Time_Lock="True" Width="40px" Height="25px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/left.gif" ToolTip="Remove Layer"></asp:imagebutton></TD>
							</TR>
							<TR vAlign="top">
								<TD height="158"></TD>
								<TD></TD>
								<TD><asp:imagebutton id="btnDown" runat="server" Design_Time_Lock="True" Width="25px" Height="30px" ImageUrl="http://winmap.bbn.com/OpenMapWeb/images/down.gif" ToolTip="Move Layer Down"></asp:imagebutton></TD>
							</TR>
							<TR vAlign="top">
								<TD height="1"></TD>
								<TD rowSpan="3"></TD>
								<TD colSpan="8" rowSpan="3"><asp:label id="Label8" runat="server" Design_Time_Lock="True" Font-Bold="True" Width="183px" Height="26px" Font-Size="Medium">Advanced Settings:</asp:label></TD>
								<TD colSpan="8"></TD>
							</TR>
							<TR vAlign="top">
								<TD height="3"></TD>
								<TD colSpan="5"></TD>
								<TD colSpan="3" rowSpan="2"><asp:textbox id="TextBox5" runat="server" Design_Time_Lock="True" Width="105px">350</asp:textbox></TD>
							</TR>
							<TR vAlign="top">
								<TD height="23"></TD>
								<TD colSpan="3"></TD>
								<TD colSpan="2"><asp:label id="Label5" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">Height:</asp:label></TD>
							</TR>
							<TR vAlign="top">
								<TD height="2"></TD>
								<TD colSpan="6"></TD>
								<TD colSpan="3" rowSpan="3"><asp:textbox id="TextBox1" runat="server" Design_Time_Lock="True" Width="105px">30</asp:textbox></TD>
								<TD colSpan="5" rowSpan="2"></TD>
								<TD colSpan="3" rowSpan="3"><asp:textbox id="TextBox6" runat="server" Design_Time_Lock="True" Width="105px">350</asp:textbox></TD>
							</TR>
							<TR vAlign="top">
								<TD height="2"></TD>
								<TD colSpan="2" rowSpan="6"></TD>
								<TD colSpan="4" rowSpan="2"><asp:label id="Label1" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">LAT:</asp:label></TD>
							</TR>
							<TR vAlign="top">
								<TD height="22"></TD>
								<TD colSpan="2"></TD>
								<TD colSpan="3"><asp:label id="Label6" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">Width:</asp:label></TD>
							</TR>
							<TR vAlign="top">
								<TD height="4"></TD>
								<TD colSpan="4" rowSpan="2"><asp:label id="Label2" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">LON:</asp:label></TD>
								<TD colSpan="3" rowSpan="2"><asp:textbox id="TextBox2" runat="server" Design_Time_Lock="True" Width="105px">-70</asp:textbox></TD>
								<TD colSpan="5"></TD>
								<TD colSpan="3" rowSpan="2"><asp:textbox id="TextBox4" runat="server" Design_Time_Lock="True" Width="105px">cadrg</asp:textbox></TD>
							</TR>
							<TR vAlign="top">
								<TD height="21"></TD>
								<TD colSpan="4"></TD>
								<TD><asp:label id="Label4" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">Projection ID:</asp:label></TD>
							</TR>
							<TR vAlign="top">
								<TD height="4"></TD>
								<TD colSpan="4" rowSpan="2"><asp:label id="Label3" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">Scale:</asp:label></TD>
								<TD colSpan="3" rowSpan="2"><asp:textbox id="TextBox3" runat="server" Design_Time_Lock="True" Width="105px">250000000</asp:textbox></TD>
								<TD colSpan="5"></TD>
								<TD colSpan="3" rowSpan="2"><asp:textbox id="TextBox7" runat="server" Design_Time_Lock="True" Width="105px">FFFFFF</asp:textbox></TD>
							</TR>
							<TR vAlign="top">
								<TD height="52"></TD>
								<TD colSpan="4"></TD>
								<TD><asp:label id="Label7" runat="server" Design_Time_Lock="True" Width="90px" Height="20px">BG Color:</asp:label></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="23" height="10"></TD>
								<TD colSpan="13" rowSpan="2"><asp:rangevalidator id="RangeValidator1" runat="server" Design_Time_Lock="True" Width="362px" Height="25px" Display="Dynamic" ControlToValidate="TextBox5" MinimumValue="50" MaximumValue="1000" Type="Integer" ErrorMessage="Height values must be from 50 to 1000"></asp:rangevalidator></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="5" height="22"></TD>
								<TD colSpan="7" rowSpan="2"><asp:button id="btnEndSession" runat="server" Design_Time_Lock="True" Width="95px" Height="27px" Text="End Session"></asp:button></TD>
								<TD colSpan="11" rowSpan="2"></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="5" height="29"></TD>
								<TD colSpan="13"><asp:rangevalidator id="RangeValidator2" runat="server" Width="362px" Height="25px" Display="Dynamic" ControlToValidate="TextBox6" MinimumValue="50" MaximumValue="1000" Type="Integer" ErrorMessage="Width values must be from 50 to 1000"></asp:rangevalidator></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="24" height="37"></TD>
								<TD colSpan="12"><asp:rangevalidator id="RangeValidator3" runat="server" Width="362px" Height="25px" Display="Dynamic" ControlToValidate="TextBox3" MinimumValue="50" MaximumValue="10000000000" Type="Double" ErrorMessage="Scale must be an integer value"></asp:rangevalidator></TD>
							</TR>
							<TR vAlign="top">
								<TD colSpan="6" height="75"></TD>
								<TD colSpan="13">
									<DIV align="left" ms_positioning="FlowLayout">
										<TABLE height="74" cellSpacing="0" cellPadding="0" width="355" border="0" ms_1d_layout="TRUE">
											<TR>
												<TD>
													<P><A href="http://openmap.bbn.com"><STRONG><FONT face="Arial" size="1">OpenMap</FONT></STRONG></A><FONT face="Arial" size="1">
															is a trademark of BBN Technologies, a part of Verizon.<BR>
														</FONT><A href="http://www.gte.com/legals.html"><FONT face="Arial" size="1">©</FONT></A><FONT face="Arial" size="1">
															2001 BBNT Solutions LLC. All rights reserved. | </FONT><A href="http://www.gte.com/legals.html">
															<FONT face="Arial" size="1">Legal Information<BR>
																<BR>
															</FONT></A><FONT face="Arial" size="1">.NET is a trademark of Microsoft 
															Corporation.</FONT>
														<BR>
														<FONT face="Arial" size="1">All other trademarks are either owned by Verizon or by 
															other companies.<BR>
															<BR>
														</FONT><FONT face="Arial" size="1">Contact </FONT><A href="mailto:openmap@bbn.com"><FONT face="Arial" size="1">
																openmap@bbn.com</FONT></A><FONT face="Arial" size="1"> for more information 
															about OpenMap.</FONT></P>
												</TD>
											</TR>
										</TABLE>
									</DIV>
								</TD>
								<TD colSpan="17"></TD>
							</TR>
						</TABLE>
					</form>
				</TD>
			</TR>
		</TABLE>
	</body>
</HTML>
