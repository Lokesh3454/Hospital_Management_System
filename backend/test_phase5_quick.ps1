$base = "http://localhost:8080/api"

# 1. Login Admin
$adminAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$adminToken = $adminAuth.token
$adminHeaders = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}
Write-Host "Admin login successful. Token acquired."

# 2. Get Admin Dashboard
$adminDash = Invoke-RestMethod -Uri "$base/dashboard/admin" -Method Get -Headers $adminHeaders
Write-Host "`n=== ADMIN DASHBOARD METRICS ==="
Write-Host "Total Patients:" $adminDash.totalPatients
Write-Host "Total Doctors:" $adminDash.totalDoctors
Write-Host "Total Appointments:" $adminDash.totalAppointments
Write-Host "Pending Bills:" $adminDash.pendingBills
Write-Host "Pending Bills Amount: $" $adminDash.pendingBillsAmount
Write-Host "Today Appointments:" $adminDash.todayAppointments

# 3. Get Doctor Dashboard
$docAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"doctor_smith","password":"doctor123"}'
$docHeaders = @{
    "Authorization" = "Bearer $($docAuth.token)"
    "Content-Type" = "application/json"
}
$docDash = Invoke-RestMethod -Uri "$base/dashboard/doctor" -Method Get -Headers $docHeaders
Write-Host "`n=== DOCTOR DASHBOARD METRICS ==="
Write-Host "Today Appointments:" $docDash.todayAppointments
Write-Host "Upcoming Appointments:" $docDash.upcomingAppointments
Write-Host "Total Patients:" $docDash.totalPatients
Write-Host "Recent Records:" $docDash.recentMedicalRecords
Write-Host "Prescriptions:" $docDash.prescriptions

# 4. Get Patient Dashboard
$patAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"patient_john","password":"patient123"}'
$patHeaders = @{
    "Authorization" = "Bearer $($patAuth.token)"
    "Content-Type" = "application/json"
}
$patDash = Invoke-RestMethod -Uri "$base/dashboard/patient" -Method Get -Headers $patHeaders
Write-Host "`n=== PATIENT DASHBOARD METRICS ==="
Write-Host "Appointment History:" $patDash.appointmentHistory
Write-Host "Medical Records:" $patDash.medicalRecords
Write-Host "Prescriptions:" $patDash.prescriptions
Write-Host "Pending Bills:" $patDash.pendingBills

# 5. Get Receptionist Dashboard
$recAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"receptionist_sarah","password":"rec123"}'
$recHeaders = @{
    "Authorization" = "Bearer $($recAuth.token)"
    "Content-Type" = "application/json"
}
$recDash = Invoke-RestMethod -Uri "$base/dashboard/receptionist" -Method Get -Headers $recHeaders
Write-Host "`n=== RECEPTIONIST DASHBOARD METRICS ==="
Write-Host "Today Appointments:" $recDash.todayAppointments
Write-Host "Patient Registrations:" $recDash.patientRegistrations
Write-Host "Appointment Bookings:" $recDash.appointmentBookings
Write-Host "Pending Bills:" $recDash.pendingBills

# 6. List and Create Bill
$newBillBody = @{
    patientId = 1
    consultationFee = 600.00
    medicineCharges = 150.00
    testCharges = 200.00
    otherCharges = 50.00
    paymentStatus = "PENDING"
    notes = "Pre-op cardiac consultation"
} | ConvertTo-Json

$createdBill = Invoke-RestMethod -Uri "$base/bills" -Method Post -Headers $adminHeaders -Body $newBillBody
Write-Host "`n=== CREATED BILL ==="
Write-Host "Bill Number:" $createdBill.billNumber
Write-Host "Total Amount (expected 1000.00):" $createdBill.totalAmount
Write-Host "Payment Status:" $createdBill.paymentStatus

# 7. Mark Bill as Paid with UPI
$payBody = @{
    paymentMethod = "UPI"
    notes = "Paid via Google Pay"
} | ConvertTo-Json
$paidBill = Invoke-RestMethod -Uri "$base/bills/$($createdBill.id)/pay" -Method Patch -Headers $adminHeaders -Body $payBody
Write-Host "`n=== PAID BILL ==="
Write-Host "Updated Status:" $paidBill.paymentStatus
Write-Host "Payment Method:" $paidBill.paymentMethod
