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

type DashboardState = {
  token: string;
  customers: Customer[];
  assessments: RiskAssessment[];
};

const STORAGE_KEY = 'risk-api-token';
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/$/, '');
const appRoot = document.querySelector<HTMLDivElement>('#app') as HTMLDivElement | null;

if (!appRoot) {
  throw new Error('Application root element not found');
}

const root = appRoot;

const state: DashboardState = {
  token: localStorage.getItem(STORAGE_KEY) ?? '',
  customers: [],
  assessments: [],
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

function renderLogin(): void {
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

          <button type="submit" class="primary-button">Login</button>
        </form>
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
}

function getRiskSummary() {
  const summary = {
    low: 0,
    medium: 0,
    high: 0,
  };

  for (const assessment of state.assessments) {
    const level = formatRiskLevel(assessment.riskLevel);
    if (level === 'LOW') summary.low += 1;
    if (level === 'MEDIUM') summary.medium += 1;
    if (level === 'HIGH') summary.high += 1;
  }

  return summary;
}

function renderDashboard(): void {
  const summary = getRiskSummary();

  root.innerHTML = `
    <div class="dashboard-shell">
      <aside class="sidebar">
        <div class="brand-block">
          <p class="eyebrow">Risk API</p>
          <h2>Control Center</h2>
        </div>

        <nav class="nav-links">
          <span class="nav-item active">Dashboard</span>
          <span class="nav-item">Customers</span>
          <span class="nav-item">Reports</span>
        </nav>
      </aside>

      <main class="content-panel">
        <header class="topbar">
          <div>
            <p class="eyebrow alt">Overview</p>
            <h1>Portfolio dashboard</h1>
          </div>
          <button id="logout-button" class="secondary-button" type="button">Logout</button>
        </header>

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
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  ${state.customers.length === 0 ? `
                    <tr>
                      <td colspan="4" class="empty-state">No customers yet.</td>
                    </tr>
                  ` : state.customers.map((customer) => `
                    <tr>
                      <td>
                        <div class="customer-name">${customer.name}</div>
                        <small>${customer.email}</small>
                      </td>
                      <td>${customer.creditScore}</td>
                      <td>${formatMoney(customer.annualIncome)}</td>
                      <td>
                        <button class="mini-button" type="button" data-customer-id="${customer.id}">Run assessment</button>
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
              <span>Create record</span>
            </div>

            <form id="customer-form" class="stacked-form compact-form">
              <div class="field-row">
                <label>
                  <span>Full name</span>
                  <input name="name" required />
                </label>
                <label>
                  <span>External ID</span>
                  <input name="externalId" required />
                </label>
              </div>

              <div class="field-row">
                <label>
                  <span>Email</span>
                  <input type="email" name="email" required />
                </label>
                <label>
                  <span>Birth date</span>
                  <input type="date" name="birthDate" required />
                </label>
              </div>

              <div class="field-row">
                <label>
                  <span>Credit score</span>
                  <input type="number" name="creditScore" min="0" max="850" required />
                </label>
                <label>
                  <span>Annual income</span>
                  <input type="number" name="annualIncome" min="0" step="1000" required />
                </label>
              </div>

              <button type="submit" class="primary-button">Create customer</button>
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
                    return `
                      <tr>
                        <td>${customer?.name ?? `Customer #${assessment.customerId}`}</td>
                        <td><span class="risk-pill ${assessment.riskLevel.toLowerCase()}">${formatRiskLevel(assessment.riskLevel)}</span></td>
                        <td>${assessment.decision}</td>
                      </tr>
                    `;
                  }).join('')}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      </main>
    </div>
  `;

  const logoutButton = root.querySelector<HTMLButtonElement>('#logout-button');
  logoutButton?.addEventListener('click', () => {
    setToken('');
    renderLogin();
  });

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

    await request('/api/customers', {
      method: 'POST',
      body: JSON.stringify(payload),
    });

    await loadDashboard();
  });

  root.querySelectorAll<HTMLButtonElement>('[data-customer-id]').forEach((button) => {
    button.addEventListener('click', async () => {
      const customerId = button.getAttribute('data-customer-id');
      if (!customerId) {
        return;
      }

      await request(`/api/risk-assessments/calculate/${customerId}`, {
        method: 'POST',
      });

      await loadDashboard();
    });
  });
}

async function loadDashboard(): Promise<void> {
  try {
    const [customers, assessments] = await Promise.all([
      request<Customer[]>('/api/customers'),
      request<RiskAssessment[]>('/api/risk-assessments'),
    ]);

    state.customers = customers ?? [];
    state.assessments = assessments ?? [];
    renderDashboard();
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Unable to load dashboard';
    alert(message);
    setToken('');
    renderLogin();
  }
}

if (!state.token) {
  renderLogin();
} else {
  void loadDashboard();
}
