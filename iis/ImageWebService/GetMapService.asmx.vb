'<%@ WebService Language="vb" Codebehind="GetMapService.asmx.vb" Class="ImageWebService.GetMapService" %>
Imports System.Configuration
Imports System.Drawing
Imports System.Drawing.Imaging
Imports System.IO
Imports System.Net
Imports System.Web.Services

<WebService(Namespace:="http://winmap.bbn.com/ImageWebService", _
Description:="This Web Service returns images from the server in a base64 encoded manner.")> _
Public Class GetMapService
    Inherits System.Web.Services.WebService

    Private Shared PortOffset As Integer = CType(ConfigurationSettings.AppSettings("PortOffset"), Integer)
    Private oPropService As New ProperService()

    <WebMethod(Description:="This method returns a map image in base64 encoded format.", _
    EnableSession:=True)> _
     Public Function GetMapImage(ByVal strLat As String, ByVal strLon As String, ByVal strScale As String, _
        ByVal strProjType As String, ByVal strHeight As String, ByVal strWidth As String, ByVal strBgColor As String, ByVal strLayers As String) As Byte()

        Dim port As Integer = CheckProcess()
        Return GetImageStream(port, strLat, strLon, strScale, strProjType, strHeight, strWidth, strBgColor, strLayers)
    End Function

    <WebMethod(Description:="This method pans the map and returns a Lat/Lon coordinate.", _
    EnableSession:=True)> _
 Public Function PanMap(ByVal strLat As String, ByVal strLon As String, ByVal strScale As String, _
    ByVal strProjType As String, ByVal strHeight As String, ByVal strWidth As String, _
    ByVal strBgColor As String, ByVal strAzimuth As String, ByVal strLayers As String) As String()

        Dim port As Integer = CheckProcess()
        Dim url As String = "http://localhost:" + port.ToString + "/openmap?REQUEST=PAN" & _
                        "&LAT=" + strLat & _
                        "&LON=" + strLon & _
                        "&SCALE=" + strScale & _
                        "&PROJTYPE=" + strProjType & _
                        "&HEIGHT=" + strHeight & _
                        "&WIDTH=" + strWidth & _
                        "&BGCOLOR=" + strBgColor & _
                        "&LAYERS=" + strLayers & _
                        "&AZIMUTH=" + strAzimuth

        Dim req As WebRequest = WebRequest.Create(url)
        Dim result As WebResponse = req.GetResponse()
        Dim oRead As StreamReader = New StreamReader(result.GetResponseStream())
        Dim LatLon As String() = String.Copy(oRead.ReadToEnd).Split(":")
        'GetImageStream(port, LatLon(0), LatLon(1), strScale, strProjType, strHeight, strWidth, strBgColor, strLayers)
        Return LatLon
    End Function

    <WebMethod(Description:="This method recenters the map and returns a Lat/Lon coordinate.", _
    EnableSession:=True)> _
    Public Function RecenterMap(ByVal strLat As String, ByVal strLon As String, ByVal strScale As String, _
         ByVal strProjType As String, ByVal strHeight As String, ByVal strWidth As String, ByVal strBgColor As String, _
         ByVal strX As String, ByVal strY As String, ByVal strLayers As String) As String()

        Dim port As Integer = CheckProcess()
        Dim url As String = "http://localhost:" + port.ToString + "/openmap?REQUEST=RECENTER" & _
                        "&LAT=" + strLat & _
                        "&LON=" + strLon & _
                        "&SCALE=" + strScale & _
                        "&PROJTYPE=" + strProjType & _
                        "&HEIGHT=" + strHeight & _
                        "&WIDTH=" + strWidth & _
                        "&BGCOLOR=" + strBgColor & _
                        "&LAYERS=" + strLayers & _
                        "&X=" + strX & _
                        "&Y=" + strY

        Dim req As WebRequest = WebRequest.Create(url)
        Dim result As WebResponse = req.GetResponse()
        Dim oRead As StreamReader = New StreamReader(result.GetResponseStream())
        Dim LatLon As String() = String.Copy(oRead.ReadToEnd).Split(":")
        'GetImageStream (port, LatLon(0), LatLon(1), strScale, strProjType, strHeight, strWidth, strBgColor, strLayers)
        Return LatLon
    End Function

    Public Function GetImageStream(ByVal port As Integer, ByVal strLat As String, ByVal strLon As String, ByVal strScale As String, _
             ByVal strProjType As String, ByVal strHeight As String, ByVal strWidth As String, ByVal strBgColor As String, ByVal strLayers As String) As Byte()
        'Make a MAP request to the image server and return raw data
        Dim MyImg As Image
        Dim req As WebRequest
        Dim result As WebResponse
        Dim ReceiveStream As Stream

        Dim url As String = "http://localhost:" + port.ToString + "/openmap?REQUEST=MAP" & _
                        "&LAT=" + strLat & _
                        "&LON=" + strLon & _
                        "&SCALE=" + strScale & _
                        "&PROJTYPE=" + strProjType & _
                        "&HEIGHT=" + strHeight & _
                        "&WIDTH=" + strWidth & _
                        "&BGCOLOR=" + strBgColor & _
                        "&LAYERS=" + strLayers

        req = WebRequest.Create(url)
        result = req.GetResponse()
        ReceiveStream = result.GetResponseStream()
        MyImg = New Bitmap(ReceiveStream)
        Dim MemStr As New MemoryStream()
        MyImg.Save(MemStr, ImageFormat.Gif)
        Return MemStr.GetBuffer()
    End Function

    Private Function CheckProcess() As Integer
        'Check whether the process is running
        Dim port As Integer = CType(Session("port"), Integer)
        If port < PortOffset Then
            oPropService.StartNewImageServer()
            port = CType(Session("port"), Integer)
        Else
            Dim procId As Integer = CType(Session("ProcId"), Integer)
            Try 'in the case that the process has been killed
                Dim proc As Process = Process.GetProcessById(procId)
                proc.Dispose()
                proc = Nothing
            Catch
                oPropService.StartNewImageServer()
                port = CType(Session("port"), Integer)
            End Try
        End If
        CheckProcess = port
    End Function

End Class
