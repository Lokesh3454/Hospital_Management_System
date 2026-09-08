$authRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body '{"username":"receptionist_sarah","password":"rec123"}'
$token = $authRes.token
Write-Host "Logged in as: $($authRes.username) with roles: $($authRes.roles -join ',')"
$headers = @{ Authorization = "Bearer $token" }

$endpoints = @(
  'beds',
  'medicines',
  'lab-tests',
  'analytics',
  'emergency/active',
  'surgery',
  'blood-bank',
  'insurance',
  'staff-roster',
  'notifications/recent',
  'appointments'
)

foreach ($ep in $endpoints) {
  try {
    $null = Invoke-RestMethod -Uri "http://localhost:8080/api/$ep" -Headers $headers
    Write-Host "[SUCCESS] /api/$ep"
  } catch {
    Write-Host "[FAILED]  /api/$ep - $($_.Exception.Message)"
  }
}
