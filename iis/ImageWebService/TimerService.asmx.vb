Imports System.Threading
Imports System.Web.Services

<WebService(Description:="This Web Service is a utility to perform 'cleaning' of the image servers.", _
Namespace:="http://winmap.bbn.com/ImageWebService")> _
Public Class TimerService
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

    Private Shared timer As timer
    Private Shared interval As Integer
    Private Shared propService As New ProperService()
    Private Shared t As Thread
    ' Create the delegate that invokes a method for the timer.
    Private timerDelegate As New TimerCallback(AddressOf CheckStatus)

    '<WebMethod(Description:="This method starts the timer to watch for image servers.")> _
    Public Function StartVerification(ByVal minutes As Integer) As String

        interval = minutes

        ' Create a timer that waits one second, then invokes every <minutes> minutes.
        timer = New Timer(timerDelegate, timer, 1000, interval * 60 * 1000)
        If Not t Is Nothing Then
            If t.IsAlive Then
                t.Abort()
            End If
            While t.IsAlive
                'Do nothing
            End While
        End If
        t = New Thread(AddressOf CleanServices)
        t.Name = "CleanServerProcs"
        t.Start()

        StartVerification = "Success: Timer started with the interval of " + minutes.ToString + " minute(s)"
    End Function

    ' The following method is called by the timer's delegate.
    Private Sub CheckStatus(ByVal state As [Object])
        Dim result As String = propService.CleanServerProcs(interval)
        Console.WriteLine(result)
    End Sub 'CheckStatus

    Private Sub CleanServices()
        Console.WriteLine("In Thread")
        While Not (timer Is Nothing)
            Thread.Sleep(interval * 60 * 1000) 'in milliseconds
        End While
    End Sub 'CheckStatus

    '<WebMethod(Description:="This method stops the timer.")> _
    Public Function StopVerification() As String
        If Not timer Is Nothing Then
            timer.Dispose()
            timer = Nothing
        End If
        StopVerification = "Success: Timer has stopped."
    End Function

End Class
