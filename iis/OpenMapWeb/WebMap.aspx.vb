Imports System.IO
Imports System.Net
Imports System.Web.Services.Protocols
Imports OpenMapWeb.com.bbn.winmap.ProperService
Imports OpenMapWeb.com.bbn.winmap.GetMapService

Public Class WebForm1
    Inherits System.Web.UI.Page
    Protected WithEvents Label1 As System.Web.UI.WebControls.Label
    Protected WithEvents Label2 As System.Web.UI.WebControls.Label
    Protected WithEvents Label3 As System.Web.UI.WebControls.Label
    Protected WithEvents Label4 As System.Web.UI.WebControls.Label
    Protected WithEvents Label5 As System.Web.UI.WebControls.Label
    Protected WithEvents Label6 As System.Web.UI.WebControls.Label
    Protected WithEvents Label7 As System.Web.UI.WebControls.Label
    Protected WithEvents TextBox1 As System.Web.UI.WebControls.TextBox
    Protected WithEvents TextBox2 As System.Web.UI.WebControls.TextBox
    Protected WithEvents TextBox3 As System.Web.UI.WebControls.TextBox
    Protected WithEvents TextBox4 As System.Web.UI.WebControls.TextBox
    Protected WithEvents TextBox5 As System.Web.UI.WebControls.TextBox
    Protected WithEvents TextBox6 As System.Web.UI.WebControls.TextBox
    Protected WithEvents btnGetMap As System.Web.UI.WebControls.Button
    Protected WithEvents btnUpdate As System.Web.UI.WebControls.Button
    Protected WithEvents imgbN As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbS As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbW As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbE As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbSE As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbSW As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbNW As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgbNE As System.Web.UI.WebControls.ImageButton
    Protected WithEvents Label8 As System.Web.UI.WebControls.Label
    Protected WithEvents btnZoomIn As System.Web.UI.WebControls.ImageButton
    Protected WithEvents btnZoomOut As System.Web.UI.WebControls.ImageButton
    Protected WithEvents Label9 As System.Web.UI.WebControls.Label
    Protected WithEvents Label10 As System.Web.UI.WebControls.Label
    Protected WithEvents ibtnImage As System.Web.UI.WebControls.ImageButton
    Protected WithEvents lstAllLayers As System.Web.UI.WebControls.ListBox
    Protected WithEvents lstShownLayers As System.Web.UI.WebControls.ListBox
    Protected WithEvents btnUp As System.Web.UI.WebControls.ImageButton
    Protected WithEvents btnRight As System.Web.UI.WebControls.ImageButton
    Protected WithEvents btnDown As System.Web.UI.WebControls.ImageButton
    Protected WithEvents btnLeft As System.Web.UI.WebControls.ImageButton
    Protected WithEvents Label11 As System.Web.UI.WebControls.Label
    Protected WithEvents Label12 As System.Web.UI.WebControls.Label
    Protected WithEvents imgHeightPlus As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgWidthPlus As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgHeightMinus As System.Web.UI.WebControls.ImageButton
    Protected WithEvents imgWidthMinus As System.Web.UI.WebControls.ImageButton
    Protected WithEvents Panel2 As System.Web.UI.WebControls.Panel
    Protected WithEvents Panel1 As System.Web.UI.WebControls.Panel
    Protected WithEvents btnEndSession As System.Web.UI.WebControls.Button
    Protected WithEvents RangeValidator1 As System.Web.UI.WebControls.RangeValidator
    Protected WithEvents RangeValidator2 As System.Web.UI.WebControls.RangeValidator
    Protected WithEvents RangeValidator3 As System.Web.UI.WebControls.RangeValidator
    Protected WithEvents Label13 As System.Web.UI.WebControls.Label
    Protected WithEvents TextBox7 As System.Web.UI.WebControls.TextBox

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
    Private oPropService As New ProperService()
    Private cc As New CookieContainer()

    Private Shared arrLayers As String()
    Private Shared arrPrettyNames As String()

    Private Sub Page_Load(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles MyBase.Load
        oPropService.CookieContainer = cc
        oMapService.CookieContainer = cc

        If Session("CookieContainer") Is Nothing Then
            Session("CookieContainer") = cc
        Else
            oMapService.CookieContainer = Session("CookieContainer")
            oPropService.CookieContainer = Session("CookieContainer")
        End If
        If (lstAllLayers.Items.Count = 0) Then
            LoadLayers()
        End If
    End Sub

    Private Function GetMap()
        Dim arrContent As Byte()
        Dim memStream As MemoryStream

        Try
            ibtnImage.Height = New Unit(TextBox5.Text())
            ibtnImage.Width = New Unit(TextBox6.Text())
            'Pass the parameters from the textboxes
            ibtnImage.ImageUrl() = "MapImage.aspx" & _
                "?lat=" + TextBox1.Text & _
                "&lon=" + TextBox2.Text & _
                "&scale=" + TextBox3.Text & _
                "&proj=" + TextBox4.Text & _
                "&height=" + TextBox5.Text & _
                "&width=" + TextBox6.Text & _
                "&bgcolor=" + TextBox7.Text & _
                "&layers=" + GetLayers() & _
                "&id=" + Date.Now
        Catch mySE As SoapException
            Console.WriteLine("Could not download picture!")
        End Try
    End Function

    Private Sub btnGetMap_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnGetMap.Click
        GetMap()
    End Sub

    Private Function PanMap(ByVal strAzimuth As String)

        Try
            'Get LAT/LON values
            Dim arrContent As String() = oMapService.PanMap(TextBox1.Text, TextBox2.Text, TextBox3.Text, _
                TextBox4.Text, TextBox5.Text, TextBox6.Text, TextBox7.Text, strAzimuth, GetLayers())
            TextBox1.Text = arrContent(0)
            TextBox2.Text = arrContent(1)
            Dim str As String = "MapImage.aspx" & _
                "?lat=" + TextBox1.Text & _
                "&lon=" + TextBox2.Text & _
                "&scale=" + TextBox3.Text & _
                "&proj=" + TextBox4.Text & _
                "&height=" + TextBox5.Text & _
                "&width=" + TextBox6.Text & _
                "&bgcolor=" + TextBox7.Text & _
                "&layers=" + GetLayers() & _
                "&id=" + Date.Now
            ibtnImage.ImageUrl() = str

        Catch mySE As SoapException
            Console.WriteLine("Could not download picture!")
        End Try
    End Function

    Private Sub imgbSE_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbSE.Click
        PanMap("135")
    End Sub

    Private Sub imgbN_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbN.Click
        PanMap("0")
    End Sub

    Private Sub imgbNW_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbNW.Click
        PanMap("-45")
    End Sub

    Private Sub imgbNE_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbNE.Click
        PanMap("45")
    End Sub

    Private Sub imgbW_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbW.Click
        PanMap("-90")
    End Sub

    Private Sub imgbE_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbE.Click
        PanMap("90")
    End Sub

    Private Sub imgbS_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbS.Click
        PanMap("180")
    End Sub

    Private Sub imgbSW_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgbSW.Click
        PanMap("-135")
    End Sub

    Private Sub btnZoomIn_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles btnZoomIn.Click
        Dim iScale As Double = Double.Parse(TextBox3.Text)
        If iScale > 100 Then
            TextBox3.Text = Math.Round(iScale / 2, 0).ToString
        End If
        GetMap()
    End Sub

    Private Sub btnZoomOut_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles btnZoomOut.Click
        Dim iScale As Integer = Integer.Parse(TextBox3.Text)
        If iScale < 250000000 Then
            TextBox3.Text = (iScale * 2).ToString
        End If
        GetMap()
    End Sub

    Private Sub ibtnImage_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles ibtnImage.Click
        Try
            'Recenter the image
            Dim arrContent As String() = oMapService.RecenterMap(TextBox1.Text, TextBox2.Text, TextBox3.Text, _
                TextBox4.Text, TextBox5.Text, TextBox6.Text, TextBox7.Text, e.X, e.Y, GetLayers())
            TextBox1.Text = arrContent(0)
            TextBox2.Text = arrContent(1)
            Dim str As String = "MapImage.aspx" & _
                "?lat=" + TextBox1.Text & _
                "&lon=" + TextBox2.Text & _
                "&scale=" + TextBox3.Text & _
                "&proj=" + TextBox4.Text & _
                "&height=" + TextBox5.Text & _
                "&width=" + TextBox6.Text & _
                "&bgcolor=" + TextBox7.Text & _
                "&layers=" + GetLayers() & _
                "&id=" + Date.Now
            ibtnImage.ImageUrl() = str

        Catch mySE As SoapException
            Console.WriteLine("Could not download picture!")
        End Try
    End Sub

    Private Sub btnRight_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles btnRight.Click
        'Show selected layers
        Dim i As Integer
        For i = 0 To lstAllLayers.Items.Count - 1
            If lstAllLayers.Items.Item(i).Selected = True Then
                If Not lstShownLayers.Items.Contains(lstAllLayers.Items.Item(i)) Then
                    lstShownLayers.Items.Insert(0, lstAllLayers.Items.Item(i).ToString)
                End If
            End If
        Next
        GetMap()
    End Sub

    Private Sub btnLeft_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles btnLeft.Click
        'Remove selected layer
        If lstShownLayers.SelectedIndex > -1 Then
            lstShownLayers.Items.Remove(lstShownLayers.SelectedItem)
            GetMap()
        End If
    End Sub

    Private Sub btnUp_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles btnUp.Click
        'move a layer to the front
        If lstShownLayers.SelectedIndex > 0 Then
            Dim index As Integer = lstShownLayers.SelectedIndex
            Dim item As ListItem = lstShownLayers.Items(index)
            lstShownLayers.Items.RemoveAt(index)
            lstShownLayers.Items.Insert(index - 1, item)
            GetMap()
        End If
    End Sub

    Private Sub btnDown_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles btnDown.Click
        'move a layer to the back
        If lstShownLayers.SelectedIndex > -1 And lstShownLayers.SelectedIndex < lstShownLayers.Items.Count - 1 Then
            Dim index As Integer = lstShownLayers.SelectedIndex
            Dim item As ListItem = lstShownLayers.Items(index)
            lstShownLayers.Items.RemoveAt(index)
            lstShownLayers.Items.Insert(index + 1, item)
            GetMap()
        End If
    End Sub

    Private Function GetLayers() As String
        'Get all available layers
        Dim i As Integer
        Dim index As Integer
        Dim strLayers As String
        For i = 0 To lstShownLayers.Items.Count - 1
            index = arrPrettyNames.IndexOf(arrPrettyNames, lstShownLayers.Items.Item(i).ToString)
            If index >= 0 Then
                If i = 0 Then
                    strLayers = arrLayers(index)
                Else
                    strLayers = strLayers + "," + arrLayers(index)
                End If
            End If
        Next
        GetLayers = strLayers
    End Function

    Private Function LoadLayers() As String
        Dim strArray As String() = oPropService.GetLayers

        arrLayers = strArray(0).Split(" ")
        arrPrettyNames = strArray(1).Split(";")
        Dim arrCheckedLayers As String() = strArray(2).Split(" ")
        Dim i As Integer
        lstAllLayers.Items.Clear()
        lstShownLayers.Items.Clear()
        For i = 0 To arrLayers.Length - 1
            lstAllLayers.Items.Add(arrPrettyNames(i))
            If Array.IndexOf(arrCheckedLayers, arrLayers(i)) >= 0 Then
                lstShownLayers.Items.Add(arrPrettyNames(i))
            End If
        Next
    End Function

    Private Sub imgHeightPlus_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgHeightPlus.Click
        TextBox5.Text = (Integer.Parse(TextBox5.Text) + 50).ToString
        GetMap()
    End Sub

    Private Sub imgHeightMinus_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgHeightMinus.Click
        Dim size As Integer = Integer.Parse(TextBox5.Text)
        If size > 50 Then
            TextBox5.Text = (size - 50).ToString
            GetMap()
        End If
    End Sub

    Private Sub imgWidthPlus_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgWidthPlus.Click
        TextBox6.Text = (Integer.Parse(TextBox6.Text) + 50).ToString
        GetMap()
    End Sub

    Private Sub imgWidthMinus_Click(ByVal sender As System.Object, ByVal e As System.Web.UI.ImageClickEventArgs) Handles imgWidthMinus.Click
        Dim size As Integer = Integer.Parse(TextBox6.Text)
        If size > 50 Then
            TextBox6.Text = (size - 50).ToString
            GetMap()
        End If
    End Sub

    Private Sub btnEndSession_Click(ByVal sender As System.Object, ByVal e As System.EventArgs) Handles btnEndSession.Click
        oPropService.KillImageServer(0)
    End Sub
End Class
