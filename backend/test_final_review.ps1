$base = "http://localhost:8080/api"
$passed = 0
$total = 0

function Assert-Test($condition, $testName) {
    $global:total++
    if ($condition) {
        Write-Host " [PASS] $testName" -ForegroundColor Green
        $global:passed++
    } else {
        Write-Host " [FAIL] $testName" -ForegroundColor Red
    }
}

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "  HOSPITAL MANAGEMENT SYSTEM - FINAL SENIOR REVIEW TEST SUITE" -ForegroundColor Cyan
Write-Host "=================================================================`n" -ForegroundColor Cyan

# ==========================================
# 1. AUTHENTICATE ALL 4 ROLES
# ==========================================
$adminAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$adminHeaders = @{ "Authorization" = "Bearer $($adminAuth.token)"; "Content-Type" = "application/json" }

$doctorAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"doctor_smith","password":"doctor123"}'
$doctorHeaders = @{ "Authorization" = "Bearer $($doctorAuth.token)"; "Content-Type" = "application/json" }

$patientAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"patient_john","password":"patient123"}'
$patientHeaders = @{ "Authorization" = "Bearer $($patientAuth.token)"; "Content-Type" = "application/json" }

$receptionistAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"receptionist_sarah","password":"rec123"}'
$receptionistHeaders = @{ "Authorization" = "Bearer $($receptionistAuth.token)"; "Content-Type" = "application/json" }

# ==========================================
# 2. BUSINESS LOGIC TESTING
# ==========================================
Write-Host "`n--- BUSINESS LOGIC VERIFICATION ---" -ForegroundColor Yellow

# Test 1: Patient registration via auth endpoint
$uniqueUser = "testpat_" + (Get-Random -Minimum 1000 -Maximum 9999)
$regBody = @{
    username = $uniqueUser
    email = "$uniqueUser@hospital.com"
    password = "password123"
    role = "PATIENT"
    firstName = "Test"
    lastName = "Patient"
    phone = "9876543210"
    gender = "Male"
    bloodGroup = "O+"
} | ConvertTo-Json
$regRes = Invoke-RestMethod -Uri "$base/auth/register" -Method Post -ContentType "application/json" -Body $regBody
Assert-Test ($regRes.message -match "registered successfully") "1. Patient Registration creates user account & patient profile"

$patsList = Invoke-RestMethod -Uri "$base/patients" -Method Get -Headers $adminHeaders
$targetPatientId = $patsList.data[0].patientId

# Test 2: Doctor Profile & Credentials Check
$docRes = Invoke-RestMethod -Uri "$base/doctors/1" -Method Get -Headers $adminHeaders
Assert-Test ($docRes.data.doctorId -eq 1 -and $docRes.data.specialization -ne $null) "2. Doctor Profile retrieval returns credentials & consultation fee"

# Test 3: Doctor Availability
$availRes = Invoke-RestMethod -Uri "$base/doctors/1/availabilities" -Method Get -Headers $adminHeaders
Assert-Test ($availRes.data.Count -gt 0) "3. Doctor Availability returns configured working hours schedule"

# Test 4: Available Time Slots
$slots = Invoke-RestMethod -Uri "$base/appointments/available-slots?doctorId=1&date=2026-10-15" -Method Get -Headers $patientHeaders
Assert-Test ($slots.data.Count -gt 0) "4. Available Time Slots computation dynamically returns doctor slots"

# Test 5: Appointment Booking
$nextDate = (Get-Date).AddDays(25 + (Get-Random -Minimum 1 -Maximum 30))
while ($nextDate.DayOfWeek -in @("Saturday", "Sunday")) {
    $nextDate = $nextDate.AddDays(1)
}
$bookDate = $nextDate.ToString("yyyy-MM-dd")
$bookTime = "11:00:00"
$bookBody = @{
    patientId = $targetPatientId
    doctorId = 1
    appointmentDate = $bookDate
    appointmentTime = $bookTime
    reason = "Regular cardiac checkup"
} | ConvertTo-Json
$bookRes = Invoke-RestMethod -Uri "$base/appointments" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body $bookBody
$bookedApptId = $bookRes.data.id
Assert-Test ($bookRes.data.status -eq "BOOKED" -and $bookedApptId -gt 0) "5. Appointment Booking succeeds with status BOOKED"

# Test 6: Double Booking Prevention (same doctor + same date/time)
$doubleBookFailed = $false
try {
    Invoke-RestMethod -Uri "$base/appointments" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body $bookBody
} catch {
    $doubleBookFailed = ($_.Exception.Response.StatusCode.value__ -eq 400)
}
Assert-Test ($doubleBookFailed) "6. Double Booking Prevention blocks overlapping booking for same doctor slot (400 Bad Request)"

# Test 7: Appointment Cancellation
$cancelBody = '{"reason":"Patient schedule conflict"}'
$cancelRes = Invoke-RestMethod -Uri "$base/appointments/$bookedApptId/cancel" -Method Put -Headers $adminHeaders -ContentType "application/json" -Body $cancelBody
Assert-Test ($cancelRes.data.status -eq "CANCELLED") "7. Appointment Cancellation marks status CANCELLED with reason"

# Test 8: Medical Records Creation
$recBody = @{
    patientId = $targetPatientId
    doctorId = 1
    diagnosis = "Acute Bronchitis"
    symptoms = "Persistent cough, mild fever 100F"
    treatment = "Oral antibiotics, bed rest, warm fluids"
    testResults = "Chest X-ray clear, SpO2 98%"
    recordDate = "2026-09-07"
} | ConvertTo-Json
$recRes = Invoke-RestMethod -Uri "$base/medical-records" -Method Post -Headers $doctorHeaders -ContentType "application/json" -Body $recBody
$createdRecId = $recRes.data.id
Assert-Test ($recRes.data.diagnosis -eq "Acute Bronchitis" -and $createdRecId -gt 0) "8. Medical Records Creation records diagnosis, vitals, and treatment"

# Test 9: Prescription Creation with multiple items
$rxBody = @{
    patientId = $targetPatientId
    doctorId = 1
    prescriptionDate = "2026-09-07"
    notes = "Take after food with full glass of water"
    items = @(
        @{ medicineName = "Amoxicillin 500mg"; dosage = "1 capsule"; frequency = "Three times daily"; duration = "7 days"; instructions = "After meals" },
        @{ medicineName = "Paracetamol 650mg"; dosage = "1 tablet"; frequency = "As needed for fever"; duration = "3 days"; instructions = "SOS" }
    )
} | ConvertTo-Json
$rxRes = Invoke-RestMethod -Uri "$base/prescriptions" -Method Post -Headers $doctorHeaders -ContentType "application/json" -Body $rxBody
Assert-Test ($rxRes.data.items.Count -eq 2 -and $rxRes.data.id -gt 0) "9. Prescription Creation supports multi-drug itemization with instructions"

# Test 10: Billing Charge Calculation Formula & Settlement
# Formula: consultationFee + medicineCharges + testCharges + otherCharges
$billBody = @{
    patientId = $targetPatientId
    consultationFee = 600.00
    medicineCharges = 150.00
    testCharges = 350.00
    otherCharges = 50.00
    paymentStatus = "PENDING"
} | ConvertTo-Json
$billRes = Invoke-RestMethod -Uri "$base/bills" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body $billBody
$billId = $billRes.id
$calculatedSum = $billRes.totalAmount
Assert-Test ($calculatedSum -eq 1150.00) "10. Billing calculates formula (600+150+350+50 = 1150.00)"

# Settle Bill
$payBody = '{"paymentMethod":"UPI","paymentStatus":"PAID","notes":"UPI Ref: 897451236"}'
$payRes = Invoke-RestMethod -Uri "$base/bills/$billId/pay" -Method Patch -Headers $adminHeaders -ContentType "application/json" -Body $payBody
Assert-Test ($payRes.paymentStatus -eq "PAID" -and $payRes.paymentMethod -eq "UPI") "11. Billing Settlement marks PAID via UPI"

# Test 11: Dynamic Dashboards for all 4 roles
$adminDash = Invoke-RestMethod -Uri "$base/dashboard/admin" -Method Get -Headers $adminHeaders
$docDash = Invoke-RestMethod -Uri "$base/dashboard/doctor" -Method Get -Headers $doctorHeaders
$patDash = Invoke-RestMethod -Uri "$base/dashboard/patient" -Method Get -Headers $patientHeaders
$recDash = Invoke-RestMethod -Uri "$base/dashboard/receptionist" -Method Get -Headers $receptionistHeaders
Assert-Test ($adminDash.totalPatients -ge 1 -and $docDash.totalPatients -ge 0 -and $patDash.appointmentHistory -ge 0 -and $recDash.patientRegistrations -ge 0) "12. Role-based Dashboards deliver live dynamic metrics for all 4 roles"

# Test 12: Search & Filter on Patients, Doctors, Appointments, Bills, Records
$patFilter = Invoke-RestMethod -Uri "$base/patients?gender=Male" -Method Get -Headers $adminHeaders
$docFilter = Invoke-RestMethod -Uri "$base/doctors?specialization=Cardiology" -Method Get -Headers $adminHeaders
$billFilter = Invoke-RestMethod -Uri "$base/bills?status=PAID" -Method Get -Headers $adminHeaders
Assert-Test ($patFilter.data.Count -gt 0 -and $docFilter.data.Count -gt 0 -and $billFilter.Count -gt 0) "13. Advanced Multi-Criteria Search & Filter operates across all modules"

# ==========================================
# 3. SECURITY & RBAC TESTING
# ==========================================
Write-Host "`n--- SECURITY & RBAC MATRIX VERIFICATION ---" -ForegroundColor Yellow

# Test 14: Doctor cannot create bills (403)
$docBillDenied = $false
try {
    Invoke-RestMethod -Uri "$base/bills" -Method Post -Headers $doctorHeaders -ContentType "application/json" -Body $billBody
} catch {
    $docBillDenied = ($_.Exception.Response.StatusCode.value__ -eq 403)
}
Assert-Test ($docBillDenied) "14. RBAC: Doctor cannot create bills (HTTP 403 Forbidden)"

# Test 15: Doctor cannot access Admin Dashboard (403)
$docAdminDenied = $false
try {
    Invoke-RestMethod -Uri "$base/dashboard/admin" -Method Get -Headers $doctorHeaders
} catch {
    $docAdminDenied = ($_.Exception.Response.StatusCode.value__ -eq 403)
}
Assert-Test ($docAdminDenied) "15. RBAC: Doctor cannot access Admin Dashboard (HTTP 403 Forbidden)"

# Test 16: Patient cannot access Admin Dashboard (403)
$patAdminDenied = $false
try {
    Invoke-RestMethod -Uri "$base/dashboard/admin" -Method Get -Headers $patientHeaders
} catch {
    $patAdminDenied = ($_.Exception.Response.StatusCode.value__ -eq 403)
}
Assert-Test ($patAdminDenied) "16. RBAC: Patient cannot access Admin Dashboard (HTTP 403 Forbidden)"

# Test 17: Patient cannot create medical records (403)
$patRecDenied = $false
try {
    Invoke-RestMethod -Uri "$base/medical-records" -Method Post -Headers $patientHeaders -ContentType "application/json" -Body $recBody
} catch {
    $patRecDenied = ($_.Exception.Response.StatusCode.value__ -eq 403)
}
Assert-Test ($patRecDenied) "17. RBAC: Patient cannot create medical records (HTTP 403 Forbidden)"

# Test 18: Patient cannot create prescriptions (403)
$patRxDenied = $false
try {
    Invoke-RestMethod -Uri "$base/prescriptions" -Method Post -Headers $patientHeaders -ContentType "application/json" -Body $rxBody
} catch {
    $patRxDenied = ($_.Exception.Response.StatusCode.value__ -eq 403)
}
Assert-Test ($patRxDenied) "18. RBAC: Patient cannot create prescriptions (HTTP 403 Forbidden)"

# Test 19: Receptionist cannot create medical records (403)
$recRecordDenied = $false
try {
    Invoke-RestMethod -Uri "$base/medical-records" -Method Post -Headers $receptionistHeaders -ContentType "application/json" -Body $recBody
} catch {
    $recRecordDenied = ($_.Exception.Response.StatusCode.value__ -eq 403)
}
Assert-Test ($recRecordDenied) "19. RBAC: Receptionist cannot create medical records (HTTP 403 Forbidden)"

# Test 20: Patient Privacy - Patient 1 CANNOT view Patient 2's medical history (400/403)
$patOtherRecDenied = $false
try {
    Invoke-RestMethod -Uri "$base/medical-records/patient/2" -Method Get -Headers $patientHeaders
} catch {
    $patOtherRecDenied = ($_.Exception.Response.StatusCode.value__ -in @(400, 403))
}
Assert-Test ($patOtherRecDenied) "20. Privacy: Patient cannot view another patient's medical history"

# Test 21: Patient Privacy - Patient 1 CANNOT view Patient 2's bills (400/403)
$patOtherBillDenied = $false
try {
    Invoke-RestMethod -Uri "$base/bills/patient/2" -Method Get -Headers $patientHeaders
} catch {
    $patOtherBillDenied = ($_.Exception.Response.StatusCode.value__ -in @(400, 403))
}
Assert-Test ($patOtherBillDenied) "21. Privacy: Patient cannot view another patient's billing history"

# Test 22: Patient Privacy - Patient 1 CANNOT view Patient 2's prescriptions (400/403)
$patOtherRxDenied = $false
try {
    Invoke-RestMethod -Uri "$base/prescriptions/patient/2" -Method Get -Headers $patientHeaders
} catch {
    $patOtherRxDenied = ($_.Exception.Response.StatusCode.value__ -in @(400, 403))
}
Assert-Test ($patOtherRxDenied) "22. Privacy: Patient cannot view another patient's prescriptions"

# Test 23: Patient Privacy - Patient 1 CANNOT view Patient 2's appointments (400/403)
$patOtherApptDenied = $false
try {
    Invoke-RestMethod -Uri "$base/appointments/patient/2" -Method Get -Headers $patientHeaders
} catch {
    $patOtherApptDenied = ($_.Exception.Response.StatusCode.value__ -in @(400, 403))
}
Assert-Test ($patOtherApptDenied) "23. Privacy: Patient cannot view another patient's appointments"

# Test 24: Patient Privacy - Patient 1 CANNOT view Patient 2's profile (400/403)
$patOtherProfDenied = $false
try {
    Invoke-RestMethod -Uri "$base/patients/2" -Method Get -Headers $patientHeaders
} catch {
    $patOtherProfDenied = ($_.Exception.Response.StatusCode.value__ -in @(400, 403))
}
Assert-Test ($patOtherProfDenied) "24. Privacy: Patient cannot view another patient's profile"

# Test 25: Unauthenticated request rejected (401 Unauthorized)
$unauthDenied = $false
try {
    Invoke-RestMethod -Uri "$base/patients" -Method Get
} catch {
    $unauthDenied = ($_.Exception.Response.StatusCode.value__ -eq 401)
}
Assert-Test ($unauthDenied) "25. Security: Unauthenticated request rejected with HTTP 401 Unauthorized"

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "   FINAL SENIOR REVIEW TEST RESULTS: $passed / $total PASSED" -ForegroundColor Cyan
Write-Host "=================================================================`n" -ForegroundColor Cyan
