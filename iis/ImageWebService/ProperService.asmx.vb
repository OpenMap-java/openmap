Imports System.IO
Imports System.Configuration
Imports System.Threading
Imports System.Web.Services

<WebService(Namespace:="http://winmap.bbn.com/ImageWebService", _
    Description:="This Web Service is responsible for starting and killing Java Image servers, updating user sessions, keeping information about layers used.")> _
Public Class ProperService
    Inherits System.Web.Services.WebService

#Region " Web Services Designer Generated Code "

    Public Sub New()
        MyBase.New()

        'This call is required by the Web Services Designer.
        InitializeComponent()

        'Add your own initialization code after the InitializeComponent() call

    End Sub

    'Required by the Web Services Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Web Services Designer
    'It can be modified using the Web Services Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()
        components = New System.ComponentModel.Container()
    End Sub

    Protected Overloads Overrides Sub Dispose(ByVal disposing As Boolean)
        'CODEGEN: This procedure is required by the Web Services Designer
        'Do not modify it using the code editor.
        If disposing Then
            If Not (components Is Nothing) Then
                components.Dispose()
            End If
        End If
        MyBase.Dispose(disposing)
    End Sub

#End Region

    'Get properties from web.config
    Private Shared PortOffset As Integer = CType(ConfigurationSettings.AppSettings("PortOffset"), Integer)
    Private Shared PropFile As String = ConfigurationSettings.AppSettings("PropFile")
    Private Shared DefaultPath As String = ConfigurationSettings.AppSettings("DefaultPath")
    Private Shared JavaArguments As String = ConfigurationSettings.AppSettings("JavaArgs")

    Private Shared hshProcesses As New Hashtable()
    Private Shared nextAvailable As Integer = 0

    '<WebMethod(Description:="This method updates a user's session.", EnableSession:=True)> _
    Public Function UpdateSession(ByVal procId As Integer, ByVal port As Integer) As String
        If Not Session("ProcId") Is Nothing Then
            Dim id As Integer = CInt(Session("ProcId"))

            If Not id = 0 And Not id = procId Then
                Dim proc As New Process()
                Try
                    proc = proc.GetProcessById(id)
                    proc.Kill()
                Catch exc As Exception
                End Try
                hshProcesses.Remove(id)
            End If
        End If
        Session("port") = port
        Session("ProcId") = procId
        Return "Success"
    End Function

    <WebMethod(Description:="This method starts a new java image server.", _
        EnableSession:=True)> Public Function StartNewImageServer() As String

        Dim proc As New Process()
        Dim port As Integer = PortOffset + nextAvailable
        proc.StartInfo.Arguments() = JavaArguments + DefaultPath + PropFile + " -port " + port.ToString
        proc.StartInfo.FileName = "java.exe"
        proc.Start()
        Dim value As New ArrayList(2)
        value.Insert(0, Date.Now)
        value.Insert(1, port)
        hshProcesses.Add(proc.Id, value)
        nextAvailable = nextAvailable Mod 99 + 1
        UpdateSession(proc.Id, port)
        Thread.Sleep(2000)
        StartNewImageServer = "Success: Started new image server" '+ " with process ID " + proc.Id.ToString
    End Function

    <WebMethod(Description:="This method kills an image server on a machine.", EnableSession:=True)> _
        Public Function KillImageServer(ByVal procId As Integer) As String
        If procId = 0 Then
            procId = CType(Session("ProcId"), Integer)
        End If
        If procId > 0 Then
            Dim proc As New Process()
            proc = proc.GetProcessById(procId)
            If (hshProcesses.Contains(procId)) Then
                proc.Kill()
                hshProcesses.Remove(procId)
                While Not proc.HasExited
                    'Do Nothing
                End While
            End If
            If procId = CType(Session("ProcId"), Integer) Then
                UpdateSession(0, 0)
            End If
            Return "Success"
        Else
            Return "Failure"
        End If
    End Function

    <WebMethod(Description:="This method kills all the running java image server processes.", _
        EnableSession:=True)> Public Function KillAllImageServers() As String
        Dim proc As New Process()
        Dim i As Integer = 0
        Dim keys As IEnumerator = hshProcesses.Keys.GetEnumerator
        While keys.MoveNext
            Try
                proc = proc.GetProcessById(keys.Current)
                proc.Kill()
            Catch exc As ArgumentException
            End Try
        End While
        hshProcesses.Clear()
        nextAvailable = 0
        Thread.Sleep(2000)
        UpdateSession(0, 0)
        KillAllImageServers = "Success: Killed all image servers."
    End Function

    '<WebMethod(Description:="This method returns the next available port for a new java image server.")> _
    Public Function GetNextAvailablePort() As Integer
        GetNextAvailablePort = PortOffset + nextAvailable
    End Function

    '<WebMethod(Description:="This method is used by a timer to kill long running java image servers.", _
    '    EnableSession:=True)> _
    Public Function CleanServerProcs(ByVal iMinutes As Integer) As String
        Dim proc As New Process()
        Dim i As Integer = 0
        Dim keys As IEnumerator = hshProcesses.Keys.GetEnumerator
        Dim keys_to_remove As New ArrayList()
        Dim filename As String
        Dim value As ArrayList
        Dim start_date As Date
        Dim port As Integer
        While keys.MoveNext
            start_date = CType(CType(hshProcesses.Item(keys.Current), ArrayList)(0), Date)
            If Date.Now.Subtract(start_date).Minutes > iMinutes Then
                Try
                    proc = proc.GetProcessById(keys.Current)
                    proc.Kill()
                Catch exc As ArgumentException
                End Try
                keys_to_remove.Add(keys.Current)
            End If
        End While
        For i = 0 To keys_to_remove.Count - 1
            hshProcesses.Remove(keys_to_remove(i))
        Next
        CleanServerProcs = "Success: Killed " + keys_to_remove.Count.ToString + " image servers."
    End Function

    <WebMethod(Description:="This method returns arrays of available and default layers.", _
        EnableSession:=True)> Public Function GetLayers() As String()
        ' Open the properties file to read.
        Dim sr As StreamReader = File.OpenText(DefaultPath + PropFile)

        ' Read each line in the file.
        Dim x As String = ";"
        Dim strProps As String
        While sr.Peek <> -1
            x = sr.ReadLine()
            If Not x.StartsWith("#") Then
                strProps = strProps + x + ";"
            End If
        End While

        'close the file
        sr.Close()

        Dim strStrings As String()
        strStrings = Array.CreateInstance(GetType(String), 3)
        Dim startIndex As Integer = strProps.IndexOf(";openmap.layers=")
        Dim endIndex As Integer = strProps.IndexOf(";", startIndex + 16)
        Dim strLayers As String = strProps.Substring(startIndex + 16, endIndex - startIndex - 16)
        strStrings(0) = strLayers
        Dim arrLayers As String() = strLayers.Split(" ")
        Dim arrPrettyNames As String() = Array.CreateInstance(GetType(String), arrLayers.Length)
        Dim strSearch As String
        Dim i As Integer
        strLayers = ""
        For i = 0 To arrLayers.Length - 1
            strSearch = ";" + arrLayers(i) + ".prettyName="
            startIndex = strProps.IndexOf(strSearch)
            endIndex = strProps.IndexOf(";", startIndex + strSearch.Length)
            strLayers = strLayers + strProps.Substring(startIndex + strSearch.Length, endIndex - startIndex - strSearch.Length) + ";"
            arrPrettyNames(i) = strProps.Substring(startIndex + strSearch.Length, endIndex - startIndex - strSearch.Length)
        Next
        strStrings(1) = strLayers
        'get the layers specified for the server
        startIndex = strProps.IndexOf(";openmap.startUpLayers=")
        endIndex = strProps.IndexOf(";", startIndex + 23)
        strLayers = strProps.Substring(startIndex + 23, endIndex - startIndex - 23)
        strStrings(2) = strLayers
        Dim arrCheckedLayers As String() = strLayers.Split(" ")
        Dim arrList As ArrayList = New ArrayList()
        arrList.Add(arrLayers)
        arrList.Add(arrPrettyNames)
        arrList.Add(arrPrettyNames)
        GetLayers = strStrings
    End Function

    Private Function GetSessionPort() As Integer
        Dim obj As Object = Session("port")
        If obj Is Nothing Then
            GetSessionPort = 0
        Else
            GetSessionPort = CType(obj, Integer)
        End If
    End Function

    Private Function GetSesstionProcId() As Integer
        Return CType(Session("ProcId"), Integer)
    End Function

    Private Function GetProcessStartTime(ByRef procId As Integer) As Date
        Dim value As ArrayList = CType(hshProcesses.Item(procId), ArrayList)
        Return CType(value(0), Date)
    End Function

    Private Function GetPortByProcId(ByRef procId As Integer) As Integer
        Dim value As ArrayList = CType(hshProcesses.Item(procId), ArrayList)
        If value Is Nothing Then
            GetPortByProcId = 0
        Else
            GetPortByProcId = CType(value(1), Integer)
        End If
    End Function

End Class
