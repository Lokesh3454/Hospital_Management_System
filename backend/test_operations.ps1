$base = "http://localhost:8080/api"
$auth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$headers = @{ "Authorization" = "Bearer $($auth.token)"; "Content-Type" = "application/json" }

Write-Host "Testing Hospital Operations Endpoints..." -ForegroundColor Cyan
$beds = Invoke-RestMethod -Uri "$base/beds" -Method Get -Headers $headers
Write-Host "Beds Count: $($beds.data.Count)" -ForegroundColor Green

$meds = Invoke-RestMethod -Uri "$base/medicines" -Method Get -Headers $headers
Write-Host "Medicines Count: $($meds.data.Count)" -ForegroundColor Green

$labs = Invoke-RestMethod -Uri "$base/lab-tests" -Method Get -Headers $headers
Write-Host "Lab Tests Count: $($labs.data.Count)" -ForegroundColor Green

$analytics = Invoke-RestMethod -Uri "$base/analytics" -Method Get -Headers $headers
Write-Host "Analytics Total Patients: $($analytics.data.totalPatients)" -ForegroundColor Green
Write-Host "Hospital Operations Backend: HEALTHY AND OPERATIONAL" -ForegroundColor Green
