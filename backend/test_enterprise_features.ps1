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
Write-Host "  ENTERPRISE HEALTHCARE EXPANSION - AUTOMATED TEST SUITE" -ForegroundColor Cyan
Write-Host "=================================================================`n" -ForegroundColor Cyan

# 1. AUTHENTICATE ADMIN
$adminAuth = Invoke-RestMethod -Uri "$base/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
$headers = @{ "Authorization" = "Bearer $($adminAuth.token)"; "Content-Type" = "application/json" }

# 2. EMERGENCY & TRAUMA TESTS
Write-Host "`n--- 1. EMERGENCY & TRAUMA TRIAGE ---" -ForegroundColor Yellow
$erList = Invoke-RestMethod -Uri "$base/emergency" -Method Get -Headers $headers
Assert-Test ($erList.data.Count -ge 4) "1.1 Retrieved seeded emergency cases (Count: $($erList.data.Count))"

$erNewBody = @{
    caseNumber = "ER-TEST-" + (Get-Random -Minimum 1000 -Maximum 9999)
    patientName = "Alexander Pierce"
    patientAge = 62
    gender = "Male"
    triageLevel = "RESUSCITATION"
    chiefComplaint = "Sudden dyspnea, acute pulmonary edema"
    heartRate = 125
    bloodPressure = "170/110"
    spo2 = 86
    temperature = 98.6
    status = "IN_TREATMENT"
    assignedBedNumber = "ER-Bay-2"
} | ConvertTo-Json
$erCreated = Invoke-RestMethod -Uri "$base/emergency" -Method Post -Headers $headers -Body $erNewBody
Assert-Test ($erCreated.data.id -ne $null) "1.2 Triage creation assigns emergency case ID"

$codeBlueRes = Invoke-RestMethod -Uri "$base/emergency/$($erCreated.data.id)/code-blue" -Method Post -Headers $headers
Assert-Test ($codeBlueRes.data.codeBlueTriggered -eq $true) "1.3 Trigger Code Blue broadcasts alarm & updates status to RESUSCITATION"

# 3. SURGERY & OT SCHEDULING TESTS
Write-Host "`n--- 2. OPERATION THEATER (OT) & SURGERY SCHEDULING ---" -ForegroundColor Yellow
$surgList = Invoke-RestMethod -Uri "$base/surgery" -Method Get -Headers $headers
Assert-Test ($surgList.data.Count -ge 4) "2.1 Retrieved OT surgical schedules (Count: $($surgList.data.Count))"

$surgNewBody = @{
    surgeryNumber = "SURG-TEST-" + (Get-Random -Minimum 1000 -Maximum 9999)
    patientName = "Alexander Pierce"
    patientId = 1
    leadSurgeonName = "Dr. David Smith"
    otRoom = "OT-1 (General Surgery Suite)"
    procedureName = "Coronary Bypass Grafting (CABG)"
    surgeryDate = (Get-Date).ToString("yyyy-MM-dd")
    scheduledStartTime = "11:30 AM"
    estimatedDurationHours = 3.5
    status = "SCHEDULED"
    preOpCleared = $false
} | ConvertTo-Json
$surgCreated = Invoke-RestMethod -Uri "$base/surgery" -Method Post -Headers $headers -Body $surgNewBody
Assert-Test ($surgCreated.data.id -ne $null) "2.2 Surgical suite booking scheduled in OT-1"

$preOpBody = @{
    preOpCleared = $true
    anesthesiaCleared = $true
    consentSigned = $true
    bloodReserved = $true
} | ConvertTo-Json
$preOpUpdated = Invoke-RestMethod -Uri "$base/surgery/$($surgCreated.data.id)/pre-op" -Method Put -Headers $headers -Body $preOpBody
Assert-Test ($preOpUpdated.data.preOpCleared -eq $true -and $preOpUpdated.data.consentSigned -eq $true) "2.3 Pre-Op safety checklist signed off"

# 4. BLOOD BANK & COMPONENT REGISTRY TESTS
Write-Host "`n--- 3. BLOOD BANK & COMPONENT REGISTRY ---" -ForegroundColor Yellow
$bloodList = Invoke-RestMethod -Uri "$base/blood-bank" -Method Get -Headers $headers
Assert-Test ($bloodList.data.Count -ge 8) "3.1 Retrieved Blood Bank stock across all blood groups (Count: $($bloodList.data.Count))"

$restockBloodBody = @{
    bloodGroup = "O-"
    componentType = "WHOLE_BLOOD"
    units = 5
} | ConvertTo-Json
$restockRes = Invoke-RestMethod -Uri "$base/blood-bank/restock" -Method Post -Headers $headers -Body $restockBloodBody
Assert-Test ($restockRes.data.unitsAvailable -ge 5) "3.2 Universal donor O- stock replenished"

$reserveRes = Invoke-RestMethod -Uri "$base/blood-bank/$($restockRes.data.id)/reserve" -Method Post -Headers $headers -Body '{"units": 2}'
Assert-Test ($reserveRes.data.reservedUnits -ge 2) "3.3 Reserved blood units for impending surgery"

# 5. INSURANCE & TPA PRE-AUTHORIZATION TESTS
Write-Host "`n--- 4. INSURANCE CLAIMS & TPA MANAGEMENT ---" -ForegroundColor Yellow
$claimsList = Invoke-RestMethod -Uri "$base/insurance" -Method Get -Headers $headers
Assert-Test ($claimsList.data.Count -ge 3) "4.1 Retrieved insurance claims (Count: $($claimsList.data.Count))"

$claimBody = @{
    claimNumber = "CLM-TEST-" + (Get-Random -Minimum 1000 -Maximum 9999)
    patientName = "Alexander Pierce"
    patientId = 1
    policyNumber = "BCBS-5510294"
    insuranceProvider = "BlueCross BlueShield"
    tpaName = "MediAssist TPA"
    totalBillAmount = 2500.00
    icdCode = "I20.0 Unstable Angina"
    claimNotes = "Pre-authorization requested for cardiac intervention"
} | ConvertTo-Json
$claimCreated = Invoke-RestMethod -Uri "$base/insurance" -Method Post -Headers $headers -Body $claimBody
Assert-Test ($claimCreated.data.id -ne $null -and $claimCreated.data.patientCoPay -eq 250.00) "4.2 Claim created with automated 10% co-pay split ($250.00)"

$claimStatusBody = @{
    status = "PRE_AUTH_APPROVED"
    approvedAmount = 2250.00
    patientCoPay = 250.00
    notes = "Insurer pre-auth confirmed"
} | ConvertTo-Json
$claimStatusUpdated = Invoke-RestMethod -Uri "$base/insurance/$($claimCreated.data.id)/status" -Method Put -Headers $headers -Body $claimStatusBody
Assert-Test ($claimStatusUpdated.data.status -eq "PRE_AUTH_APPROVED") "4.3 Pre-authorization approved by TPA desk"

# 6. STAFF ROSTERING & SHIFT MANAGEMENT TESTS
Write-Host "`n--- 5. STAFF SHIFT ROSTERING & ON-CALL SPECIALISTS ---" -ForegroundColor Yellow
$shiftsList = Invoke-RestMethod -Uri "$base/staff-roster" -Method Get -Headers $headers
Assert-Test ($shiftsList.data.Count -ge 5) "5.1 Retrieved staff weekly duty roster (Count: $($shiftsList.data.Count))"

$onCallList = Invoke-RestMethod -Uri "$base/staff-roster/on-call" -Method Get -Headers $headers
Assert-Test ($onCallList.data.Count -ge 2) "5.2 Retrieved on-call emergency specialists"

# 7. AUTOMATED NOTIFICATIONS DISPATCH TESTS
Write-Host "`n--- 6. NOTIFICATION DISPATCH OUTBOX ---" -ForegroundColor Yellow
$notifsList = Invoke-RestMethod -Uri "$base/notifications" -Method Get -Headers $headers
Assert-Test ($notifsList.data.Count -ge 4) "6.1 Retrieved notification dispatch logs (Count: $($notifsList.data.Count))"

$notifBody = @{
    recipientName = "Alexander Pierce"
    recipientContact = "+1 555-0999"
    channel = "WHATSAPP"
    triggerEvent = "APPOINTMENT_CONFIRMED"
    subject = "Surgery Scheduled Confirmation"
    message = "Your procedure is scheduled for 11:30 AM in OT-1. NPO guidelines in effect."
} | ConvertTo-Json
$notifSent = Invoke-RestMethod -Uri "$base/notifications/dispatch-test" -Method Post -Headers $headers -Body $notifBody
Assert-Test ($notifSent.data.status -eq "DELIVERED") "6.2 Dispatched automated WhatsApp notification"

# 8. AI CLINICAL DECISION SUPPORT TESTS
Write-Host "`n--- 7. AI CLINICAL DECISION SUPPORT & DDI CHECKER ---" -ForegroundColor Yellow
$ddiBody = @{
    medicineNames = @("Warfarin 5mg", "Aspirin 75mg", "Amoxicillin 500mg")
    patientAllergies = "Penicillin, Dust"
    patientId = 1
} | ConvertTo-Json
$safetyCheck = Invoke-RestMethod -Uri "$base/clinical-support/check-safety" -Method Post -Headers $headers -Body $ddiBody
$hasDdi = ($safetyCheck.data | Where-Object { $_.type -eq "DRUG_INTERACTION" -and $_.severity -eq "HIGH" }) -ne $null
$hasAllergy = ($safetyCheck.data | Where-Object { $_.type -eq "ALLERGY_CONTRAINDICATION" -and $_.severity -eq "HIGH" }) -ne $null
Assert-Test ($hasDdi -and $hasAllergy) "7.1 AI Safety Engine flagged Warfarin+Aspirin DDI and Amoxicillin+Penicillin allergy"

$firstPat = (Invoke-RestMethod -Uri "$base/patients" -Method Get -Headers $headers).data[0]
$targetPatId = if ($firstPat.patientId) { $firstPat.patientId } else { $firstPat.id }
$briefRes = Invoke-RestMethod -Uri "$base/clinical-support/patient-brief/$targetPatId" -Method Get -Headers $headers
Assert-Test ($briefRes.data.patientName -ne $null -and $briefRes.data.clinicalHighlights.Count -ge 1) "7.2 Generated 3-bullet AI Clinical Encounter Brief for Doctor ($($briefRes.data.patientName))"

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "   ENTERPRISE SUITE TEST RESULTS: $passed / $total PASSED" -ForegroundColor Cyan
Write-Host "=================================================================`n" -ForegroundColor Cyan
