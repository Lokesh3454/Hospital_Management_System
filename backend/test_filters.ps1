$base = "http://localhost:8080/api"
$auth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$headers = @{ "Authorization" = "Bearer $($auth.token)" }

# Test Patient Filters
$pName = Invoke-RestMethod -Uri "$base/patients?name=John" -Method Get -Headers $headers
Write-Host "Filter Patient by Name (John): Found" $pName.data.Count
$pPhone = Invoke-RestMethod -Uri "$base/patients?phone=0102" -Method Get -Headers $headers
Write-Host "Filter Patient by Phone (0102): Found" $pPhone.data.Count
$pEmail = Invoke-RestMethod -Uri "$base/patients?email=john.doe@email.com" -Method Get -Headers $headers
Write-Host "Filter Patient by Email (john.doe@email.com): Found" $pEmail.data.Count

# Test Doctor Filters
$dSpec = Invoke-RestMethod -Uri "$base/doctors?specialization=Cardiology" -Method Get -Headers $headers
Write-Host "Filter Doctor by Specialization (Cardiology): Found" $dSpec.data.Count
$dExp = Invoke-RestMethod -Uri "$base/doctors?experience=10" -Method Get -Headers $headers
Write-Host "Filter Doctor by Experience (>=10): Found" $dExp.data.Count

# Test Bills Filters
$billsPaid = Invoke-RestMethod -Uri "$base/bills?paymentStatus=PAID" -Method Get -Headers $headers
Write-Host "Filter Bills by Status PAID: Found" $billsPaid.Count
$billsPending = Invoke-RestMethod -Uri "$base/bills?paymentStatus=PENDING" -Method Get -Headers $headers
Write-Host "Filter Bills by Status PENDING: Found" $billsPending.Count
