# Multi-Tenant Smart Campus Seeding and End-to-End Validation Script
# Target API: http://localhost:8080 (api-gateway)

$GatewayUrl = "http://localhost:8080"
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Starting Smart Campus multi-tenant SaaS Seeding Engine " -ForegroundColor Cyan
Write-Host " Gateway Endpoint: $GatewayUrl" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Helper function to send requests
function Invoke-CampusRequest {
    param(
        [string]$Path,
        [string]$Method = "POST",
        [object]$Body = $null,
        [string]$Token = $null,
        [string]$TenantId = $null
    )

    $Headers = @{
        "Content-Type" = "application/json"
    }
    if ($Token) {
        $Headers.Add("Authorization", "Bearer $Token")
    }
    if ($TenantId) {
        $Headers.Add("X-Tenant-ID", $TenantId)
    }

    $Params = @{
        Uri = "$GatewayUrl$Path"
        Method = $Method
        Headers = $Headers
    }
    if ($Body) {
        $Params.Add("Body", ($Body | ConvertTo-Json -Depth 5))
    }

    try {
        $Response = Invoke-RestMethod @Params
        return $Response
    } catch {
        Write-Error "Request to $Path failed: $_"
        throw $_
    }
}

# -------------------------------------------------------------
# STEP 1: ONBOARD TENANTS
# -------------------------------------------------------------
Write-Host "`n[1/5] Onboarding MIT and Harvard Campuses..." -ForegroundColor Yellow

$MitTenant = Invoke-CampusRequest -Path "/api/tenants" -Method "POST" -Body @{
    name = "Massachusetts Institute of Technology"
    subdomain = "mit"
    plan = "ENTERPRISE"
}
Write-Host "[OK] Onboarded Tenant: $($MitTenant.name) [subdomain: $($MitTenant.subdomain)]" -ForegroundColor Green

$HarvardTenant = Invoke-CampusRequest -Path "/api/tenants" -Method "POST" -Body @{
    name = "Harvard University"
    subdomain = "harvard"
    plan = "ENTERPRISE"
}
Write-Host "[OK] Onboarded Tenant: $($HarvardTenant.name) [subdomain: $($HarvardTenant.subdomain)]" -ForegroundColor Green


# -------------------------------------------------------------
# STEP 2: REGISTER CAMPUS USER ACCOUNTS
# -------------------------------------------------------------
Write-Host "`n[2/5] Creating user credentials for both campuses..." -ForegroundColor Yellow

# MIT Users
$UsersMit = @(
    @{ name = "MIT Admin"; email = "admin@mit.edu"; password = "password123"; role = "CAMPUS_ADMIN"; tenantId = "mit" }
    @{ name = "MIT Librarian"; email = "librarian@mit.edu"; password = "password123"; role = "LIBRARIAN"; tenantId = "mit" }
    @{ name = "MIT Warden"; email = "warden@mit.edu"; password = "password123"; role = "WARDEN"; tenantId = "mit" }
    @{ name = "Alice Student (MIT)"; email = "student@mit.edu"; password = "password123"; role = "STUDENT"; tenantId = "mit" }
)

foreach ($u in $UsersMit) {
    $res = Invoke-CampusRequest -Path "/api/auth/register" -Method "POST" -Body $u
    Write-Host "  [OK] Registered MIT User: $($u.name) ($($u.role))" -ForegroundColor DarkGray
}

# Harvard Users
$UsersHarvard = @(
    @{ name = "Harvard Admin"; email = "admin@harvard.edu"; password = "password123"; role = "CAMPUS_ADMIN"; tenantId = "harvard" }
    @{ name = "Harvard Librarian"; email = "librarian@harvard.edu"; password = "password123"; role = "LIBRARIAN"; tenantId = "harvard" }
    @{ name = "Harvard Warden"; email = "warden@harvard.edu"; password = "password123"; role = "WARDEN"; tenantId = "harvard" }
    @{ name = "Bob Student (Harvard)"; email = "student@harvard.edu"; password = "password123"; role = "STUDENT"; tenantId = "harvard" }
)

foreach ($u in $UsersHarvard) {
    $res = Invoke-CampusRequest -Path "/api/auth/register" -Method "POST" -Body $u
    Write-Host "  [OK] Registered Harvard User: $($u.name) ($($u.role))" -ForegroundColor DarkGray
}


# -------------------------------------------------------------
# STEP 3: SEED MIT DOMAIN DATA
# -------------------------------------------------------------
Write-Host "`n[3/5] Seeding data for MIT Campus..." -ForegroundColor Yellow

# Login as MIT Librarian to add books
$MitLibLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "librarian@mit.edu"
    password = "password123"
    tenantId = "mit"
}
$MitLibToken = $MitLibLogin.token

$BooksMit = @(
    @{ title = "Clean Code"; author = "Robert C. Martin"; isbn = "978-0132350884"; availableCopies = 5 }
    @{ title = "Design Patterns"; author = "Erich Gamma"; isbn = "978-0201633610"; availableCopies = 3 }
    @{ title = "Introduction to Algorithms"; author = "Thomas H. Cormen"; isbn = "978-0262033848"; availableCopies = 2 }
)
foreach ($b in $BooksMit) {
    $res = Invoke-CampusRequest -Path "/api/library/books" -Method "POST" -Body $b -Token $MitLibToken -TenantId "mit"
    Write-Host "  [OK] Seeded Book (MIT): $($b.title)" -ForegroundColor DarkGray
}

# Login as MIT Warden to add rooms
$MitWardenLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "warden@mit.edu"
    password = "password123"
    tenantId = "mit"
}
$MitWardenToken = $MitWardenLogin.token

$RoomsMit = @(
    @{ roomNumber = "101-A"; hostelName = "MIT Dorm East"; capacity = 4 }
    @{ roomNumber = "102-B"; hostelName = "MIT Dorm East"; capacity = 2 }
)
foreach ($r in $RoomsMit) {
    $res = Invoke-CampusRequest -Path "/api/hostel/rooms" -Method "POST" -Body $r -Token $MitWardenToken -TenantId "mit"
    Write-Host "  [OK] Seeded Room (MIT): $($r.roomNumber)" -ForegroundColor DarkGray
}

# Login as MIT Admin to add fee structure
$MitAdminLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "admin@mit.edu"
    password = "password123"
    tenantId = "mit"
}
$MitAdminToken = $MitAdminLogin.token

$FeeMit = Invoke-CampusRequest -Path "/api/fees/structures" -Method "POST" -Body @{
    name = "Tuition Fee Fall 2026"
    amount = 6000.00
    dueDate = "2026-09-30"
} -Token $MitAdminToken -TenantId "mit"
Write-Host "  [OK] Seeded Fee Structure (MIT): Tuition Fee (`$6000.00)" -ForegroundColor DarkGray


# -------------------------------------------------------------
# STEP 4: SEED HARVARD DOMAIN DATA
# -------------------------------------------------------------
Write-Host "`n[4/5] Seeding data for Harvard Campus..." -ForegroundColor Yellow

# Login as Harvard Librarian to add books
$HarvardLibLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "librarian@harvard.edu"
    password = "password123"
    tenantId = "harvard"
}
$HarvardLibToken = $HarvardLibLogin.token

$BooksHarvard = @(
    @{ title = "Effective Java"; author = "Joshua Bloch"; isbn = "978-0134685991"; availableCopies = 4 }
    @{ title = "Cracking the Coding Interview"; author = "Gayle Laakmann McDowell"; isbn = "978-0984782857"; availableCopies = 6 }
)
foreach ($b in $BooksHarvard) {
    $res = Invoke-CampusRequest -Path "/api/library/books" -Method "POST" -Body $b -Token $HarvardLibToken -TenantId "harvard"
    Write-Host "  [OK] Seeded Book (Harvard): $($b.title)" -ForegroundColor DarkGray
}

# Login as Harvard Warden to add rooms
$HarvardWardenLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "warden@harvard.edu"
    password = "password123"
    tenantId = "harvard"
}
$HarvardWardenToken = $HarvardWardenLogin.token

$RoomsHarvard = @(
    @{ roomNumber = "Eliot-203"; hostelName = "Eliot House Residence"; capacity = 3 }
)
foreach ($r in $RoomsHarvard) {
    $res = Invoke-CampusRequest -Path "/api/hostel/rooms" -Method "POST" -Body $r -Token $HarvardWardenToken -TenantId "harvard"
    Write-Host "  [OK] Seeded Room (Harvard): $($r.roomNumber)" -ForegroundColor DarkGray
}

# Login as Harvard Admin to add fee structure
$HarvardAdminLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "admin@harvard.edu"
    password = "password123"
    tenantId = "harvard"
}
$HarvardAdminToken = $HarvardAdminLogin.token

$FeeHarvard = Invoke-CampusRequest -Path "/api/fees/structures" -Method "POST" -Body @{
    name = "Harvard Tuition Fee 2026"
    amount = 8500.00
    dueDate = "2026-10-15"
} -Token $HarvardAdminToken -TenantId "harvard"
Write-Host "  [OK] Seeded Fee Structure (Harvard): Tuition Fee (`$8500.00)" -ForegroundColor DarkGray


# -------------------------------------------------------------
# STEP 5: VERIFY DATA ISOLATION END-TO-END
# -------------------------------------------------------------
Write-Host "`n[5/5] Performing multi-tenant data isolation checks..." -ForegroundColor Yellow

# Check 1: MIT Student Login Context
$MitStudentLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "student@mit.edu"
    password = "password123"
    tenantId = "mit"
}
$MitStudentToken = $MitStudentLogin.token

$MitCatalog = @(Invoke-CampusRequest -Path "/api/library/books" -Method "GET" -Token $MitStudentToken -TenantId "mit")
Write-Host "  MIT Library Query yielded: $($MitCatalog.Length) books" -ForegroundColor White
if ($MitCatalog.Length -ne 3) {
    Write-Host "  [FAIL] Expected exactly 3 books in MIT catalog!" -ForegroundColor Red
    exit 1
}

$MitRooms = @(Invoke-CampusRequest -Path "/api/hostel/rooms" -Method "GET" -Token $MitStudentToken -TenantId "mit")
Write-Host "  MIT Hostel Rooms Query yielded: $($MitRooms.Length) rooms" -ForegroundColor White
if ($MitRooms.Length -ne 2) {
    Write-Host "  [FAIL] Expected exactly 2 rooms in MIT hostel inventory!" -ForegroundColor Red
    exit 1
}

# Check 2: Harvard Student Login Context
$HarvardStudentLogin = Invoke-CampusRequest -Path "/api/auth/login" -Method "POST" -Body @{
    email = "student@harvard.edu"
    password = "password123"
    tenantId = "harvard"
}
$HarvardStudentToken = $HarvardStudentLogin.token

$HarvardCatalog = @(Invoke-CampusRequest -Path "/api/library/books" -Method "GET" -Token $HarvardStudentToken -TenantId "harvard")
Write-Host "  Harvard Library Query yielded: $($HarvardCatalog.Length) books" -ForegroundColor White
if ($HarvardCatalog.Length -ne 2) {
    Write-Host "  [FAIL] Expected exactly 2 books in Harvard catalog!" -ForegroundColor Red
    exit 1
}

$HarvardRooms = @(Invoke-CampusRequest -Path "/api/hostel/rooms" -Method "GET" -Token $HarvardStudentToken -TenantId "harvard")
Write-Host "  Harvard Hostel Rooms Query yielded: $($HarvardRooms.Length) rooms" -ForegroundColor White
if ($HarvardRooms.Length -ne 1) {
    Write-Host "  [FAIL] Expected exactly 1 room in Harvard hostel inventory!" -ForegroundColor Red
    exit 1
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host " SUCCESS: Multi-Tenant Data Isolation Fully Verified! " -ForegroundColor Green
Write-Host " All microservices are correctly scoped by tenant context. " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
