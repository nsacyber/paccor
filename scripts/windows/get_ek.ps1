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
          ) {
                Write-Progress -Activity "Gathering an EK Certificate" -CurrentOperation "Accessing the TPM" -PercentComplete 10
                $data = $null
                $manufacturerCerts = (Get-TpmEndorsementKeyInfo).ManufacturerCertificates
                if ($manufacturerCerts -ne $null -and $manufacturerCerts.Length -gt 0)
                {
                    $data = $manufacturerCerts[0].GetRawCertData()
                } else {
                    $additionalCerts = (Get-TpmEndorsementKeyInfo).AdditionalCertificates
                    if ($additionalCerts -ne $null -and $additionalCerts.Length -gt 0) {
                        $data = $additionalCerts[0].GetRawCertData()
                    }
                }

                if ($data -eq $null) {
                    echo "Found no EK Certificates using the PowerShell TrustedPlatformModule module."
                    $data = $null
                } else {
                    Write-Progress -Activity "EK Certificate Gathered" -CurrentOperation "Converting to Base64" -PercentComplete 75
                    $base64 = [Convert]::ToBase64String($data,'InsertLineBreaks')
                    Write-Progress -Activity "EK Certificate Gathered" -CurrentOperation "Writing PEM" -PercentComplete 90
                    $pem = ("-----BEGIN CERTIFICATE-----`n$base64`n-----END CERTIFICATE-----").Replace("`r`n", "`n")
                    [IO.File]::WriteAllText($filename, $pem)
                    Write-Progress "Done" -PercentComplete 100
                }
            }
        Else {
            echo "Not admin"
        }
    }
)