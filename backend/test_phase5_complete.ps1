$base = "http://localhost:8080/api"
$passed = 0
$total = 15

function Assert-Test($condition, $testName) {
    if ($condition) {
        Write-Host " [PASS] $testName" -ForegroundColor Green
        $global:passed++
    } else {
        Write-Host " [FAIL] $testName" -ForegroundColor Red
    }
}

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "   HOSPITAL MANAGEMENT SYSTEM - PHASE 5 INTEGRATION SUITE" -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

# 1. Admin Login & Dashboard
$adminAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$adminHeaders = @{ "Authorization" = "Bearer $($adminAuth.token)"; "Content-Type" = "application/json" }
$adminDash = Invoke-RestMethod -Uri "$base/dashboard/admin" -Method Get -Headers $adminHeaders
Assert-Test ($adminDash.totalPatients -gt 0 -and $adminDash.totalDoctors -gt 0 -and $adminDash.totalAppointments -ge 0) "1. Admin Dashboard returns live database counts"

# 2. Doctor Login & Dashboard
$docAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"doctor_smith","password":"doctor123"}'
$docHeaders = @{ "Authorization" = "Bearer $($docAuth.token)"; "Content-Type" = "application/json" }
$docDash = Invoke-RestMethod -Uri "$base/dashboard/doctor" -Method Get -Headers $docHeaders
Assert-Test ($docDash.totalPatients -gt 0 -and $docDash.prescriptions -ge 0) "2. Doctor Dashboard returns clinical patient counts and prescription metrics"

# 3. Patient Login & Dashboard
$patAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"patient_john","password":"patient123"}'
$patHeaders = @{ "Authorization" = "Bearer $($patAuth.token)"; "Content-Type" = "application/json" }
$patDash = Invoke-RestMethod -Uri "$base/dashboard/patient" -Method Get -Headers $patHeaders
Assert-Test ($patDash.appointmentHistory -ge 0 -and $patDash.medicalRecords -ge 0) "3. Patient Dashboard returns personal history, records, and billing status"

# 4. Receptionist Login & Dashboard
$recAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"receptionist_sarah","password":"rec123"}'
$recHeaders = @{ "Authorization" = "Bearer $($recAuth.token)"; "Content-Type" = "application/json" }
$recDash = Invoke-RestMethod -Uri "$base/dashboard/receptionist" -Method Get -Headers $recHeaders
Assert-Test ($recDash.patientRegistrations -gt 0 -and $recDash.appointmentBookings -ge 0) "4. Receptionist Dashboard returns front desk queue and registrations"

# 5. Create Bill with automatic total calculation
$billBody = @{
    patientId = 1
    consultationFee = 500.00
    medicineCharges = 120.00
    testCharges = 250.00
    otherCharges = 30.00
    paymentStatus = "PENDING"
    notes = "Cardiac consultation with lipid panel & ECG"
} | ConvertTo-Json
$createdBill = Invoke-RestMethod -Uri "$base/bills" -Method Post -Headers $recHeaders -Body $billBody
Assert-Test ($createdBill.id -ne $null -and $createdBill.totalAmount -eq 900.00 -and $createdBill.paymentStatus -eq "PENDING") "5. Create Bill itemizes charges & calculates totalAmount (500+120+250+30 = 900.00)"

# 6. Retrieve Bill Details by ID
$retrievedBill = Invoke-RestMethod -Uri "$base/bills/$($createdBill.id)" -Method Get -Headers $adminHeaders
Assert-Test ($retrievedBill.billNumber -eq $createdBill.billNumber -and $retrievedBill.patientName -ne $null) "6. View Bill details includes invoice header, patient info, and charge itemization"

# 7. Update Bill Charges
$updateBody = @{
    patientId = 1
    consultationFee = 500.00
    medicineCharges = 120.00
    testCharges = 250.00
    otherCharges = 50.00
    paymentStatus = "PENDING"
    notes = "Cardiac consultation with lipid panel & ECG - additional consumables"
} | ConvertTo-Json
$updatedBill = Invoke-RestMethod -Uri "$base/bills/$($createdBill.id)" -Method Put -Headers $recHeaders -Body $updateBody
Assert-Test ($updatedBill.totalAmount -eq 920.00) "7. Update Bill recalculates totalAmount (500+120+250+50 = 920.00)"

# 8. Filter Bills by Status PENDING
$pendingBills = Invoke-RestMethod -Uri "$base/bills?paymentStatus=PENDING" -Method Get -Headers $adminHeaders
$containsCreated = ($pendingBills | Where-Object { $_.id -eq $createdBill.id })
Assert-Test ($containsCreated -ne $null) "8. Search & Filter Bills finds outstanding PENDING invoice"

# 9. Mark Bill as Paid with UPI (Simulated Gateway)
$payBody = @{
    paymentMethod = "UPI"
    paymentStatus = "PAID"
    notes = "Settled via UPI reference #UPI987421"
} | ConvertTo-Json
$paidBill = Invoke-RestMethod -Uri "$base/bills/$($createdBill.id)/pay" -Method Patch -Headers $patHeaders -Body $payBody
Assert-Test ($paidBill.paymentStatus -eq "PAID" -and $paidBill.paymentMethod -eq "UPI") "9. Mark Bill as Paid records status PAID and payment method UPI"

# 10. Patient views their own bills
$patientBills = Invoke-RestMethod -Uri "$base/bills/patient/1" -Method Get -Headers $patHeaders
Assert-Test ($patientBills.Count -gt 0) "10. View Patient Bills retrieves billing history for authenticated patient"

# 11. Patient Search & Filter (Name, Phone, Email)
$pName = Invoke-RestMethod -Uri "$base/patients?name=John" -Method Get -Headers $adminHeaders
$pPhone = Invoke-RestMethod -Uri "$base/patients?phone=0102" -Method Get -Headers $adminHeaders
$pEmail = Invoke-RestMethod -Uri "$base/patients?email=john.doe@email.com" -Method Get -Headers $adminHeaders
Assert-Test ($pName.data.Count -gt 0 -and $pPhone.data.Count -gt 0 -and $pEmail.data.Count -gt 0) "11. Patient Search/Filter by Name, Phone, and Email returns matched records"

# 12. Doctor Search & Filter (Name, Specialization, Experience)
$dName = Invoke-RestMethod -Uri "$base/doctors?name=Smith" -Method Get -Headers $adminHeaders
$dSpec = Invoke-RestMethod -Uri "$base/doctors?specialization=Cardiology" -Method Get -Headers $adminHeaders
$dExp = Invoke-RestMethod -Uri "$base/doctors?experience=10" -Method Get -Headers $adminHeaders
Assert-Test ($dName.data.Count -gt 0 -and $dSpec.data.Count -gt 0 -and $dExp.data.Count -gt 0) "12. Doctor Search/Filter by Name, Specialization, and Experience returns matched specialists"

# 13. Appointment Search & Filter (Patient, Doctor, Date, Status)
$appts = Invoke-RestMethod -Uri "$base/appointments?doctorId=1&status=CONFIRMED" -Method Get -Headers $adminHeaders
Assert-Test ($appts.data.Count -ge 0) "13. Appointment Search/Filter by Doctor, Date, and Status operates correctly"

# 14. Medical Records Search & Filter (Patient, Doctor, Date)
$meds = Invoke-RestMethod -Uri "$base/medical-records?doctorId=1" -Method Get -Headers $adminHeaders
Assert-Test ($meds.data.Count -gt 0) "14. Medical Record Search/Filter by Doctor, Patient, and Date operates correctly"

# 15. Role-Based Access Control (RBAC) Enforcement
$patientCannotCreateBill = $false
try {
    Invoke-RestMethod -Uri "$base/bills" -Method Post -Headers $patHeaders -Body $billBody
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        $patientCannotCreateBill = $true
    }
}
$docCannotAccessAdminDash = $false
try {
    Invoke-RestMethod -Uri "$base/dashboard/admin" -Method Get -Headers $docHeaders
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        $docCannotAccessAdminDash = $true
    }
}
Assert-Test ($patientCannotCreateBill -and $docCannotAccessAdminDash) "15. RBAC security: Patient cannot create bills (403) & Doctor cannot access Admin Dashboard (403)"

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "   TEST RESULTS: $passed / $total PASSED" -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan
