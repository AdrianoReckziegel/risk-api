import './style.css';

type Customer = {
  id: number;
  externalId: string;
  name: string;
  email: string;
  birthDate: string | null;
  creditScore: number;
  annualIncome: number;
  createdAt: string;
};

type RiskAssessment = {
  id: number;
  customerId: number;
  assessmentDate: string;
  riskScore: number;
  riskLevel: string;
  decision: string;
  createdAt: string;
};

type LoginResponse = {
  token: string;
};

type ActiveView = 'dashboard' | 'customers' | 'reports';

type DashboardState = {
  token: string;
  customers: Customer[];
  assessments: RiskAssessment[];
  currentView: ActiveView;
  searchQuery: string;
  editingCustomer: Customer | null;
  isAddModalOpen: boolean;
  isViewOnly: boolean;
};

const STORAGE_KEY = 'risk-api-token';
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '');
const appRoot = document.querySelector<HTMLDivElement>('#app') as HTMLDivElement | null;

if (!appRoot) {
  throw new Error('Application root element not found');
}

const root = appRoot;

const DEMO_CUSTOMERS: Customer[] = [
  {
    id: 101,
    externalId: 'CUST-8401',
    name: 'Sarah Jenkins',
    email: 's.jenkins@example.com',
    birthDate: '1988-04-12',
    creditScore: 785,
    annualIncome: 125000,
    createdAt: '2026-09-15T10:00:00Z',
  },
  {
    id: 102,
    externalId: 'CUST-8402',
    name: 'Marcus Vance',
    email: 'm.vance@example.com',
    birthDate: '1992-09-23',
    creditScore: 690,
    annualIncome: 74000,
    createdAt: '2026-09-18T11:30:00Z',
  },
  {
    id: 103,
    externalId: 'CUST-8403',
    name: 'Elena Rostova',
    email: 'e.rostova@example.com',
    birthDate: '1981-11-05',
    creditScore: 820,
    annualIncome: 160000,
    createdAt: '2026-09-20T14:15:00Z',
  },
  {
    id: 104,
    externalId: 'CUST-8404',
    name: 'David Kim',
    email: 'd.kim@example.com',
    birthDate: '1995-02-18',
    creditScore: 540,
    annualIncome: 42000,
    createdAt: '2026-09-25T09:45:00Z',
  },
  {
    id: 105,
    externalId: 'CUST-8405',
    name: 'Chloe Bennett',
    email: 'c.bennett@example.com',
    birthDate: '1990-07-30',
    creditScore: 710,
    annualIncome: 89000,
    createdAt: '2026-10-01T16:20:00Z',
  },
];

const DEMO_ASSESSMENTS: RiskAssessment[] = [
  {
    id: 501,
    customerId: 101,
    assessmentDate: '2026-10-04',
    riskScore: 820,
    riskLevel: 'LOW',
    decision: 'APPROVED',
    createdAt: '2026-10-04T10:15:00Z',
  },
  {
    id: 502,
    customerId: 102,
    assessmentDate: '2026-10-04',
    riskScore: 675,
    riskLevel: 'MEDIUM',
    decision: 'REVIEW',
    createdAt: '2026-10-04T11:20:00Z',
  },
  {
    id: 503,
    customerId: 103,
    assessmentDate: '2026-10-03',
    riskScore: 845,
    riskLevel: 'LOW',
    decision: 'APPROVED',
    createdAt: '2026-10-03T14:40:00Z',
  },
  {
    id: 504,
    customerId: 104,
    assessmentDate: '2026-10-02',
    riskScore: 490,
    riskLevel: 'HIGH',
    decision: 'REJECTED',
    createdAt: '2026-10-02T16:05:00Z',
  },
  {
    id: 505,
    customerId: 105,
    assessmentDate: '2026-10-01',
    riskScore: 730,
    riskLevel: 'LOW',
    decision: 'APPROVED',
    createdAt: '2026-10-01T09:30:00Z',
  },
];

const state: DashboardState = {
  token: localStorage.getItem(STORAGE_KEY) ?? '',
  customers: [],
  assessments: [],
  currentView: 'dashboard',
  searchQuery: '',
  editingCustomer: null,
  isAddModalOpen: false,
  isViewOnly: false,
};

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers ?? {});

  if (state.token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${state.token}`);
  }

  if (!headers.has('Content-Type') && options.body && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const text = await response.text();

  if (!response.ok) {
    let message = 'Request failed';

    if (text) {
      try {
        const payload = JSON.parse(text) as { message?: string; error?: string };
        message = payload.message ?? payload.error ?? message;
      } catch {
        message = text;
      }
    }

    throw new Error(message);
  }

  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

function setToken(token: string): void {
  state.token = token;
  if (token) {
    localStorage.setItem(STORAGE_KEY, token);
    return;
  }
  localStorage.removeItem(STORAGE_KEY);
}

function showToast(message: string, type: 'success' | 'error' = 'success'): void {
  let container = document.querySelector<HTMLDivElement>('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(40px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function formatMoney(value: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(value);
}

function formatRiskLevel(level: string): string {
  return level?.toUpperCase() ?? 'UNKNOWN';
}

function formatDecision(decision: string): string {
  return decision?.toUpperCase() ?? '—';
}

function getScoreBadgeClass(score: number): string {
  if (score >= 750) return 'score-badge tier-1';
  if (score >= 650) return 'score-badge tier-2';
  return 'score-badge tier-3';
}

function getRiskSummary() {
  const summary = {
    low: 0,
    medium: 0,
    high: 0,
    approved: 0,
    review: 0,
    rejected: 0,
  };

  for (const assessment of state.assessments) {
    const level = formatRiskLevel(assessment.riskLevel);
    if (level === 'LOW') summary.low += 1;
    if (level === 'MEDIUM') summary.medium += 1;
    if (level === 'HIGH') summary.high += 1;

    const decision = assessment.decision?.toUpperCase();
    if (decision === 'APPROVED') summary.approved += 1;
    if (decision === 'REVIEW') summary.review += 1;
    if (decision === 'REJECTED') summary.rejected += 1;
  }

  return summary;
}

function renderLogin(): void {
  state.isViewOnly = false;
  root.innerHTML = `
    <div class="auth-shell">
      <div class="auth-card">
        <p class="eyebrow">Risk assessment portal</p>
        <h1>Welcome back</h1>
        <p class="subtitle">Sign in to review customer risk, upload account changes, and monitor decisions.</p>

        <form id="login-form" class="stacked-form">
          <label>
            <span>Username</span>
            <input name="username" type="text" value="admin" required />
          </label>

          <label>
            <span>Password</span>
            <input name="password" type="password" placeholder="Enter your password" required />
          </label>

          <button type="submit" class="primary-button" style="width: 100%;">Login</button>
        </form>

        <div class="auth-divider">
          <span>or explore</span>
        </div>

        <button type="button" class="secondary-button view-only-button" id="view-only-access-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
          Access as View Only
        </button>

        <div class="auth-footer">
          <span>Need access to this portal?</span>
          <button type="button" class="link-button" id="open-request-access-btn">Request access</button>
        </div>
      </div>
    </div>
  `;

  const form = root.querySelector<HTMLFormElement>('#login-form');
  form?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    const username = String(formData.get('username') ?? '').trim();
    const password = String(formData.get('password') ?? '').trim();

    try {
      const payload = await request<LoginResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });

      state.isViewOnly = false;
      setToken(payload.token);
      await loadDashboard();
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to sign in';
      const statusText = root.querySelector<HTMLParagraphElement>('.subtitle');
      if (statusText) {
        statusText.textContent = message;
        statusText.classList.add('error-text');
      }
    }
  });

  const viewOnlyBtn = root.querySelector<HTMLButtonElement>('#view-only-access-btn');
  viewOnlyBtn?.addEventListener('click', () => {
    enterViewOnlyMode();
  });

  const openRequestAccessBtn = root.querySelector<HTMLButtonElement>('#open-request-access-btn');
  openRequestAccessBtn?.addEventListener('click', () => {
    renderRequestAccess();
  });
}

function enterViewOnlyMode(): void {
  state.isViewOnly = true;
  state.currentView = 'dashboard';
  state.editingCustomer = null;
  state.isAddModalOpen = false;

  // Populate sample portfolio data for the view-only dashboard display
  state.customers = [...DEMO_CUSTOMERS];
  state.assessments = [...DEMO_ASSESSMENTS];

  renderApp();
}

function renderRequestAccess(): void {
  root.innerHTML = `
    <div class="auth-shell">
      <div class="auth-card">
        <p class="eyebrow">Account Clearance</p>
        <h1>Request Access</h1>
        <p class="subtitle" id="request-subtitle">
          Submit your email and the reason for access. Your request will be reviewed by an administrator.
        </p>

        <form id="request-access-form" class="stacked-form">
          <label>
            <span>Your name (optional)</span>
            <input name="name" type="text" placeholder="Jane Doe" />
          </label>

          <label>
            <span>Email address</span>
            <input name="email" type="email" placeholder="name@example.com" required />
          </label>

          <label>
            <span>Why do you need access?</span>
            <textarea name="reason" rows="4" placeholder="Briefly describe your role, department, or reason for requesting access..." required></textarea>
          </label>

          <div class="form-actions">
            <button type="button" class="secondary-button" id="back-to-login-btn">Back to Login</button>
            <button type="submit" class="primary-button" id="submit-access-btn">Submit Request</button>
          </div>
        </form>
      </div>
    </div>
  `;

  const backBtn = root.querySelector<HTMLButtonElement>('#back-to-login-btn');
  backBtn?.addEventListener('click', () => {
    renderLogin();
  });

  const requestForm = root.querySelector<HTMLFormElement>('#request-access-form');
  requestForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(requestForm);
    const name = String(formData.get('name') ?? '').trim();
    const email = String(formData.get('email') ?? '').trim();
    const reason = String(formData.get('reason') ?? '').trim();

    const submitBtn = root.querySelector<HTMLButtonElement>('#submit-access-btn');
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = 'Sending...';
    }

    try {
      await request<{ message: string }>('/api/auth/request-access', {
        method: 'POST',
        body: JSON.stringify({ name, email, reason }),
      });

      renderAccessRequestSuccess(email);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unable to submit request';
      const statusText = root.querySelector<HTMLParagraphElement>('#request-subtitle');
      if (statusText) {
        statusText.innerHTML = `<span class="error-text">${message}</span>`;
      }
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Submit Request';
      }
    }
  });
}

function renderAccessRequestSuccess(requesterEmail: string): void {
  root.innerHTML = `
    <div class="auth-shell">
      <div class="auth-card success-state">
        <div class="success-icon-wrap">
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="var(--success)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
        </div>
        <p class="eyebrow alt">Request Dispatched</p>
        <h1>Access Requested</h1>
        <p class="subtitle">
          Your request for <strong>${requesterEmail}</strong> has been submitted to the administrator for review.
        </p>

        <button type="button" class="primary-button" id="return-to-login-btn" style="width: 100%; margin-top: 24px;">Return to Login</button>
      </div>
    </div>
  `;

  const returnBtn = root.querySelector<HTMLButtonElement>('#return-to-login-btn');
  returnBtn?.addEventListener('click', () => {
    renderLogin();
  });
}

function renderDashboardView(): string {
  const summary = getRiskSummary();
  const isViewOnly = state.isViewOnly;

  return `
    ${isViewOnly ? `
      <div class="view-only-banner">
        <div class="view-only-badge">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>
          <span>View-Only Mode</span>
        </div>
        <p>This is a read-only preview of the portfolio dashboard. All buttons, actions, and form inputs are non-responsive and disabled.</p>
      </div>
    ` : ''}

    <section class="stats-grid">
      <article class="stat-card accent">
        <span>Total customers</span>
        <strong>${state.customers.length}</strong>
      </article>
      <article class="stat-card">
        <span>Assessments</span>
        <strong>${state.assessments.length}</strong>
      </article>
      <article class="stat-card warning">
        <span>High risk</span>
        <strong>${summary.high}</strong>
      </article>
      <article class="stat-card success">
        <span>Low risk</span>
        <strong>${summary.low}</strong>
      </article>
    </section>

    <section class="panel-grid">
      <div class="panel">
        <div class="panel-header">
          <h3>Customer list</h3>
          <span>${state.customers.length} active records</span>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Score</th>
                <th>Income</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${state.customers.length === 0 ? `
                <tr>
                  <td colspan="4" class="empty-state">No customers yet.</td>
                </tr>
              ` : state.customers.slice(0, 8).map((customer) => `
                <tr>
                  <td>
                    <div class="customer-name">${customer.name}</div>
                    <small>${customer.email} • ${customer.externalId}</small>
                  </td>
                  <td><span class="${getScoreBadgeClass(customer.creditScore)}">${customer.creditScore}</span></td>
                  <td>${formatMoney(customer.annualIncome)}</td>
                  <td>
                    <div class="action-cell">
                      ${isViewOnly ? `
                        <button class="mini-button secondary disabled-control" type="button" disabled title="Disabled in view-only mode">Edit</button>
                        <button class="mini-button disabled-control" type="button" disabled title="Disabled in view-only mode">Assess</button>
                      ` : `
                        <button class="mini-button secondary" type="button" data-edit-customer-id="${customer.id}">Edit</button>
                        <button class="mini-button" type="button" data-customer-id="${customer.id}">Assess</button>
                      `}
                    </div>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>Risk reports</h3>
          <span>Latest outcomes</span>
        </div>

        <div class="report-stack">
          <div class="report-row">
            <span>Low</span>
            <div class="bar"><i style="width:${state.assessments.length ? (summary.low / state.assessments.length) * 100 : 0}%"></i></div>
            <strong>${summary.low}</strong>
          </div>
          <div class="report-row">
            <span>Medium</span>
            <div class="bar"><i style="width:${state.assessments.length ? (summary.medium / state.assessments.length) * 100 : 0}%"></i></div>
            <strong>${summary.medium}</strong>
          </div>
          <div class="report-row">
            <span>High</span>
            <div class="bar"><i style="width:${state.assessments.length ? (summary.high / state.assessments.length) * 100 : 0}%"></i></div>
            <strong>${summary.high}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="bottom-grid">
      <div class="panel">
        <div class="panel-header">
          <h3>Add customer</h3>
          ${isViewOnly ? '<span class="status-tag view-only-tag">Disabled in View-Only</span>' : '<span>Create record</span>'}
        </div>

        <form id="customer-form" class="stacked-form compact-form ${isViewOnly ? 'disabled-control' : ''}">
          <div class="field-row">
            <label>
              <span>Full name</span>
              <input name="name" placeholder="John Doe" ${isViewOnly ? 'disabled readonly' : 'required'} />
            </label>
            <label>
              <span>External ID</span>
              <input name="externalId" placeholder="CUST-001" ${isViewOnly ? 'disabled readonly' : 'required'} />
            </label>
          </div>

          <div class="field-row">
            <label>
              <span>Email</span>
              <input type="email" name="email" placeholder="john@example.com" ${isViewOnly ? 'disabled readonly' : 'required'} />
            </label>
            <label>
              <span>Birth date</span>
              <input type="date" name="birthDate" ${isViewOnly ? 'disabled readonly' : 'required'} />
            </label>
          </div>

          <div class="field-row">
            <label>
              <span>Credit score (300 - 850)</span>
              <input type="number" name="creditScore" min="300" max="850" placeholder="720" ${isViewOnly ? 'disabled readonly' : 'required'} />
            </label>
            <label>
              <span>Annual income ($)</span>
              <input type="number" name="annualIncome" min="0" step="1000" placeholder="85000" ${isViewOnly ? 'disabled readonly' : 'required'} />
            </label>
          </div>

          <button type="submit" class="primary-button ${isViewOnly ? 'disabled-control' : ''}" ${isViewOnly ? 'disabled' : ''}>
            ${isViewOnly ? 'Create customer (Disabled in View Only)' : 'Create customer'}
          </button>
        </form>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>Recent assessments</h3>
          <span>Decision stream</span>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Customer</th>
                <th>Risk</th>
                <th>Decision</th>
              </tr>
            </thead>
            <tbody>
              ${state.assessments.length === 0 ? `
                <tr>
                  <td colspan="3" class="empty-state">No assessments yet.</td>
                </tr>
              ` : state.assessments.slice(0, 6).map((assessment) => {
                const customer = state.customers.find((item) => item.id === assessment.customerId);
                const dec = formatDecision(assessment.decision);
                return `
                  <tr>
                    <td>${customer?.name ?? `Customer #${assessment.customerId}`}</td>
                    <td><span class="risk-pill ${assessment.riskLevel.toLowerCase()}">${formatRiskLevel(assessment.riskLevel)}</span></td>
                    <td><span class="decision-pill ${(assessment.decision || '').toLowerCase()}">${dec}</span></td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  `;
}

function renderCustomersView(): string {
  const query = state.searchQuery.toLowerCase().trim();
  const filtered = state.customers.filter((c) => {
    if (!query) return true;
    return (
      c.name.toLowerCase().includes(query) ||
      c.email.toLowerCase().includes(query) ||
      c.externalId.toLowerCase().includes(query)
    );
  });

  return `
    <div class="toolbar">
      <div class="search-box">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color:var(--muted)">
          <circle cx="11" cy="11" r="8"></circle>
          <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
        </svg>
        <input id="customer-search-input" type="search" placeholder="Search by name, email, or external ID..." value="${state.searchQuery}" />
      </div>

      <button id="open-add-customer-btn" class="primary-button" type="button">+ New Customer</button>
    </div>

    <div class="panel full-width">
      <div class="panel-header">
        <div>
          <h3>All Customer Records</h3>
          <span>Showing ${filtered.length} of ${state.customers.length} total customers</span>
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Customer</th>
              <th>Email</th>
              <th>Birth Date</th>
              <th>Credit Score</th>
              <th>Annual Income</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            ${filtered.length === 0 ? `
              <tr>
                <td colspan="6" class="empty-state">
                  ${state.searchQuery ? `No customers matched "${state.searchQuery}".` : 'No customer records found.'}
                </td>
              </tr>
            ` : filtered.map((c) => `
              <tr>
                <td>
                  <div class="customer-name">${c.name}</div>
                  <small>ID: ${c.externalId}</small>
                </td>
                <td>${c.email}</td>
                <td>${c.birthDate || '—'}</td>
                <td><span class="${getScoreBadgeClass(c.creditScore)}">${c.creditScore}</span></td>
                <td>${formatMoney(c.annualIncome)}</td>
                <td>
                  <div class="action-cell">
                    <button class="mini-button secondary" type="button" data-edit-customer-id="${c.id}">Edit</button>
                    <button class="mini-button" type="button" data-customer-id="${c.id}">Assess</button>
                    <button class="mini-button danger" type="button" data-delete-customer-id="${c.id}">Delete</button>
                  </div>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function renderReportsView(): string {
  const summary = getRiskSummary();
  const total = state.assessments.length;

  const lowPct = total ? Math.round((summary.low / total) * 100) : 0;
  const medPct = total ? Math.round((summary.medium / total) * 100) : 0;
  const highPct = total ? Math.round((summary.high / total) * 100) : 0;

  const appPct = total ? Math.round((summary.approved / total) * 100) : 0;
  const revPct = total ? Math.round((summary.review / total) * 100) : 0;
  const rejPct = total ? Math.round((summary.rejected / total) * 100) : 0;

  return `
    <section class="stats-grid">
      <article class="stat-card accent">
        <span>Total Assessments</span>
        <strong>${total}</strong>
      </article>
      <article class="stat-card success">
        <span>Approval Rate</span>
        <strong>${appPct}%</strong>
      </article>
      <article class="stat-card warning">
        <span>In Review</span>
        <strong>${revPct}%</strong>
      </article>
      <article class="stat-card" style="background: linear-gradient(180deg, rgba(248, 113, 113, 0.14), rgba(15, 23, 42, 0.9));">
        <span>Rejection Rate</span>
        <strong>${rejPct}%</strong>
      </article>
    </section>

    <section class="panel-grid">
      <div class="panel">
        <div class="panel-header">
          <h3>Risk Level Distribution</h3>
          <span>Score bands (Low >= 80, Med >= 50, High < 50)</span>
        </div>

        <div class="report-stack">
          <div class="report-row">
            <span>Low</span>
            <div class="bar"><i style="width:${lowPct}%"></i></div>
            <strong>${summary.low} (${lowPct}%)</strong>
          </div>
          <div class="report-row">
            <span>Medium</span>
            <div class="bar"><i style="width:${medPct}%"></i></div>
            <strong>${summary.medium} (${medPct}%)</strong>
          </div>
          <div class="report-row">
            <span>High</span>
            <div class="bar"><i style="width:${highPct}%"></i></div>
            <strong>${summary.high} (${highPct}%)</strong>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>Decision Distribution</h3>
          <span>Automated underwriting outcomes</span>
        </div>

        <div class="report-stack">
          <div class="report-row">
            <span>Approved</span>
            <div class="bar"><i style="width:${appPct}%; background:var(--success);"></i></div>
            <strong>${summary.approved} (${appPct}%)</strong>
          </div>
          <div class="report-row">
            <span>Review</span>
            <div class="bar"><i style="width:${revPct}%; background:var(--warning);"></i></div>
            <strong>${summary.review} (${revPct}%)</strong>
          </div>
          <div class="report-row">
            <span>Rejected</span>
            <div class="bar"><i style="width:${rejPct}%; background:var(--danger);"></i></div>
            <strong>${summary.rejected} (${rejPct}%)</strong>
          </div>
        </div>
      </div>
    </section>

    <div class="panel full-width">
      <div class="panel-header">
        <h3>Full Decision & Assessment Stream</h3>
        <span>Chronological audit log of all completed evaluations</span>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Customer</th>
              <th>Assessment Date</th>
              <th>Risk Score</th>
              <th>Risk Level</th>
              <th>Decision</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${state.assessments.length === 0 ? `
              <tr>
                <td colspan="7" class="empty-state">No assessments calculated yet.</td>
              </tr>
            ` : state.assessments.map((a) => {
              const customer = state.customers.find((c) => c.id === a.customerId);
              return `
                <tr>
                  <td>#${a.id}</td>
                  <td>
                    <div class="customer-name">${customer?.name ?? `Customer #${a.customerId}`}</div>
                    <small>${customer ? `ID: ${customer.externalId}` : ''}</small>
                  </td>
                  <td>${a.assessmentDate || '—'}</td>
                  <td><strong>${a.riskScore}</strong></td>
                  <td><span class="risk-pill ${a.riskLevel.toLowerCase()}">${formatRiskLevel(a.riskLevel)}</span></td>
                  <td><span class="decision-pill ${(a.decision || '').toLowerCase()}">${formatDecision(a.decision)}</span></td>
                  <td>
                    <button class="mini-button" type="button" data-customer-id="${a.customerId}">Re-assess</button>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function renderEditModal(): string {
  const c = state.editingCustomer;
  if (!c) return '';

  return `
    <div class="modal-overlay" id="edit-modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Modify record</p>
            <h3>Edit Customer</h3>
          </div>
          <button class="modal-close" id="close-edit-modal-btn" type="button">✕</button>
        </div>

        <form id="edit-customer-form" class="stacked-form compact-form">
          <div class="field-row">
            <label>
              <span>Full name</span>
              <input name="name" value="${c.name}" required />
            </label>
            <label>
              <span>External ID</span>
              <input name="externalId" value="${c.externalId}" required />
            </label>
          </div>

          <div class="field-row">
            <label>
              <span>Email</span>
              <input type="email" name="email" value="${c.email}" required />
            </label>
            <label>
              <span>Birth date</span>
              <input type="date" name="birthDate" value="${c.birthDate ?? ''}" required />
            </label>
          </div>

          <div class="field-row">
            <label>
              <span>Credit score (300 - 850)</span>
              <input type="number" name="creditScore" min="300" max="850" value="${c.creditScore}" required />
            </label>
            <label>
              <span>Annual income ($)</span>
              <input type="number" name="annualIncome" min="0" step="1000" value="${c.annualIncome}" required />
            </label>
          </div>

          <div class="modal-actions">
            <button type="button" class="secondary-button" id="cancel-edit-modal-btn">Cancel</button>
            <button type="submit" class="primary-button">Save Changes</button>
          </div>
        </form>
      </div>
    </div>
  `;
}

function renderAddModal(): string {
  if (!state.isAddModalOpen) return '';

  return `
    <div class="modal-overlay" id="add-modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <p class="eyebrow">Create record</p>
            <h3>Add New Customer</h3>
          </div>
          <button class="modal-close" id="close-add-modal-btn" type="button">✕</button>
        </div>

        <form id="add-modal-form" class="stacked-form compact-form">
          <div class="field-row">
            <label>
              <span>Full name</span>
              <input name="name" placeholder="Alice Smith" required />
            </label>
            <label>
              <span>External ID</span>
              <input name="externalId" placeholder="CUST-009" required />
            </label>
          </div>

          <div class="field-row">
            <label>
              <span>Email</span>
              <input type="email" name="email" placeholder="alice@example.com" required />
            </label>
            <label>
              <span>Birth date</span>
              <input type="date" name="birthDate" required />
            </label>
          </div>

          <div class="field-row">
            <label>
              <span>Credit score (300 - 850)</span>
              <input type="number" name="creditScore" min="300" max="850" placeholder="750" required />
            </label>
            <label>
              <span>Annual income ($)</span>
              <input type="number" name="annualIncome" min="0" step="1000" placeholder="95000" required />
            </label>
          </div>

          <div class="modal-actions">
            <button type="button" class="secondary-button" id="cancel-add-modal-btn">Cancel</button>
            <button type="submit" class="primary-button">Create Customer</button>
          </div>
        </form>
      </div>
    </div>
  `;
}

function renderApp(): void {
  const titles = {
    dashboard: { eyebrow: 'Overview', title: 'Portfolio dashboard' },
    customers: { eyebrow: 'Directory', title: 'Customer Management' },
    reports: { eyebrow: 'Analytics', title: 'Risk & Decision Reports' },
  };

  const header = titles[state.currentView];
  const isViewOnly = state.isViewOnly;

  root.innerHTML = `
    <div class="dashboard-shell ${isViewOnly ? 'is-view-only' : ''}">
      <aside class="sidebar">
        <div class="brand-block">
          <p class="eyebrow">${isViewOnly ? 'Preview Mode' : 'Risk API'}</p>
          <h2>Control Center</h2>
        </div>

        <nav class="nav-links">
          <button type="button" class="nav-item ${state.currentView === 'dashboard' ? 'active' : ''}" data-view="dashboard">
            Dashboard
          </button>
          <button type="button" class="nav-item ${isViewOnly ? 'disabled-nav' : ''} ${state.currentView === 'customers' ? 'active' : ''}" ${isViewOnly ? 'disabled title="Disabled in view-only mode"' : 'data-view="customers"'}>
            Customers
          </button>
          <button type="button" class="nav-item ${isViewOnly ? 'disabled-nav' : ''} ${state.currentView === 'reports' ? 'active' : ''}" ${isViewOnly ? 'disabled title="Disabled in view-only mode"' : 'data-view="reports"'}>
            Reports
          </button>
        </nav>
      </aside>

      <main class="content-panel">
        <header class="topbar">
          <div>
            <p class="eyebrow alt">${isViewOnly ? 'Read-Only Preview' : header.eyebrow}</p>
            <h1>${header.title}</h1>
          </div>
          <button id="logout-button" class="secondary-button" type="button">
            ${isViewOnly ? 'Exit View Only' : 'Logout'}
          </button>
        </header>

        ${state.currentView === 'dashboard' ? renderDashboardView() : ''}
        ${!isViewOnly && state.currentView === 'customers' ? renderCustomersView() : ''}
        ${!isViewOnly && state.currentView === 'reports' ? renderReportsView() : ''}
      </main>
    </div>

    ${!isViewOnly ? renderEditModal() : ''}
    ${!isViewOnly ? renderAddModal() : ''}
  `;

  bindEvents();
}

function bindEvents(): void {
  // If in View-Only mode, nothing is responsive! Only exit back to login is active.
  if (state.isViewOnly) {
    const exitButton = root.querySelector<HTMLButtonElement>('#logout-button');
    exitButton?.addEventListener('click', () => {
      state.isViewOnly = false;
      state.customers = [];
      state.assessments = [];
      renderLogin();
    });
    return;
  }

  // Navigation switching
  root.querySelectorAll<HTMLButtonElement>('[data-view]').forEach((button) => {
    button.addEventListener('click', () => {
      const view = button.getAttribute('data-view') as ActiveView | null;
      if (view && view !== state.currentView) {
        state.currentView = view;
        renderApp();
      }
    });
  });

  // Logout
  const logoutButton = root.querySelector<HTMLButtonElement>('#logout-button');
  logoutButton?.addEventListener('click', () => {
    setToken('');
    state.isViewOnly = false;
    renderLogin();
  });

  // Search input on Customers view
  const searchInput = root.querySelector<HTMLInputElement>('#customer-search-input');
  if (searchInput) {
    searchInput.addEventListener('input', () => {
      state.searchQuery = searchInput.value;
      const filteredPanel = root.querySelector('.panel.full-width');
      if (filteredPanel) {
        // Quick re-render of Customers view without flickering the full shell
        renderApp();
        const updatedInput = root.querySelector<HTMLInputElement>('#customer-search-input');
        if (updatedInput) {
          updatedInput.focus();
          updatedInput.setSelectionRange(state.searchQuery.length, state.searchQuery.length);
        }
      }
    });
  }

  // Open Add modal button
  const openAddBtn = root.querySelector<HTMLButtonElement>('#open-add-customer-btn');
  openAddBtn?.addEventListener('click', () => {
    state.isAddModalOpen = true;
    renderApp();
  });

  // Close Add modal
  const closeAddBtn = root.querySelector<HTMLButtonElement>('#close-add-modal-btn');
  const cancelAddBtn = root.querySelector<HTMLButtonElement>('#cancel-add-modal-btn');
  const addOverlay = root.querySelector<HTMLDivElement>('#add-modal-overlay');

  const closeAdd = () => {
    state.isAddModalOpen = false;
    renderApp();
  };

  closeAddBtn?.addEventListener('click', closeAdd);
  cancelAddBtn?.addEventListener('click', closeAdd);
  addOverlay?.addEventListener('click', (e) => {
    if (e.target === addOverlay) closeAdd();
  });

  // Add customer form (in modal)
  const addModalForm = root.querySelector<HTMLFormElement>('#add-modal-form');
  addModalForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(addModalForm);
    const payload = {
      externalId: String(formData.get('externalId') ?? '').trim(),
      name: String(formData.get('name') ?? '').trim(),
      email: String(formData.get('email') ?? '').trim(),
      birthDate: String(formData.get('birthDate') ?? '').trim(),
      creditScore: Number(formData.get('creditScore')),
      annualIncome: Number(formData.get('annualIncome')),
    };

    try {
      await request('/api/customers', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      state.isAddModalOpen = false;
      showToast('Customer created successfully', 'success');
      await loadData();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to create customer';
      showToast(msg, 'error');
    }
  });

  // Inline customer form (on dashboard)
  const customerForm = root.querySelector<HTMLFormElement>('#customer-form');
  customerForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(customerForm);
    const payload = {
      externalId: String(formData.get('externalId') ?? '').trim(),
      name: String(formData.get('name') ?? '').trim(),
      email: String(formData.get('email') ?? '').trim(),
      birthDate: String(formData.get('birthDate') ?? '').trim(),
      creditScore: Number(formData.get('creditScore')),
      annualIncome: Number(formData.get('annualIncome')),
    };

    try {
      await request('/api/customers', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      showToast('Customer created successfully', 'success');
      await loadData();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to create customer';
      showToast(msg, 'error');
    }
  });

  // Edit Customer Modal triggers
  root.querySelectorAll<HTMLButtonElement>('[data-edit-customer-id]').forEach((button) => {
    button.addEventListener('click', () => {
      const customerId = Number(button.getAttribute('data-edit-customer-id'));
      const customer = state.customers.find((c) => c.id === customerId);
      if (customer) {
        state.editingCustomer = customer;
        renderApp();
      }
    });
  });

  // Close Edit modal
  const closeEditBtn = root.querySelector<HTMLButtonElement>('#close-edit-modal-btn');
  const cancelEditBtn = root.querySelector<HTMLButtonElement>('#cancel-edit-modal-btn');
  const editOverlay = root.querySelector<HTMLDivElement>('#edit-modal-overlay');

  const closeEdit = () => {
    state.editingCustomer = null;
    renderApp();
  };

  closeEditBtn?.addEventListener('click', closeEdit);
  cancelEditBtn?.addEventListener('click', closeEdit);
  editOverlay?.addEventListener('click', (e) => {
    if (e.target === editOverlay) closeEdit();
  });

  // Edit customer form submit
  const editForm = root.querySelector<HTMLFormElement>('#edit-customer-form');
  editForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    const customerId = state.editingCustomer?.id;
    if (!customerId) return;

    const formData = new FormData(editForm);
    const payload = {
      externalId: String(formData.get('externalId') ?? '').trim(),
      name: String(formData.get('name') ?? '').trim(),
      email: String(formData.get('email') ?? '').trim(),
      birthDate: String(formData.get('birthDate') ?? '').trim(),
      creditScore: Number(formData.get('creditScore')),
      annualIncome: Number(formData.get('annualIncome')),
    };

    try {
      await request(`/api/customers/${customerId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });

      state.editingCustomer = null;
      showToast('Customer updated successfully', 'success');
      await loadData();
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to update customer';
      showToast(msg, 'error');
    }
  });

  // Delete customer
  root.querySelectorAll<HTMLButtonElement>('[data-delete-customer-id]').forEach((button) => {
    button.addEventListener('click', async () => {
      const customerId = Number(button.getAttribute('data-delete-customer-id'));
      const customer = state.customers.find((c) => c.id === customerId);
      const name = customer?.name ?? `#${customerId}`;

      if (!confirm(`Are you sure you want to delete customer ${name}?`)) {
        return;
      }

      try {
        await request(`/api/customers/${customerId}`, {
          method: 'DELETE',
        });
        showToast('Customer deleted successfully', 'success');
        await loadData();
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Failed to delete customer';
        showToast(msg, 'error');
      }
    });
  });

  // Run assessment trigger
  root.querySelectorAll<HTMLButtonElement>('[data-customer-id]').forEach((button) => {
    button.addEventListener('click', async () => {
      const customerId = button.getAttribute('data-customer-id');
      if (!customerId) return;

      try {
        button.disabled = true;
        button.textContent = 'Scoring...';

        const result = await request<RiskAssessment>(`/api/risk-assessments/calculate/${customerId}`, {
          method: 'POST',
        });

        const dec = result.decision ? `Outcome: ${result.decision}` : 'Calculated';
        showToast(`Risk Assessment completed! ${dec} (Score: ${result.riskScore})`, 'success');
        await loadData();
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Assessment failed';
        showToast(msg, 'error');
        button.disabled = false;
        button.textContent = 'Assess';
      }
    });
  });
}

async function loadData(): Promise<void> {
  try {
    const [customers, assessments] = await Promise.all([
      request<Customer[]>('/api/customers'),
      request<RiskAssessment[]>('/api/risk-assessments'),
    ]);

    state.customers = customers ?? [];
    state.assessments = assessments ?? [];
    renderApp();
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unable to load data';
    showToast(message, 'error');
    setToken('');
    renderLogin();
  }
}

async function loadDashboard(): Promise<void> {
  await loadData();
}

if (!state.token) {
  renderLogin();
} else {
  void loadDashboard();
}
