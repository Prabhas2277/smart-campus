// Global State Manager
const state = {
    activeTenant: localStorage.getItem('activeTenant') || '',
    jwtToken: localStorage.getItem('jwtToken') || '',
    user: JSON.parse(localStorage.getItem('currentUser')) || null
};

// DOM Elements
const sections = {
    onboard: document.getElementById('section-onboard'),
    login: document.getElementById('section-login'),
    dashboard: document.getElementById('section-dashboard')
};

const navItems = {
    onboard: document.getElementById('nav-onboard'),
    login: document.getElementById('nav-login'),
    dashboard: document.getElementById('nav-dashboard')
};

// Toast Notifications Helper
function showToast(message, type = 'success') {
    const box = document.getElementById('toast-box');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? '✓' : '✗';
    toast.innerHTML = `<span class="toast-icon">${icon}</span> <span>${message}</span>`;
    
    box.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse forwards';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Fetch Wrap Helper with JWT and Tenant Header injection
async function apiRequest(url, method = 'GET', body = null, bypassAuth = false) {
    const headers = {
        'Content-Type': 'application/json'
    };

    if (state.jwtToken && !bypassAuth) {
        headers['Authorization'] = `Bearer ${state.jwtToken}`;
    }

    if (state.activeTenant) {
        headers['X-Tenant-ID'] = state.activeTenant;
    }

    const config = { method, headers };
    if (body) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(url, config);
        
        if (response.status === 401) {
            handleLogout();
            showToast('Session expired. Please log in again.', 'error');
            throw new Error('Unauthorized');
        }

        const text = await response.text();
        if (!response.ok) {
            let errorMsg = text || 'An error occurred';
            try {
                const parsed = JSON.parse(text);
                errorMsg = parsed.message || parsed.error || errorMsg;
            } catch (e) {}
            throw new Error(errorMsg);
        }

        return text ? JSON.parse(text) : null;
    } catch (e) {
        console.error(`API Error on ${url}:`, e);
        throw e;
    }
}

// View Routing Router
function navigateTo(sectionKey) {
    Object.keys(sections).forEach(key => {
        if (key === sectionKey) {
            sections[key].classList.add('active');
            navItems[key].classList.add('active');
        } else {
            sections[key].classList.remove('active');
            navItems[key].classList.remove('active');
        }
    });

    // Update Titles
    const title = document.getElementById('view-title');
    const subtitle = document.getElementById('view-subtitle');

    if (sectionKey === 'onboard') {
        title.innerText = 'Onboard a New Campus';
        subtitle.innerText = 'Onboard new colleges and universities onto the smart campus multi-tenant SaaS grid.';
    } else if (sectionKey === 'login') {
        title.innerText = 'Campus Gateway Authentication';
        subtitle.innerText = 'Verify your digital ID credentials to log into your tenant campus workspace.';
    } else if (sectionKey === 'dashboard') {
        title.innerText = `${state.user.role.replace('_', ' ')} Control Dashboard`;
        subtitle.innerText = `Welcome back, ${state.user.name}. Currently connected to ${state.activeTenant.toUpperCase()} academic workspace.`;
    }
}

// -------------------------------------------------------------
// View Renderers: Role-Based Dashboards
// -------------------------------------------------------------

async function renderDashboard() {
    const container = document.getElementById('role-dashboard-container');
    container.innerHTML = '<div class="card glass text-center">Loading dashboard resources...</div>';

    const role = state.user.role;

    if (role === 'STUDENT') {
        await renderStudentDashboard(container);
    } else if (role === 'LIBRARIAN') {
        await renderLibrarianDashboard(container);
    } else if (role === 'WARDEN') {
        await renderWardenDashboard(container);
    } else if (role === 'CAMPUS_ADMIN') {
        await renderAdminDashboard(container);
    } else {
        container.innerHTML = `<div class="card glass">Unknown Role: ${role}</div>`;
    }
}

// 1. Student Dashboard
async function renderStudentDashboard(container) {
    try {
        // Fetch student specific data from services concurrently
        const [attendanceRecords, books, myPayments, feesBalance] = await Promise.all([
            apiRequest(`/api/attendance/student/${state.user.id}`).catch(() => []),
            apiRequest('/api/library/books').catch(() => []),
            apiRequest(`/api/fees/student/${state.user.id}`).catch(() => ({ payments: [], balance: 0 })),
            apiRequest(`/api/fees/student/${state.user.id}`).catch(() => 0) // Retrieves student balance directly
        ]);

        const balance = myPayments.balance !== undefined ? myPayments.balance : 0;
        const paymentsList = myPayments.payments || [];

        // Calculate attendance rate
        const totalClasses = attendanceRecords.length;
        const presentClasses = attendanceRecords.filter(r => r.status === 'PRESENT').length;
        const attendanceRate = totalClasses > 0 ? Math.round((presentClasses / totalClasses) * 100) : 0;

        container.innerHTML = `
            <div class="dashboard-header-summary">
                <div class="summary-tile">
                    <span class="summary-title">Attendance Rate</span>
                    <span class="summary-value ${attendanceRate >= 75 ? 'text-success' : 'text-danger'}">${attendanceRate}%</span>
                </div>
                <div class="summary-tile">
                    <span class="summary-title">Classes Attended</span>
                    <span class="summary-value">${presentClasses}/${totalClasses}</span>
                </div>
                <div class="summary-tile">
                    <span class="summary-title">Pending Tuition Fee</span>
                    <span class="summary-value ${balance > 0 ? 'text-danger' : 'text-success'}">$${Number(balance).toFixed(2)}</span>
                </div>
                <div class="summary-tile">
                    <span class="summary-title">Receipts Issued</span>
                    <span class="summary-value">${paymentsList.length}</span>
                </div>
            </div>

            <div class="dashboard-grid">
                <!-- Left Column -->
                <div class="flex-column gap-24">
                    <!-- Fee Tuition Payment Widget -->
                    <div class="card glass">
                        <h3>Tuition Fee Clearance Portal</h3>
                        <p class="text-secondary-sm" style="margin-bottom: 20px;">Clear active balances. All transactions map to the isolated billing engine.</p>
                        
                        <div class="grid grid-2" style="margin-bottom: 20px;">
                            <div class="summary-tile" style="background: rgba(255,255,255,0.01)">
                                <span class="summary-title">Current Outstanding</span>
                                <span class="summary-value">$${Number(balance).toFixed(2)}</span>
                            </div>
                            <div class="summary-tile" style="background: rgba(255,255,255,0.01)">
                                <span class="summary-title">Payment Mode</span>
                                <span class="summary-value" style="font-size: 16px; margin-top: 8px;">Stateless SaaS Direct</span>
                            </div>
                        </div>

                        ${balance > 0 ? `
                            <form id="form-pay-tuition" class="grid grid-2 align-end">
                                <div class="form-group" style="margin-bottom: 0;">
                                    <label for="pay-amount">Amount to Pay ($)</label>
                                    <input type="number" id="pay-amount" value="${balance}" min="1" max="${balance}" required>
                                </div>
                                <button type="submit" class="btn btn-primary">Pay Tuition Balance</button>
                            </form>
                        ` : '<div class="status-badge active" style="display:inline-block; padding: 10px 16px;">✓ Tuition fee fully cleared. No outstanding balance.</div>'}
                    </div>

                    <!-- Library Checked out Books / Catalog -->
                    <div class="card glass">
                        <h3>Library Book Catalog</h3>
                        <div class="tenant-list-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Book Title</th>
                                        <th>Author</th>
                                        <th>ISBN</th>
                                        <th>Available Copies</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${books.length > 0 ? books.map(book => `
                                        <tr>
                                            <td><strong>${book.title}</strong></td>
                                            <td>${book.author}</td>
                                            <td><code>${book.isbn}</code></td>
                                            <td>${book.availableCopies} copies</td>
                                        </tr>
                                    `).join('') : '<tr><td colspan="4">No library catalog items found for this campus.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Right Column -->
                <div class="flex-column gap-24">
                    <!-- Attendance Logs -->
                    <div class="card glass">
                        <h3>Attendance Activity Log</h3>
                        <div class="tenant-list-container" style="max-height: 250px; overflow-y: auto;">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${attendanceRecords.length > 0 ? attendanceRecords.map(rec => `
                                        <tr>
                                            <td>${rec.date}</td>
                                            <td><span class="status-badge ${rec.status === 'PRESENT' ? 'active' : 'danger'}">${rec.status}</span></td>
                                        </tr>
                                    `).join('') : '<tr><td colspan="2">No attendance entries recorded yet.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Payment Transaction Receipts -->
                    <div class="card glass">
                        <h3>Receipts Directory</h3>
                        <div class="tenant-list-container" style="max-height: 250px; overflow-y: auto;">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Receipt #</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${paymentsList.length > 0 ? paymentsList.map(pay => `
                                        <tr>
                                            <td><code>${pay.receiptNumber}</code></td>
                                            <td><strong>$${pay.amountPaid.toFixed(2)}</strong></td>
                                        </tr>
                                    `).join('') : '<tr><td colspan="2">No payments recorded.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Add payment submit listener
        const payForm = document.getElementById('form-pay-tuition');
        if (payForm) {
            payForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const amount = document.getElementById('pay-amount').value;
                
                // Get active fee structure
                try {
                    const structures = await apiRequest('/api/fees/structures');
                    if (structures.length === 0) {
                        showToast('No fee structure defined for payment validation.', 'error');
                        return;
                    }
                    const structureId = structures[0].id;
                    
                    showToast('Initiating tuition transaction...', 'success');
                    await apiRequest('/api/fees/payments', 'POST', {
                        feeStructureId: structureId,
                        studentId: state.user.id,
                        amountPaid: Number(amount)
                    });
                    
                    showToast('Tuition payment registered successfully!', 'success');
                    renderDashboard(); // Reload view state
                } catch (err) {
                    showToast(err.message, 'error');
                }
            });
        }

    } catch (e) {
        container.innerHTML = `<div class="card glass danger">Failed to render student metrics: ${e.message}</div>`;
    }
}

// 2. Librarian Dashboard
async function renderLibrarianDashboard(container) {
    try {
        const books = await apiRequest('/api/library/books').catch(() => []);

        container.innerHTML = `
            <div class="grid grid-2">
                <!-- Add Book Form -->
                <div class="card glass">
                    <h3>Add Catalog Book</h3>
                    <form id="form-add-book">
                        <div class="form-group">
                            <label for="book-title">Title</label>
                            <input type="text" id="book-title" placeholder="e.g. Design Patterns" required>
                        </div>
                        <div class="form-group">
                            <label for="book-author">Author</label>
                            <input type="text" id="book-author" placeholder="e.g. Gang of Four" required>
                        </div>
                        <div class="form-group">
                            <label for="book-isbn">ISBN Code</label>
                            <input type="text" id="book-isbn" placeholder="e.g. 978-0201633610" required>
                        </div>
                        <div class="form-group">
                            <label for="book-copies">Copies Count</label>
                            <input type="number" id="book-copies" value="5" min="1" required>
                        </div>
                        <button type="submit" class="btn btn-primary">Add Catalog Book</button>
                    </form>
                </div>

                <!-- Books List -->
                <div class="card glass flex-column justify-between">
                    <div>
                        <h3>Campus Book Inventory</h3>
                        <div class="tenant-list-container" style="max-height: 350px; overflow-y: auto;">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Book Details</th>
                                        <th>Copies</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${books.length > 0 ? books.map(book => `
                                        <tr>
                                            <td>
                                                <strong>${book.title}</strong><br>
                                                <span class="text-secondary-sm">by ${book.author} (${book.isbn})</span>
                                            </td>
                                            <td>${book.availableCopies} copies</td>
                                        </tr>
                                    `).join('') : '<tr><td colspan="2">No books registered on catalog.</td></tr>'}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('form-add-book').addEventListener('submit', async (e) => {
            e.preventDefault();
            const body = {
                title: document.getElementById('book-title').value,
                author: document.getElementById('book-author').value,
                isbn: document.getElementById('book-isbn').value,
                availableCopies: parseInt(document.getElementById('book-copies').value)
            };

            try {
                await apiRequest('/api/library/books', 'POST', body);
                showToast('Book added successfully to catalog!', 'success');
                renderDashboard(); // reload state
            } catch (err) {
                showToast(err.message, 'error');
            }
        });

    } catch (e) {
        container.innerHTML = `<div class="card glass danger">Failed to fetch library inventory: ${e.message}</div>`;
    }
}

// 3. Warden Dashboard
async function renderWardenDashboard(container) {
    try {
        const rooms = await apiRequest('/api/hostel/rooms').catch(() => []);

        container.innerHTML = `
            <div class="grid grid-2">
                <!-- Add Room Form -->
                <div class="card glass">
                    <h3>Register Hostel Room</h3>
                    <form id="form-add-room">
                        <div class="form-group">
                            <label for="room-num">Room Number</label>
                            <input type="text" id="room-num" placeholder="e.g. 101-B" required>
                        </div>
                        <div class="form-group">
                            <label for="hostel-name">Hostel Name</label>
                            <input type="text" id="hostel-name" placeholder="e.g. West Wing Residence" required>
                        </div>
                        <div class="form-group">
                            <label for="room-cap">Occupancy Capacity</label>
                            <input type="number" id="room-cap" value="4" min="1" required>
                        </div>
                        <button type="submit" class="btn btn-primary">Add Room</button>
                    </form>
                </div>

                <!-- Rooms List -->
                <div class="card glass">
                    <h3>Hostel Inventory & Occupancy</h3>
                    <div class="tenant-list-container" style="max-height: 350px; overflow-y: auto;">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Room</th>
                                    <th>Hostel Residence</th>
                                    <th>Capacity</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${rooms.length > 0 ? rooms.map(room => `
                                    <tr>
                                        <td><strong>${room.roomNumber}</strong></td>
                                        <td>${room.hostelName}</td>
                                        <td><code>${room.occupancy}/${room.capacity} beds</code></td>
                                    </tr>
                                `).join('') : '<tr><td colspan="3">No rooms registered.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('form-add-room').addEventListener('submit', async (e) => {
            e.preventDefault();
            const body = {
                roomNumber: document.getElementById('room-num').value,
                hostelName: document.getElementById('hostel-name').value,
                capacity: parseInt(document.getElementById('room-cap').value)
            };

            try {
                await apiRequest('/api/hostel/rooms', 'POST', body);
                showToast('Hostel room created!', 'success');
                renderDashboard();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });

    } catch (e) {
        container.innerHTML = `<div class="card glass danger">Failed to fetch rooms: ${e.message}</div>`;
    }
}

// 4. Admin Dashboard
async function renderAdminDashboard(container) {
    try {
        const [users, structures] = await Promise.all([
            apiRequest('/api/auth/users').catch(() => []),
            apiRequest('/api/fees/structures').catch(() => [])
        ]);

        container.innerHTML = `
            <div class="grid grid-2">
                <!-- Configure Tuition Fees Structure Form -->
                <div class="card glass">
                    <h3>Configure Tuition Structure</h3>
                    <form id="form-add-structure">
                        <div class="form-group">
                            <label for="struct-name">Fee Session Name</label>
                            <input type="text" id="struct-name" placeholder="e.g. Tuition Fee - FY 2026" required>
                        </div>
                        <div class="form-group">
                            <label for="struct-amount">Amount ($)</label>
                            <input type="number" id="struct-amount" placeholder="e.g. 5000" min="1" required>
                        </div>
                        <div class="form-group">
                            <label for="struct-due">Due Date</label>
                            <input type="date" id="struct-due" required>
                        </div>
                        <button type="submit" class="btn btn-primary">Define Fee Structure</button>
                    </form>

                    <h4 style="margin-top:24px; margin-bottom:12px;">Active Structures</h4>
                    <div class="tenant-list-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Amount</th>
                                    <th>Due</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${structures.length > 0 ? structures.map(s => `
                                    <tr>
                                        <td>${s.name}</td>
                                        <td>$${s.amount.toFixed(2)}</td>
                                        <td>${s.dueDate}</td>
                                    </tr>
                                `).join('') : '<tr><td colspan="3">No fee structures configured.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Users registered in the Tenant -->
                <div class="card glass">
                    <h3>Campus User Management</h3>
                    <p class="text-secondary-sm" style="margin-bottom:20px;">Scopes users registered within the tenant isolation layer.</p>
                    <div class="tenant-list-container" style="max-height: 400px; overflow-y: auto;">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>User Details</th>
                                    <th>Workspace Role</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${users.length > 0 ? users.map(u => `
                                    <tr>
                                        <td>
                                            <strong>${u.name}</strong><br>
                                            <span class="text-secondary-sm">${u.email}</span>
                                        </td>
                                        <td><span class="status-badge active">${u.role}</span></td>
                                    </tr>
                                `).join('') : '<tr><td colspan="2">No users registered on this campus.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('form-add-structure').addEventListener('submit', async (e) => {
            e.preventDefault();
            const body = {
                name: document.getElementById('struct-name').value,
                amount: parseFloat(document.getElementById('struct-amount').value),
                dueDate: document.getElementById('struct-due').value
            };

            try {
                await apiRequest('/api/fees/structures', 'POST', body);
                showToast('Fee structure defined successfully!', 'success');
                renderDashboard();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });

    } catch (e) {
        container.innerHTML = `<div class="card glass danger">Failed to fetch admin states: ${e.message}</div>`;
    }
}

// -------------------------------------------------------------
// Onboarding / Registry & Authentication Event Listeners
// -------------------------------------------------------------

async function loadTenants() {
    const tableBody = document.querySelector('#table-tenants tbody');
    tableBody.innerHTML = '<tr><td colspan="4">Loading tenants registry...</td></tr>';
    
    try {
        const tenants = await apiRequest('/api/tenants', 'GET', null, true);
        document.getElementById('stat-tenant-count').innerText = tenants.length;
        
        if (tenants.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="4">No campuses onboarded yet.</td></tr>';
            return;
        }

        tableBody.innerHTML = tenants.map(t => `
            <tr>
                <td><strong>${t.name}</strong></td>
                <td><code>${t.subdomain}</code></td>
                <td>${t.subscriptionPlan}</td>
                <td><span class="status-badge active">${t.active ? 'Active' : 'Suspended'}</span></td>
            </tr>
        `).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="4" class="text-danger">Failed to load tenants: ${err.message}</td></tr>`;
    }
}

// Form Onboarding
document.getElementById('form-onboard').addEventListener('submit', async (e) => {
    e.preventDefault();
    const body = {
        name: document.getElementById('onboard-name').value,
        subdomain: document.getElementById('onboard-subdomain').value,
        subscriptionPlan: document.getElementById('onboard-plan').value
    };

    const btn = document.getElementById('btn-submit-onboard');
    btn.disabled = true;
    btn.innerText = 'Registering...';

    try {
        await apiRequest('/api/tenants', 'POST', body, true);
        showToast('Campus Tenant onboarded successfully!', 'success');
        document.getElementById('form-onboard').reset();
        loadTenants();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Register Tenant';
    }
});

// Switch Form auth display
document.getElementById('btn-go-to-register').addEventListener('click', () => {
    document.getElementById('form-login').classList.add('hidden');
    document.getElementById('form-register').classList.remove('hidden');
});

document.getElementById('btn-back-to-login').addEventListener('click', () => {
    document.getElementById('form-register').classList.add('hidden');
    document.getElementById('form-login').classList.remove('hidden');
});

// Login Execution
document.getElementById('form-login').addEventListener('submit', async (e) => {
    e.preventDefault();
    const tenantId = document.getElementById('login-tenant').value.trim().toLowerCase();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        state.activeTenant = tenantId;
        const res = await apiRequest('/api/auth/login', 'POST', { email, password, tenantId }, true);
        
        state.jwtToken = res.token;
        state.user = {
            name: res.name,
            email: res.email,
            role: res.role,
            tenantId: res.tenantId
        };

        // Save State
        localStorage.setItem('activeTenant', state.activeTenant);
        localStorage.setItem('jwtToken', state.jwtToken);
        localStorage.setItem('currentUser', JSON.stringify(state.user));

        showToast('Authentication successful!', 'success');
        setupUserInterfaceState();
        navigateTo('dashboard');
        renderDashboard();
    } catch (err) {
        localStorage.clear();
        state.activeTenant = '';
        state.jwtToken = '';
        state.user = null;
        showToast(err.message, 'error');
    }
});

// Register Execution
document.getElementById('form-register').addEventListener('submit', async (e) => {
    e.preventDefault();
    const tenantId = document.getElementById('reg-tenant').value.trim().toLowerCase();
    const body = {
        name: document.getElementById('reg-name').value,
        email: document.getElementById('reg-email').value,
        password: document.getElementById('reg-password').value,
        role: document.getElementById('reg-role').value,
        tenantId
    };

    try {
        state.activeTenant = tenantId;
        await apiRequest('/api/auth/register', 'POST', body, true);
        showToast('Registration successful! Please log in.', 'success');
        document.getElementById('form-register').reset();
        document.getElementById('form-register').classList.add('hidden');
        document.getElementById('form-login').classList.remove('hidden');
    } catch (err) {
        showToast(err.message, 'error');
    }
});

// User Session state config UI
function setupUserInterfaceState() {
    const badge = document.getElementById('active-tenant-badge');
    const badgeName = document.getElementById('badge-tenant-name');
    const logoutBtn = document.getElementById('btn-logout-action');
    const profile = document.getElementById('profile-container');
    const hUser = document.getElementById('header-user-name');
    const hRole = document.getElementById('header-user-role');
    const avatar = document.getElementById('avatar-icon');

    if (state.jwtToken && state.user) {
        navItems.dashboard.classList.remove('hidden');
        badge.classList.remove('hidden');
        badgeName.innerText = `${state.activeTenant.toUpperCase()} Workspace`;
        logoutBtn.classList.remove('hidden');
        profile.classList.remove('hidden');
        hUser.innerText = state.user.name;
        hRole.innerText = state.user.role.replace('_', ' ');
        avatar.innerText = state.user.name.charAt(0).toUpperCase();
    } else {
        navItems.dashboard.classList.add('hidden');
        badge.classList.add('hidden');
        logoutBtn.classList.add('hidden');
        profile.classList.add('hidden');
    }
}

// Log Out Handler
function handleLogout() {
    localStorage.clear();
    state.activeTenant = '';
    state.jwtToken = '';
    state.user = null;
    setupUserInterfaceState();
    navigateTo('onboard');
    loadTenants();
}

document.getElementById('btn-logout-action').addEventListener('click', handleLogout);

// Nav Clicks
navItems.onboard.addEventListener('click', (e) => {
    e.preventDefault();
    navigateTo('onboard');
    loadTenants();
});

navItems.login.addEventListener('click', (e) => {
    e.preventDefault();
    if (state.jwtToken) {
        navigateTo('dashboard');
        renderDashboard();
    } else {
        navigateTo('login');
    }
});

navItems.dashboard.addEventListener('click', (e) => {
    e.preventDefault();
    if (state.jwtToken) {
        navigateTo('dashboard');
        renderDashboard();
    }
});

// Bootstrapper initialization
window.addEventListener('DOMContentLoaded', () => {
    setupUserInterfaceState();
    if (state.jwtToken && state.user) {
        navigateTo('dashboard');
        renderDashboard();
    } else {
        navigateTo('onboard');
        loadTenants();
    }
});
