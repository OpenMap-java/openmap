Imports System.IO
Imports System.Net
Imports System.Drawing.Imaging
Imports OpenMapWeb.com.bbn.winmap.GetMapService

Public Class MapImage
    Inherits System.Web.UI.Page

#Region " Web Form Designer Generated Code "

    'This call is required by the Web Form Designer.
    <System.Diagnostics.DebuggerStepThrough()> Private Sub InitializeComponent()

    End Sub

    Private Sub Page_Init(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Init
        'CODEGEN: This method call is required by the Web Form Designer
        'Do not modify it using the code editor.
        InitializeComponent()
    End Sub

#End Region
    Private oMapService As New GetMapService()
    Private cc As New CookieContainer()

    Private Sub Page_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        oMapService.CookieContainer = cc
        If Session("CookieContainer") Is Nothing Then
            Session("CookieContainer") = cc
        Else
            oMapService.CookieContainer = Session("CookieContainer")
        End If
        Try
            'Get the image from the web service
            Dim imageBytes As Byte() = oMapService.GetMapImage( _
            Request.Params.Get("lat"), _
            Request.Params.Get("lon"), _
            Request.Params.Get("scale"), _
            Request.Params.Get("proj"), _
            Request.Params.Get("height"), _
            Request.Params.Get("width"), _
            Request.Params.Get("bgcolor"), _
            Request.Params.Get("layers"))

            Dim memStream As New MemoryStream(imageBytes)
            Dim bmMem As New Bitmap(memStream)
            'Save to the output stream
            bmMem.Save(Response.OutputStream, ImageFormat.Gif)
        Catch exc As Exception
            Console.WriteLine(exc.Message)
        End Try
    End Sub

End Class
