try {
    $res = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
    Write-Host "Success:" ($res | ConvertTo-Json)
} catch {
    Write-Host "Caught exception:" $_.Exception.Message
    if ($_.ErrorDetails) {
        Write-Host "Error details:" $_.ErrorDetails.Message
    }
}
