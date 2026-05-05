let barChartInstance = null;
let pieChartInstance = null;
let yearComparisonChartInstance = null;

const API_BASE = "http://localhost:8081";
let token = localStorage.getItem("jwt");
let currentUsername = localStorage.getItem("username");

let currentMonth = new Date().getMonth(); // JS internal: 0-11
let currentYear = new Date().getFullYear();
let allExpenses = [];

/* ================================
   🔄 APP LOAD
================================ */
window.onload = () => {
  token = localStorage.getItem("jwt");

  if (token) {
    showDashboard();
    loadDashboardData();
  } else {
    const loginSection = document.getElementById("login-section");
    const dashboardSection = document.getElementById("dashboard-section");
    if (loginSection) loginSection.style.display = "flex";
    if (dashboardSection) dashboardSection.style.display = "none";
  }

  // Attach nav listeners safely
  const prevBtn = document.getElementById("prevMonth");
  const nextBtn = document.getElementById("nextMonth");
  if (prevBtn) prevBtn.onclick = onPrevMonth;
  if (nextBtn) nextBtn.onclick = onNextMonth;
};

/* ================================
   🔐 LOGIN
================================ */
function login() {
  const usernameEl = document.getElementById("username");
  const passwordEl = document.getElementById("password");
  const errEl = document.getElementById("login-error");

  const username = usernameEl ? usernameEl.value : "";
  const password = passwordEl ? passwordEl.value : "";

  fetch(`${API_BASE}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  })
    .then(res => {
      if (!res.ok) throw new Error("Login failed");
      return res.json();
    })
    .then(data => {
      localStorage.setItem("jwt", data.token);
      localStorage.setItem("username", data.username); // ✅ Store username
      localStorage.setItem("email", data.email); // ✅ Store email
      token = data.token;
      showDashboard();
      loadDashboardData();
    })
    .catch(() => {
      if (errEl) errEl.innerText = "Invalid username or password";
    });
}
/* ================================
   📝 REGISTER
================================ */
function register() {
  const usernameEl = document.getElementById("reg-username");
  const emailEl = document.getElementById("reg-email");
  const passwordEl = document.getElementById("reg-password");
  const confirmPasswordEl = document.getElementById("reg-confirm-password");
  const errEl = document.getElementById("register-error");

  const username = usernameEl ? usernameEl.value.trim() : "";
  const email = emailEl ? emailEl.value.trim() : "";
  const password = passwordEl ? passwordEl.value : "";
  const confirmPassword = confirmPasswordEl ? confirmPasswordEl.value : "";

  // Clear previous errors
  if (errEl) errEl.innerText = "";

  // Validation
  if (!username || !email || !password || !confirmPassword) {
    if (errEl) errEl.innerText = "All fields are required";
    return;
  }

  if (password !== confirmPassword) {
    if (errEl) errEl.innerText = "Passwords do not match";
    return;
  }

  if (password.length < 6) {
    if (errEl) errEl.innerText = "Password must be at least 6 characters";
    return;
  }

  // Register API call
  fetch(`${API_BASE}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password })
  })
    .then(res => {
      if (!res.ok) {
        return res.json().then(data => {
          throw new Error(data.error || "Registration failed");
        });
      }
      return res.json();
    })
    .then(data => {
      alert("Registration successful! Please login.");
      showLogin();
    })
    .catch(err => {
      if (errEl) errEl.innerText = err.message;
    });
}

function showRegister() {
  document.getElementById("login-section").style.display = "none";
  document.getElementById("register-section").style.display = "flex";
  document.getElementById("dashboard-section").style.display = "none";
}

function showLogin() {
  document.getElementById("register-section").style.display = "none";
  document.getElementById("login-section").style.display = "flex";
  document.getElementById("dashboard-section").style.display = "none";
}
function logout() {
  localStorage.removeItem("jwt");
  location.reload();
}

/* ================================
   📄 PAGE NAVIGATION
================================ */
function showDashboard() {
  document.getElementById("login-section").style.display = "none";
  document.getElementById("dashboard-section").style.display = "flex";

  document.getElementById("dashboard-page").style.display = "block";
  document.getElementById("expenses-page").style.display = "none";
  document.getElementById("summary-page").style.display = "none";

  document.getElementById("nav-dashboard").classList.add("active");
  document.getElementById("nav-expenses").classList.remove("active");
  document.getElementById("nav-summary").classList.remove("active");
}

function showExpenses() {
  document.getElementById("dashboard-section").style.display = "flex";

  document.getElementById("dashboard-page").style.display = "none";
  document.getElementById("expenses-page").style.display = "block";
  document.getElementById("summary-page").style.display = "none";

  document.getElementById("nav-dashboard").classList.remove("active");
  document.getElementById("nav-expenses").classList.add("active");
  document.getElementById("nav-summary").classList.remove("active");

  initExpenseFilters();
  loadExpensesForFilter();
}

function showSummary() {
  document.getElementById("dashboard-section").style.display = "flex";

  document.getElementById("dashboard-page").style.display = "none";
  document.getElementById("expenses-page").style.display = "none";
  document.getElementById("summary-page").style.display = "block";

  document.getElementById("nav-dashboard").classList.remove("active");
  document.getElementById("nav-expenses").classList.remove("active");
  document.getElementById("nav-summary").classList.add("active");

  loadSummaryData();
}

/* ================================
   💰 EXPENSES PAGE
================================ */
function initExpenseFilters() {
  const yearInput = document.getElementById("expenseYear");
  const monthSelect = document.getElementById("expenseMonth");

  if (!yearInput || !monthSelect) return;

  yearInput.value = currentYear;

  monthSelect.innerHTML = "";

  const months = [
    "January","February","March","April","May","June",
    "July","August","September","October","November","December"
  ];

  months.forEach((name, index) => {
    const opt = document.createElement("option");
    opt.value = index + 1;
    opt.textContent = name;
    if (index === currentMonth) opt.selected = true;
    monthSelect.appendChild(opt);
  });

  yearInput.oninput = loadExpensesForFilter;
  monthSelect.onchange = loadExpensesForFilter;
}

function loadExpensesForFilter() {
  const yearInput = document.getElementById("expenseYear");
  const monthSelect = document.getElementById("expenseMonth");

  if (!yearInput || !monthSelect) {
    console.error("Year or month filter not found");
    return;
  }

  const year = Number(yearInput.value);
  const month = Number(monthSelect.value);

  console.log("Loading expenses for:", year, month);

  currentYear = year;
  currentMonth = month - 1;

  fetch(`${API_BASE}/expenses?page=0&size=1000&username=${currentUsername}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
    .then(res => {
      if (!res.ok) throw new Error("Failed to fetch expenses");
      return res.json();
    })
    .then(data => {
      console.log("All expenses fetched:", data);

      allExpenses = data.expenses || [];

      const filtered = allExpenses.filter(e => {
        const d = new Date(e.date);
        return (
          d.getFullYear() === currentYear &&
          d.getMonth() === currentMonth
        );
      });

      console.log("Filtered expenses:", filtered);

      renderExpenseTable(filtered);
      loadMonthlyIncome();
      renderCharts();
    })
    .catch(err => {
      console.error("Error loading expenses:", err);
      alert("Failed to load expenses");
    });
}

function renderExpenseTable(expenses) {
  const body = document.getElementById("expenseTableBody");
  if (!body) return;

  body.innerHTML = "";

  if (expenses.length === 0) {
    body.innerHTML = `
      <tr>
        <td colspan="5" style="text-align:center;padding:20px;color:var(--muted);">
          No expenses found for this month
        </td>
      </tr>
    `;
    return;
  }

  expenses.forEach(e => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td style="padding:12px;">${e.title}</td>
      <td style="padding:12px;">${e.category || 'General'}</td>
      <td style="padding:12px;">${e.date}</td>
      <td style="padding:12px;">₹ ${e.amount}</td>
      <td style="padding:12px;">
        <button onclick="deleteExpense(${e.id})" style="padding:6px 12px;border-radius:6px;border:none;background:#ef4444;color:white;cursor:pointer;">Delete</button>
      </td>
    `;
    body.appendChild(tr);
  });
}

function addExpense() {
  const titleEl = document.getElementById("expenseTitle");
  const amountEl = document.getElementById("expenseAmount");
  const dateEl = document.getElementById("expenseDate");

  if (!titleEl || !amountEl || !dateEl) return;

  const title = titleEl.value.trim();
  const amount = Number(amountEl.value);
  const date = dateEl.value;

  if (!title || !amount || !date) {
    alert("Please fill all fields");
    return;
  }

  const categoryEl = document.getElementById("expenseCategory");
  const category = categoryEl ? categoryEl.value : "General";

  console.log("Adding expense:", { title, amount, date, category });

  fetch(`${API_BASE}/expenses`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({
      title,
      amount,
      date,
      category,
      username: currentUsername
    })
  })
    .then(res => {
      console.log("Add expense response status:", res.status);
      if (!res.ok) throw new Error("Failed to add expense");
      return res.json();
    })
    .then(newExpense => {
      console.log("Expense added successfully:", newExpense);

      titleEl.value = "";
      amountEl.value = "";
      dateEl.value = "";
      if (categoryEl) categoryEl.value = "General";

      const expenseDate = new Date(date);
      document.getElementById("expenseYear").value = expenseDate.getFullYear();
      document.getElementById("expenseMonth").value = expenseDate.getMonth() + 1;

      loadExpensesForFilter();
      loadDashboardData();

      if (document.getElementById("summary-page").style.display === "block") {
        loadSummaryData();
      }

      alert("Expense added successfully!");
    })
    .catch(err => {
      console.error("Error adding expense:", err);
      alert("Error adding expense: " + err.message);
    });
}

function deleteExpense(id) {
  fetch(`${API_BASE}/expenses/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` }
  }).then(() => {
    loadExpensesForFilter();
    loadDashboardData();

    if (document.getElementById("summary-page").style.display === "block") {
      loadSummaryData();
    }
  });
}

/* ================================
   📊 DASHBOARD DATA
================================ */
function loadDashboardData() {
  fetch(`${API_BASE}/expenses?page=0&size=1000&username=${currentUsername}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
    .then(res => res.json())
    .then(data => {
      allExpenses = data.expenses || [];

      drawCalendar(currentMonth, currentYear);
      renderCharts();
      loadMonthlyIncome();
      renderTodoList();
    })
    .catch(err => {
      console.error("Failed to load expenses:", err);
    });
}

/* ================================
   💰 MONTHLY INCOME
================================ */
function loadMonthlyIncome() {
  const input = document.getElementById("monthlyIncomeInput");
  const remainingEl = document.getElementById("remainingAmount");
  if (!input || !remainingEl) return;

  fetch(`${API_BASE}/kpi/income?year=${currentYear}&month=${currentMonth + 1}&username=${currentUsername}`,{
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
    .then(res => res.ok ? res.json() : Promise.reject(res))
    .then(data => {
      input.value = (data && data.amount != null) ? data.amount : 0;
      updateRemainingAmount();
    })
    .catch(err => {
      console.error("Failed to load monthly income:", err);
      input.value = 0;
      updateRemainingAmount();
    });
}

function saveMonthlyIncome() {
  const input = document.getElementById("monthlyIncomeInput");
  if (!input) return;

  const val = Number(input.value || 0);

  console.log("Saving income:", { year: currentYear, month: currentMonth + 1, amount: val, username: currentUsername });

  fetch(`${API_BASE}/kpi/income`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({
      year: currentYear,
      month: currentMonth + 1,
      amount: val,
      username: currentUsername
    })
  })
    .then(res => {
      if (!res.ok) throw new Error("Failed to save income");
      return res.json();
    })
    .then(data => {
      console.log("Income saved successfully:", data);
      updateRemainingAmount();
    })
    .catch(err => {
      console.error("Error saving income:", err);
      updateRemainingAmount();
    });
}

window.addEventListener("DOMContentLoaded", () => {
  const incomeInput = document.getElementById("monthlyIncomeInput");
  if (incomeInput) {
    incomeInput.addEventListener("blur", saveMonthlyIncome);
    incomeInput.addEventListener("keypress", (e) => {
      if (e.key === "Enter") {
        saveMonthlyIncome();
      }
    });
  }
});

function updateRemainingAmount() {
  const input = document.getElementById("monthlyIncomeInput");
  const remainingEl = document.getElementById("remainingAmount");
  if (!input || !remainingEl) return;

  const income = Number(input.value || 0);

  const monthSpent = allExpenses
    .filter(e => {
      const d = new Date(e.date);
      return d.getMonth() === currentMonth && d.getFullYear() === currentYear;
    })
    .reduce((s, e) => s + Number(e.amount || 0), 0);

  remainingEl.innerText = `₹ ${income - monthSpent}`;
}

/* ================================
   📝 TO-DO
================================ */
function fetchTodos() {
  return fetch(`${API_BASE}/kpi/todos?year=${currentYear}&month=${currentMonth + 1}&username=${currentUsername}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  }).then(res => res.json());
}

function addTodoItem() {
  const titleEl = document.getElementById("todoTitle");
  const amountEl = document.getElementById("todoAmount");
  if (!titleEl || !amountEl) return;

  const title = titleEl.value.trim();
  const amount = Number(amountEl.value);

  if (!title || !amount) return;

  fetch(`${API_BASE}/kpi/todos`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify({
      year: currentYear,
      month: currentMonth + 1,
      title,
      amount,
      done: false,
      username: currentUsername
    })
  })
    .then(() => {
      titleEl.value = "";
      amountEl.value = "";
      renderTodoList();
    })
    .catch(err => console.error("Add todo failed:", err));
}

function toggleTodo(id) {
  fetch(`${API_BASE}/kpi/todos/${id}/toggle`, {
    method: "PUT",
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
    .then(() => renderTodoList())
    .catch(err => console.error("Toggle todo failed:", err));
}

function renderTodoList() {
  const list = document.getElementById("todoList");
  if (!list) return;

  fetchTodos()
    .then(todos => {
      list.innerHTML = "";

      (todos || []).forEach(t => {
        const row = document.createElement("div");
        row.innerHTML = `
          <label style="display:flex;gap:6px;align-items:center;">
            <input type="checkbox" ${t.done ? "checked" : ""} onchange="toggleTodo(${t.id})">
            <span style="${t.done ? "text-decoration:line-through;opacity:0.6" : ""}">
              ${t.title} (₹${t.amount})
            </span>
          </label>
        `;
        list.appendChild(row);
      });
    })
    .catch(err => {
      console.error("Fetch todos failed:", err);
      list.innerHTML = "";
    });
}

/* ================================
   📊 CHARTS
================================ */
function getMonthlyTotals(expenses, year) {
  const totals = Array(12).fill(0);
  expenses.forEach(e => {
    const d = new Date(e.date);
    if (d.getFullYear() === year) totals[d.getMonth()] += Number(e.amount || 0);
  });
  return totals;
}

function getCategoryTotalsForMonth(expenses, month, year) {
  const totals = {};
  expenses.forEach(e => {
    const d = new Date(e.date);
    if (d.getMonth() === month && d.getFullYear() === year) {
      const cat = e.category || "Other";
      totals[cat] = (totals[cat] || 0) + Number(e.amount || 0);
    }
  });
  return totals;
}

function renderCharts() {
  const barCanvas = document.getElementById("barChart");
  const pieCanvas = document.getElementById("pieChart");
  if (!barCanvas || !pieCanvas) return;

  const ctx1 = barCanvas.getContext("2d");
  const ctx2 = pieCanvas.getContext("2d");

  if (barChartInstance) barChartInstance.destroy();
  if (pieChartInstance) pieChartInstance.destroy();

  barChartInstance = new Chart(ctx1, {
    type: "bar",
    data: {
      labels: ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
      datasets: [{
        data: getMonthlyTotals(allExpenses, currentYear),
        backgroundColor: "#6366f1",
        borderRadius: 6
      }]
    },
    options: { plugins: { legend: { display: false } } }
  });

  const catTotals = getCategoryTotalsForMonth(allExpenses, currentMonth, currentYear);

  pieChartInstance = new Chart(ctx2, {
    type: "pie",
    data: {
      labels: Object.keys(catTotals),
      datasets: [{ data: Object.values(catTotals) }]
    }
  });
}

/* ================================
   📅 CALENDAR
================================ */
function drawCalendar(month, year) {
  const grid = document.getElementById("calendarGrid");
  const title = document.getElementById("calendarTitle");
  if (!grid || !title) return;

  grid.innerHTML = "";
  title.innerText = `${getMonthName(month)} ${year}`;

  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"].forEach(d => {
    const h = document.createElement("div");
    h.className = "calendar-day header";
    h.innerText = d;
    grid.appendChild(h);
  });

  for (let i = 0; i < firstDay; i++) grid.appendChild(document.createElement("div"));

  for (let day = 1; day <= daysInMonth; day++) {
    const cell = document.createElement("div");
    cell.className = "calendar-day";
    cell.innerText = day;

    const dateStr = `${year}-${pad(month + 1)}-${pad(day)}`;
    const dayExpenses = allExpenses.filter(e => e.date === dateStr);
    if (dayExpenses.length > 0) cell.classList.add("has-expense");

    cell.onclick = () => showDayExpenses(dateStr, dayExpenses);
    grid.appendChild(cell);
  }

  const monthTotal = allExpenses
    .filter(e => {
      const d = new Date(e.date);
      return d.getMonth() === month && d.getFullYear() === year;
    })
    .reduce((s, e) => s + Number(e.amount || 0), 0);

  const monthTotalEl = document.getElementById("monthTotal");
  if (monthTotalEl) monthTotalEl.innerText = `₹ ${monthTotal}`;

  renderCharts();
  renderTodoList();
  loadMonthlyIncome();
}

/* ================================
   📦 MODAL
================================ */
function showDayExpenses(date, expenses) {
  const modal = document.getElementById("dayModal");
  const body = document.getElementById("modalBody");
  const title = document.getElementById("modalDate");
  const totalEl = document.getElementById("modalTotal");

  if (!modal || !body || !title || !totalEl) return;

  title.innerText = date;
  body.innerHTML = "";
  let total = 0;

  expenses.forEach(e => {
    total += Number(e.amount || 0);
    const row = document.createElement("div");
    row.className = "modal-item";
    row.innerHTML = `<span>${e.title}</span><span>₹${e.amount}</span>`;
    body.appendChild(row);
  });

  totalEl.innerText = `₹${total}`;
  modal.style.display = "flex";
}

function closeModal() {
  const modal = document.getElementById("dayModal");
  if (modal) modal.style.display = "none";
}

/* ================================
   🧭 NAV
================================ */
function onPrevMonth() {
  currentMonth--;
  if (currentMonth < 0) {
    currentMonth = 11;
    currentYear--;
  }
  drawCalendar(currentMonth, currentYear);
  loadMonthlyIncome();
}

function onNextMonth() {
  currentMonth++;
  if (currentMonth > 11) {
    currentMonth = 0;
    currentYear++;
  }
  drawCalendar(currentMonth, currentYear);
  loadMonthlyIncome();
}

function getMonthName(m) {
  return [
    "January","February","March","April","May","June",
    "July","August","September","October","November","December"
  ][m];
}

function pad(n) {
  return n < 10 ? "0" + n : n;
}

/* ================================
   📊 SUMMARY PAGE
================================ */
function loadSummaryData() {
  fetch(`${API_BASE}/expenses?page=0&size=10000&username=${currentUsername}`, {
    headers: { Authorization: `Bearer ${token}` }
  })
    .then(res => res.json())
    .then(data => {
      const expenses = data.expenses || [];
      renderYearlySummary(expenses);
      renderYearComparisonChart(expenses);
    })
    .catch(err => {
      console.error("Failed to load summary:", err);
    });
}

function renderYearlySummary(expenses) {
  const container = document.getElementById("yearlyBreakdown");
  if (!container) return;

  const yearlyData = {};

  expenses.forEach(e => {
    const date = new Date(e.date);
    const year = date.getFullYear();
    const month = date.getMonth();

    if (!yearlyData[year]) {
      yearlyData[year] = {
        total: 0,
        months: Array(12).fill(0)
      };
    }

    yearlyData[year].total += Number(e.amount);
    yearlyData[year].months[month] += Number(e.amount);
  });

  const years = Object.keys(yearlyData).sort((a, b) => b - a);

  container.innerHTML = "";

  if (years.length === 0) {
    container.innerHTML = `
      <div class="overview-card" style="text-align:center;padding:40px;">
        <p style="color:var(--muted);">No expenses yet. Start adding expenses to see your summary!</p>
      </div>
    `;
    return;
  }

  years.forEach(year => {
    const data = yearlyData[year];

    const yearSection = document.createElement("div");
    yearSection.className = "year-section";

    yearSection.innerHTML = `
      <div class="year-header" onclick="toggleYear('year-${year}')">
        <div>
          <div class="year-title">📊 ${year}</div>
        </div>
        <div style="display:flex;align-items:center;gap:20px;">
          <div class="year-total">₹ ${data.total.toLocaleString()}</div>
          <span class="expand-icon" id="icon-${year}">▼</span>
        </div>
      </div>

      <div class="year-content expanded" id="year-${year}">
        <table class="month-table">
          <thead>
            <tr>
              <th>Month</th>
              <th>Expenses</th>
            </tr>
          </thead>
          <tbody>
            ${data.months.map((amount, idx) => `
              <tr>
                <td>${getMonthName(idx)}</td>
                <td>₹ ${amount.toLocaleString()}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;

    container.appendChild(yearSection);
  });
}

function toggleYear(yearId) {
  const content = document.getElementById(yearId);
  const icon = document.getElementById(`icon-${yearId.replace('year-', '')}`);

  if (content.classList.contains("expanded")) {
    content.classList.remove("expanded");
    content.classList.add("collapsed");
    icon.classList.remove("rotated");
  } else {
    content.classList.remove("collapsed");
    content.classList.add("expanded");
    icon.classList.add("rotated");
  }
}

function renderYearComparisonChart(expenses) {
  const canvas = document.getElementById("yearComparisonChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  const yearTotals = {};
  expenses.forEach(e => {
    const year = new Date(e.date).getFullYear();
    yearTotals[year] = (yearTotals[year] || 0) + Number(e.amount);
  });

  const years = Object.keys(yearTotals).sort();
  const amounts = years.map(y => yearTotals[y]);

  if (yearComparisonChartInstance) {
    yearComparisonChartInstance.destroy();
  }

  yearComparisonChartInstance = new Chart(ctx, {
    type: "bar",
    data: {
      labels: years,
      datasets: [{
        label: "Total Spending",
        data: amounts,
        backgroundColor: "#6366f1",
        borderRadius: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: { display: false }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: function(value) {
              return '₹' + value.toLocaleString();
            }
          }
        }
      }
    }
  });
}

/* ================================
   🌙 THEME
================================ */
function toggleTheme() {
  document.body.classList.toggle("light");
}

/* ================================
   🤖 CHATBOT
================================ */
let chatPanelOpen = false;

function toggleChatPanel() {
  const panel = document.getElementById("chatPanel");
  const btn = document.getElementById("chatToggleBtn");
  if (!panel) return;

  chatPanelOpen = !chatPanelOpen;
  panel.style.display = chatPanelOpen ? "flex" : "none";

  // Highlight sidebar nav
  const navChat = document.getElementById("nav-chatbot");
  if (navChat) {
    if (chatPanelOpen) {
      navChat.classList.add("active");
    } else {
      navChat.classList.remove("active");
    }
  }

  if (chatPanelOpen) {
    const input = document.getElementById("chatInput");
    if (input) input.focus();
    scrollChatToBottom();
  }
}

// Show floating button after login
const originalShowDashboardFn = showDashboard;
showDashboard = function() {
  originalShowDashboardFn();
  const btn = document.getElementById("chatToggleBtn");
  if (btn) btn.style.display = "block";
};

function sendSuggestion(text) {
  const input = document.getElementById("chatInput");
  if (input) input.value = text;
  sendChatMessage();
}

function sendChatMessage() {
  const input = document.getElementById("chatInput");
  if (!input) return;

  const message = input.value.trim();
  if (!message) return;

  // Add user bubble
  addChatBubble("user", message);
  input.value = "";

  // Show typing indicator
  showTypingIndicator();

  // Call API
  fetch(`${API_BASE}/api/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: currentUsername,
      message: message
    })
  })
    .then(res => {
      if (!res.ok) throw new Error("Chat request failed");
      return res.json();
    })
    .then(data => {
      removeTypingIndicator();
      addChatBubble("bot", data.reply, data.detectedIntent, data.confidence);
      updateSuggestions(data.suggestions || []);
    })
    .catch(err => {
      removeTypingIndicator();
      addChatBubble("bot", "😅 Sorry, something went wrong. Please try again.");
      console.error("Chat error:", err);
    });
}

function addChatBubble(role, text, intent, confidence) {
  const container = document.getElementById("chatMessages");
  if (!container) return;

  const bubble = document.createElement("div");
  bubble.className = `chat-bubble ${role}`;

  const now = new Date();
  const timeStr = now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

  const avatar = role === "bot" ? "🤖" : "👤";

  let intentBadge = "";
  if (intent && role === "bot") {
    const pct = confidence ? Math.round(confidence * 100) : 0;
    intentBadge = `<div class="intent-badge">${intent} • ${pct}%</div>`;
  }

  bubble.innerHTML = `
    <span class="bubble-avatar">${avatar}</span>
    <div class="bubble-content">
      ${escapeHtml(text)}
      ${intentBadge}
      <div class="bubble-time">${timeStr}</div>
    </div>
  `;

  container.appendChild(bubble);
  scrollChatToBottom();
}

function showTypingIndicator() {
  const container = document.getElementById("chatMessages");
  if (!container) return;

  const typing = document.createElement("div");
  typing.className = "chat-bubble bot";
  typing.id = "typingIndicator";
  typing.innerHTML = `
    <span class="bubble-avatar">🤖</span>
    <div class="bubble-content">
      <div class="typing-indicator">
        <span></span><span></span><span></span>
      </div>
    </div>
  `;
  container.appendChild(typing);
  scrollChatToBottom();
}

function removeTypingIndicator() {
  const typing = document.getElementById("typingIndicator");
  if (typing) typing.remove();
}

function updateSuggestions(suggestions) {
  const container = document.getElementById("chatSuggestions");
  if (!container) return;

  container.innerHTML = "";
  suggestions.forEach(s => {
    const btn = document.createElement("button");
    btn.textContent = s;
    btn.onclick = () => sendSuggestion(s);
    container.appendChild(btn);
  });
}

function scrollChatToBottom() {
  const container = document.getElementById("chatMessages");
  if (container) {
    setTimeout(() => {
      container.scrollTop = container.scrollHeight;
    }, 50);
  }
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}