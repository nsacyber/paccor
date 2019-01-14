param(
    [parameter(Mandatory=$true)]
    [ValidateNotNull()]
    [string]$filename
)

(&{
        Write-Progress -Activity "Gathering an EK Certificate" -CurrentOperation "Verifying access to the TPM through Windows" -PercentComplete 0
        If( (New-Object Security.Principal.WindowsPrincipal(
                [Security.Principal.WindowsIdentity]::GetCurrent())
            ).IsInRole(
                [Security.Principal.WindowsBuiltInRole]::Administrator)
            ) 
            {
                Write-Progress -Activity "Gathering an EK Certificate" -CurrentOperation "Accessing the TPM" -PercentComplete 10
                $data=(Get-TpmEndorsementKeyInfo).ManufacturerCertificates[0].GetRawCertData()
                Write-Progress -Activity "EK Certificate Gathered" -CurrentOperation "Converting to Base64" -PercentComplete 75
                $base64 = [Convert]::ToBase64String($data,'InsertLineBreaks')
                Write-Progress -Activity "EK Certificate Gathered" -CurrentOperation "Writing PEM" -PercentComplete 90
                $pem = ("-----BEGIN CERTIFICATE-----`n$base64`n-----END CERTIFICATE-----").Replace("`r`n", "`n")
                [IO.File]::WriteAllText($filename, $pem)
                Write-Progress "Done" -PercentComplete 100
            }
        Else {
            echo "Not admin"
        }
    }
)