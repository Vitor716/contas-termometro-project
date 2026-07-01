const API = {
    entries:      "/api/lancamentos",
    recorrencias: "/api/recorrencias",
    summary:      "/api/orcamentos/mensal",
    annualSummary:"/api/orcamentos/anual",
    imports:      "/api/importacoes",
    metas:        "/api/configuracao/metas",
    termometro:   "/api/configuracao/termometro",
    snapshot:     "/api/configuracoes/termometros/snapshots"
};

const VIEWS = new Set(["dashboard", "lancamentos", "anual", "importacao", "metas"]);

const TYPE_META = {
    ENTRADA:     { label: "Entrada",      className: "income",     icon: "icon-arrow-down", sign:  1 },
    SAIDA_FIXA:  { label: "Saída fixa",   className: "fixed",      icon: "icon-home",       sign: -1 },
    GASTO_DIARIO:{ label: "Gasto diário", className: "daily",      icon: "icon-wallet",     sign: -1 },
    INVESTIMENTO:{ label: "Investimento", className: "investment",  icon: "icon-leaf",       sign: -1 },
    AJUSTE_SALDO:{ label: "Ajuste",       className: "adjustment", icon: "icon-sliders",    sign:  1 }
};

const state = {
    month: currentYearMonth(),
    annualYear: new Date().getFullYear(),
    entries: [],
    summary: null,
    annualMonths: [],
    imports: [],
    selectedFile: null,
    pendingConfirmation: null,
    activeView: "dashboard",
    showOnlyRecurring: false       // ← filtro de recorrentes
};

const moneyFormatter   = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const percentFormatter = new Intl.NumberFormat("pt-BR", { style: "percent", minimumFractionDigits: 0, maximumFractionDigits: 1 });
const monthFormatter   = new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" });
const dateFormatter    = new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "short" });

/* ── Alpine Shell ──────────────────────────────────────────── */
document.addEventListener("alpine:init", () => {
    Alpine.data("appShell", () => ({
        view: "dashboard",
        sidebarOpen: false,
        init() {
            window.addEventListener("app:navigate", event => {
                this.view = event.detail.view;
                this.sidebarOpen = false;
                if (this.view === "importacao") loadImports();
                if (this.view === "anual")      loadAnnual();
                if (this.view === "metas")      loadMetaDoMes();
                window.scrollTo({ top: 0, behavior: "smooth" });
            });
        },
        navigate(view)              { navigate(view); },
        goBack()                    { goBack(); },
        showEntries(type, label) {
            window.dispatchEvent(new CustomEvent("app:filter-entries", { detail: { type, label } }));
            this.navigate("lancamentos");
        },
        showAllEntries() {
            window.dispatchEvent(new CustomEvent("app:filter-entries", { detail: { type: "", label: "" } }));
            this.navigate("lancamentos");
        }
    }));
});

/* ── Init ──────────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", init);

function init() {
    byId("month-picker").value = state.month;
    bindNavigation();
    bindBrowserNavigation();
    bindMonthControls();
    bindEntryForm();
    bindEntryFilters();
    bindImport();
    bindAnnual();
    bindConfirmation();
    bindMetaForm();
    updateMonthLabels();
    loadMonth();
    checkHealth();
    bindTermometroForm();
    loadTermometro();
}

/* ── Termômetro Config ─────────────────────────────────────── */
function bindTermometroForm() {
    byId("termometro-form")?.addEventListener("submit", salvarTermometro);
}

async function loadTermometro() {
    try {
        const config = await apiFetch(API.termometro);
        if (config) {
            byId("termometro-id").value = config.id;
            byId("term-reserva").value        = String(config.reservaMinimaIntocavel).replace(".", ",");
            byId("term-comprometimento").value = String(config.comprometimentoMaximoRenda * 100).replace(".", ",");
            byId("term-margem").value          = String(config.margemSeguranca * 100).replace(".", ",");
            byId("term-estrategia").value      = config.estrategia;

            byId("term-diario")?.setAttribute("value",
                String(config.orcamentoDiarioMinimo).replace(".", ",")
            );
            // Set value directly (the field may already exist)
            const diarioEl = byId("term-diario");
            if (diarioEl) diarioEl.value = String(config.orcamentoDiarioMinimo).replace(".", ",");

            setText("snapshot-estrategia", config.estrategia);
        }
    } catch {
        console.warn("Configuração inicial do termômetro não encontrada.");
    }
}

async function salvarTermometro(event) {
    event.preventDefault();
    const id = byId("termometro-id").value;
    const diarioEl = byId("term-diario");
    const diarioValor = diarioEl ? parseMoney(diarioEl.value) : 0;

    const payload = {
        reservaMinimaIntocavel:    parseMoney(byId("term-reserva").value),
        orcamentoDiarioMinimo:     diarioValor,
        comprometimentoMaximoRenda: parseMoney(byId("term-comprometimento").value) / 100,
        margemSeguranca:           parseMoney(byId("term-margem").value) / 100,
        estrategia:                byId("term-estrategia").value
    };

    const method = id ? "PUT" : "POST";
    const url    = id ? `${API.termometro}/${id}` : API.termometro;

    try {
        await apiFetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        toast("Regras do termômetro salvas com sucesso!");
        await loadTermometro();
    } catch (e) {
        toast(e.message || "Erro ao salvar termômetro.", "error");
    }
}

/* ── Meta do Mês ───────────────────────────────────────────── */
function bindMetaForm() {
    byId("meta-form")?.addEventListener("submit", salvarMeta);
}

async function loadMetaDoMes() {
    setMetaLoading(true);
    try {
        const meta = await apiFetch(`${API.metas}?mes=${state.month}`);
        if (meta) {
            byId("meta-id").value             = meta.id;
            byId("meta-percentual").value     = (number(meta.percentualMetaInvestimento) * 100).toString().replace(".", ",");
            byId("meta-diario-minimo").value  = number(meta.orcamentoDiarioMinimo).toString().replace(".", ",");
            byId("meta-motivo").value         = meta.motivo || "";
            setText("meta-status-vigencia", `Meta ativa vigente desde: ${formatarDataIso(meta.vigenteDesde)}`);
        } else {
            limparFormularioMeta();
        }
    } catch (error) {
        limparFormularioMeta();
        if (!error.message.includes("404")) {
            toast("Erro ao carregar meta do mês.", "error");
        }
    } finally {
        setMetaLoading(false);
    }
}

function limparFormularioMeta() {
    byId("meta-form")?.reset();
    byId("meta-id").value = "";
    setText("meta-status-vigencia", "Nenhuma meta customizada definida. Usando padrão (20%).");
}

function setMetaLoading(loading) {
    const loader  = byId("meta-loading");
    if (loader) loader.hidden = !loading;
}

async function salvarMeta(event) {
    event.preventDefault();
    const form    = event.currentTarget;
    const button  = form.querySelector("button[type='submit']");
    const original = button.innerHTML;
    const id       = byId("meta-id").value;
    const pctRaw   = parseMoney(byId("meta-percentual").value) / 100;
    const diarioRaw = parseMoney(byId("meta-diario-minimo").value);

    if (pctRaw < 0 || pctRaw > 1) {
        toast("O percentual deve estar entre 0% e 100%.", "error");
        return;
    }

    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Salvando...`;

    const payload = {
        percentualMetaInvestimento: pctRaw,
        orcamentoDiarioMinimo:      diarioRaw,
        motivo:                     nullIfBlank(byId("meta-motivo").value)
    };

    const url    = id ? `${API.metas}/${id}` : `${API.metas}?mes=${state.month}`;
    const method = id ? "PUT" : "POST";

    try {
        await apiFetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        toast(id ? "Meta mensal atualizada com sucesso." : "Meta mensal parametrizada com sucesso.");
        await loadMetaDoMes();
        await loadMonth();
    } catch (error) {
        toast(error.message || "Não foi possível salvar a meta.", "error");
    } finally {
        button.disabled  = false;
        button.innerHTML = original;
    }
}

function formatarDataIso(isoString) {
    if (!isoString) return "—";
    return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" })
        .format(new Date(isoString));
}

/* ── Annual ────────────────────────────────────────────────── */
function bindAnnual() {
    const yearPicker = byId("year-picker");
    if (yearPicker) {
        yearPicker.value = state.annualYear;
        yearPicker.addEventListener("change", event => {
            const year = Number(event.target.value);
            if (!Number.isInteger(year) || year < 2000 || year > 2100) return;
            state.annualYear = year;
            setText("annual-year-label", year);
            loadAnnual();
        });
    }
    setText("annual-year-label", state.annualYear);
    byId("previous-year")?.addEventListener("click", () => changeAnnualYear(-1));
    byId("next-year")?.addEventListener("click", () => changeAnnualYear(1));
    byId("refresh-annual")?.addEventListener("click", loadAnnual);
}

function changeAnnualYear(offset) {
    state.annualYear += offset;
    const picker = byId("year-picker");
    if (picker) picker.value = state.annualYear;
    setText("annual-year-label", state.annualYear);
    loadAnnual();
}

async function loadAnnual() {
    setAnnualLoading(true);
    try {
        const response = await apiFetch(`${API.annualSummary}?ano=${state.annualYear}`);
        state.annualMonths = response.meses || [];
        renderAnnual();
    } catch (error) {
        state.annualMonths = [];
        renderAnnual();
        toast(error.message || "Não foi possível consolidar o ano.", "error");
    } finally {
        setAnnualLoading(false);
    }
}

function emptySummary() {
    return {
        somaEntradas: 0, somaSaidasFixas: 0, totalGastoDiario: 0,
        totalInvestido: 0, saidaTotal: 0, saldoMes: 0,
        porcentagemInvestida: 0, metaInvestimento: 0,
        performanceContraMeta: 0, gastoDiarioEsperadoAtual: 0, gastoDiarioRestante: 0
    };
}

function setAnnualLoading(loading) {
    const loader = byId("annual-loading");
    if (loader) loader.hidden = !loading;
    document.querySelectorAll(".annual-metrics, .annual-grid, .annual-months-panel").forEach(el => {
        if (el) el.hidden = loading;
    });
}

function renderAnnual() {
    const months = state.annualMonths.length
        ? state.annualMonths
        : Array.from({ length: 12 }, (_, i) => ({
            month: `${state.annualYear}-${String(i + 1).padStart(2, "0")}`,
            entriesCount: 0,
            ...emptySummary()
        }));

    const total       = key => months.reduce((sum, m) => sum + number(m[key]), 0);
    const income      = total("somaEntradas");
    const out         = total("saidaTotal");
    const invested    = total("totalInvestido");
    const balance     = total("saldoMes");
    const activeMonths = months.filter(m => m.entriesCount > 0).length;
    const divisor     = activeMonths || 12;

    setText("annual-income",          money(income));
    setText("annual-income-average",  `Média mensal: ${money(income / divisor)}`);
    setText("annual-out",             money(out));
    setText("annual-out-average",     `Média mensal: ${money(out / divisor)}`);
    setText("annual-invested",        money(invested));
    setText("annual-invested-rate",   `${percent(income > 0 ? invested / income : 0)} das entradas`);
    setText("annual-balance",         money(balance));
    setText("annual-active-months",   `${activeMonths} ${activeMonths === 1 ? "mês com movimentação" : "meses com movimentação"}`);

    renderAnnualChart(months);
    renderAnnualHighlights(months);
    renderAnnualMonths(months);
}

function renderAnnualHighlights(months) {
    const active = months.filter(m => m.entriesCount > 0);
    setAnnualHighlight("annual-best-income",      "annual-best-income-value",      maxMonth(active, "somaEntradas"),  "somaEntradas");
    setAnnualHighlight("annual-best-investment",  "annual-best-investment-value",  maxMonth(active, "totalInvestido"),"totalInvestido");
    setAnnualHighlight("annual-highest-out",      "annual-highest-out-value",      maxMonth(active, "saidaTotal"),    "saidaTotal");
}

function renderAnnualChart(months) {
    const maxValue = Math.max(1, ...months.flatMap(m => [
        number(m.somaEntradas), number(m.saidaTotal), number(m.totalInvestido)
    ]));

    const chartEl = byId("annual-chart");
    if (!chartEl) return;

    chartEl.innerHTML = months.map(m => {
        const date    = monthToDate(m.month);
        const label   = new Intl.DateTimeFormat("pt-BR", { month: "short" }).format(date).replace(".", "");
        const entradas = number(m.somaEntradas);
        const saidas   = number(m.saidaTotal);
        const investido = number(m.totalInvestido);

        const bars = [["income", entradas], ["out", saidas], ["investment", investido]]
            .map(([cls, val]) =>
                `<i class="chart-bar ${cls}" style="height:${val > 0 ? Math.max(val / maxValue * 100, 2) : 0}%"></i>`
            ).join("");

        const tooltip = `
            <div class="chart-tooltip">
                <strong>${monthName(m.month)}</strong>
                <div class="tooltip-row"><i class="income"></i> Entradas: <span>${money(entradas)}</span></div>
                <div class="tooltip-row"><i class="out"></i> Saídas: <span>${money(saidas)}</span></div>
                <div class="tooltip-row"><i class="investment"></i> Investido: <span>${money(investido)}</span></div>
            </div>`;

        return `<div class="chart-month" tabindex="0">
            ${tooltip}
            <div class="chart-bars">${bars}</div>
            <span>${label}</span>
        </div>`;
    }).join("");
}

function maxMonth(months, key) {
    return months.reduce((best, m) => (!best || number(m[key]) > number(best[key])) ? m : best, null);
}

function setAnnualHighlight(labelId, valueId, item, key) {
    setText(labelId, item ? monthName(item.month) : "—");
    setText(valueId, money(item ? item[key] : 0));
}

function renderAnnualMonths(months) {
    const container = byId("annual-months-grid");
    if (!container) return;

    container.innerHTML = months.map(m => {
        const balance   = number(m.saldoMes);
        const fixedOut  = number(m.somaSaidasFixas);

        // Negative caused mainly by fixed recurring expenses → amber (informative), not red (alarming)
        const isRecurringNeg = balance < 0 && fixedOut >= Math.abs(balance) * 0.5;
        const balanceCls     = balance < 0
            ? (isRecurringNeg ? "recurring-negative" : "negative")
            : "";
        const recurNote      = isRecurringNeg
            ? `<small class="month-recurring-note"><svg aria-hidden="true"><use href="#icon-refresh"></use></svg> inclui recorrentes</small>`
            : "";

        return `<article class="annual-month-card ${m.entriesCount ? "" : "is-empty"}"
                         role="button" tabindex="0" data-annual-month="${m.month}">
            <header>
                <strong>${monthName(m.month)}</strong>
                <span>${m.entriesCount} ${m.entriesCount === 1 ? "lançamento" : "lançamentos"}</span>
            </header>
            <strong class="month-balance ${balanceCls}">${money(balance)}</strong>
            ${recurNote}
            <div class="month-summary">
                <span>Entradas<strong>${money(m.somaEntradas)}</strong></span>
                <span>Saídas<strong>${money(m.saidaTotal)}</strong></span>
                <span>Investido<strong>${money(m.totalInvestido)}</strong></span>
                <span>Gasto diário<strong>${money(m.totalGastoDiario)}</strong></span>
            </div>
        </article>`;
    }).join("");

    container.querySelectorAll("[data-annual-month]").forEach(card => {
        const open = () => openAnnualMonth(card.dataset.annualMonth);
        card.addEventListener("click", open);
        card.addEventListener("keydown", event => {
            if (event.key === "Enter" || event.key === " ") { event.preventDefault(); open(); }
        });
    });
}

function openAnnualMonth(month) {
    state.month = month;
    const picker = byId("month-picker");
    if (picker) picker.value = month;
    const typeFilter = byId("entry-type-filter");
    if (typeFilter) typeFilter.value = "";
    updateEntryFilterContext("", "");
    updateMonthLabels();
    loadMonth();
    navigate("lancamentos");
}

/* ── Navigation ────────────────────────────────────────────── */
function bindNavigation() {
    window.addEventListener("app:filter-entries", event => {
        const { type = "", label = "" } = event.detail || {};
        const typeFilter = byId("entry-type-filter");
        if (typeFilter) typeFilter.value = type;
        updateEntryFilterContext(type, label);
        renderEntries();
    });

    byId("clear-entry-context")?.addEventListener("click", () => {
        const typeFilter = byId("entry-type-filter");
        if (typeFilter) typeFilter.value = "";
        updateEntryFilterContext("", "");
        renderEntries();
    });
}

function bindBrowserNavigation() {
    const initialView = viewFromHash();
    if (location.hash !== `#${initialView}`) {
        history.replaceState({ view: initialView }, "", `#${initialView}`);
    }
    navigate(initialView, { replace: true });
    window.addEventListener("popstate", event => {
        navigate(event.state?.view || viewFromHash(), { fromHistory: true });
    });
    window.addEventListener("hashchange", () => {
        navigate(viewFromHash(), { fromHistory: true });
    });
}

function navigate(view, options = {}) {
    const target = VIEWS.has(view) ? view : "dashboard";
    if (!options.fromHistory) {
        const method = options.replace ? "replaceState" : "pushState";
        if (location.hash !== `#${target}` || history.state?.view !== target) {
            history[method]({ view: target, previousView: state.activeView }, "", `#${target}`);
        }
    }
    state.activeView = target;
    window.dispatchEvent(new CustomEvent("app:navigate", { detail: { view: target } }));
}

function goBack() {
    if (history.state?.previousView) { history.back(); return; }
    navigate("dashboard");
}

function viewFromHash() {
    const value = location.hash.replace(/^#/, "");
    return VIEWS.has(value) ? value : "dashboard";
}

/* ── Month Controls ────────────────────────────────────────── */
function bindMonthControls() {
    byId("month-picker")?.addEventListener("change", event => {
        if (!event.target.value) return;
        state.month = event.target.value;
        updateMonthLabels();
        loadMonth();
    });
    byId("previous-month")?.addEventListener("click", () => changeMonth(-1));
    byId("next-month")?.addEventListener("click",    () => changeMonth(1));
    byId("refresh-dashboard")?.addEventListener("click", loadMonth);
}

function changeMonth(offset) {
    const [year, month] = state.month.split("-").map(Number);
    const date = new Date(year, month - 1 + offset, 1);
    state.month = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    const picker = byId("month-picker");
    if (picker) picker.value = state.month;
    updateMonthLabels();
    loadMonth();
}

function updateMonthLabels() {
    const date = monthToDate(state.month);
    setText("dashboard-month-name", monthFormatter.format(date));
}

/* ── Load Month ────────────────────────────────────────────── */
async function loadMonth() {
    setDashboardLoading(true);
    try {
        const [summary, entries] = await Promise.all([
            apiFetch(`${API.summary}?mes=${summaryReferenceDate(state.month)}`),
            apiFetch(`${API.entries}?mes=${state.month}`)
        ]);
        state.summary = summary;
        state.entries = Array.isArray(entries) ? entries : [];
        renderDashboard();
        renderEntries();
        await loadSnapshot();
    } catch (error) {
        state.summary = null;
        state.entries = [];
        renderDashboard();
        renderEntries();
        toast(error.message || "Não foi possível carregar o mês.", "error");
    } finally {
        setDashboardLoading(false);
    }
}

async function loadSnapshot() {
    try {
        const snapshot = await apiFetch(`${API.snapshot}/${state.month}`);
        const statusEl = byId("temperature-status");
        if (!statusEl) return;
        statusEl.className = "status-pill";

        if (snapshot.statusAtual === "VERDE") {
            statusEl.classList.add("good");
            statusEl.textContent = "Saudável";
            setText("temperature-description", "Seus investimentos e gastos estão dentro do seguro.");
        } else if (snapshot.statusAtual === "AMARELO") {
            statusEl.classList.add("warning");
            statusEl.textContent = "Aguardando Aporte";
            setText("temperature-description", "Você tem margem no orçamento. Faça seu investimento para bater a meta do mês!");
        } else {
            statusEl.classList.add("danger");
            statusEl.textContent = "Alerta Crítico";
            setText("temperature-description", "Seu ritmo de gastos comprometeu a sua meta ou seu limite diário.");
        }
    } catch {
        // Status permanece como calculado por renderTemperature()
    }
}

function setDashboardLoading(loading) {
    const loader = byId("dashboard-loading");
    if (loader) loader.hidden = !loading;
    document.querySelectorAll("#dashboard-content .metric-grid, #dashboard-content .dashboard-grid, #dashboard-content .recent-panel").forEach(el => {
        if (el) el.hidden = loading;
    });
}

/* ── Render Dashboard ──────────────────────────────────────── */
function renderDashboard() {
    const s = state.summary || {};

    setText("metric-income",  money(s.somaEntradas));
    setText("metric-fixed",   money(s.somaSaidasFixas));
    setText("metric-daily",   money(s.totalGastoDiario));
    setText("metric-balance", money(s.saldoMes));

    const metaValorReais = number(s.somaEntradas) * normalizeRate(s.metaInvestimento);
    setText("thermo-invested",    money(s.totalInvestido));
    setText("thermo-goal",        money(metaValorReais));
    setText("thermo-total-out",   money(s.saidaTotal));
    setText("metric-daily-remaining", money(s.gastoDiarioRestante));
    setText("daily-spent",    money(s.totalGastoDiario));
    setText("daily-expected", money(s.gastoDiarioEsperadoAtual));

    // Balance caption
    const balance = number(s.saldoMes);
    setText("balance-caption", balance < 0 ? "Seu mês está no negativo" : "Resultado até agora");

    // Temperature gauge
    const performance = normalizeRate(s.performanceContraMeta);
    renderTemperature(performance, number(s.somaEntradas) > 0);

    // Daily progress bar
    const spent    = Math.max(0, number(s.totalGastoDiario));
    const expected = Math.max(0, number(s.gastoDiarioEsperadoAtual));
    const ratio    = expected > 0 ? spent / expected : 0;

    const barFill = byId("daily-progress");
    if (barFill) {
        barFill.style.width      = `${Math.min(ratio * 100, 100)}%`;
        barFill.style.background = ratio > 1 ? "var(--red)" : ratio > 0.8 ? "var(--amber)" : "var(--green)";
    }
    setText("daily-progress-label", expected <= 0
        ? "Aguardando dados do orçamento"
        : ratio <= 1
            ? `${percent(ratio)} do ritmo esperado utilizado`
            : `${percent(ratio - 1)} acima do ritmo esperado`
    );

    // Delta informativo: quanto abaixo/acima do ritmo ideal
    const deltaEl = byId("daily-delta");
    if (deltaEl) {
        if (expected > 0) {
            const delta = expected - spent;
            deltaEl.hidden    = false;
            deltaEl.className = `daily-delta ${delta >= 0 ? "ahead" : "behind"}`;
            deltaEl.textContent = delta >= 0
                ? `↓ ${money(delta)} abaixo do ritmo — você está economizando ✓`
                : `↑ ${money(Math.abs(delta))} acima do ritmo — monitore os gastos`;
        } else {
            deltaEl.hidden = true;
        }
    }

    renderRecentEntries();
}

/* ── Temperature Gauge (novo design) ──────────────────────── */
function renderTemperature(performance, hasIncome) {
    const statusEl   = byId("temperature-status");
    if (!statusEl) return;
    statusEl.className = "status-pill";

    const fill       = byId("thermometer-fill");
    const pctDisplay = byId("metric-performance");

    if (!hasIncome) {
        setText("temperature-title",       "Comece pelas entradas");
        setText("temperature-description", "Registre uma entrada para o termômetro comparar seus investimentos com a meta mensal.");
        statusEl.textContent = "Sem dados";
        if (fill) { fill.style.width = "0%"; fill.style.background = "var(--ink-soft)"; }
        if (pctDisplay) { pctDisplay.textContent = "—"; pctDisplay.className = ""; }
        return;
    }

    // Fill grows left-to-right; cap at 100% visually
    const pct = Math.min(Math.max(performance, 0), 1) * 100;
    const fillColor = performance >= 1 ? "var(--green)"
        : performance >= 0.8 ? "var(--amber)"
            : "var(--red)";

    if (fill) {
        fill.style.width      = `${Math.min(pct, 100)}%`;
        fill.style.background = fillColor;
        fill.style.boxShadow  = performance >= 1
            ? "0 0 14px rgba(16,217,160,.35)"
            : performance >= 0.8
                ? "0 0 14px rgba(245,166,35,.3)"
                : "0 0 14px rgba(248,113,113,.25)";
    }

    if (pctDisplay) {
        pctDisplay.textContent = `${Math.round(pct)}%`;
        pctDisplay.className   = performance >= 1 ? "pct-good"
            : performance >= 0.8 ? "pct-warn"
                : "pct-danger";
    }

    if (performance >= 1) {
        statusEl.classList.add("good");
        statusEl.textContent = "Saudável";
        setText("temperature-title",       "Meta alcançada! 🎯");
        setText("temperature-description", "O investimento planejado foi alcançado. Preserve esse resultado ao tomar novas decisões.");
    } else if (performance >= 0.8) {
        statusEl.classList.add("warning");
        statusEl.textContent = "Atenção";
        setText("temperature-title",       "Você está perto da meta");
        setText("temperature-description", "O ritmo é positivo. Revise os gastos variáveis antes de assumir um novo compromisso.");
    } else {
        statusEl.classList.add("danger");
        statusEl.textContent = "Crítico";
        setText("temperature-title",       "Meta em risco");
        setText("temperature-description", "O valor investido ainda está abaixo do planejado. Priorize a margem restante do mês.");
    }
}

/* ── Recent Entries ────────────────────────────────────────── */
function renderRecentEntries() {
    const container = byId("recent-list");
    if (!container) return;

    const recent = [...state.entries]
        .sort((a, b) => String(b.data).localeCompare(String(a.data)) || b.id - a.id)
        .slice(0, 5);

    if (!recent.length) {
        container.innerHTML = `<div class="empty-state"><p>Nenhuma movimentação registrada neste mês.</p></div>`;
        return;
    }
    container.innerHTML = recent.map(entry => {
        const meta = typeMeta(entry.tipo);
        return `<div class="transaction-row">
            <span class="transaction-symbol ${meta.className}">${icon(meta.icon)}</span>
            <div class="transaction-copy">
                <strong>${escapeHtml(entry.descricao)}</strong>
                <small>${formatDate(entry.data)} · ${escapeHtml(entry.categoria || meta.label)}</small>
            </div>
            <strong class="transaction-amount ${meta.className}">${entrySign(entry)}${money(entry.valor)}</strong>
        </div>`;
    }).join("");
}

/* ── Entry Filters ─────────────────────────────────────────── */
function bindEntryFilters() {
    byId("entry-search")?.addEventListener("input", renderEntries);

    byId("entry-type-filter")?.addEventListener("change", event => {
        const type = event.target.value;
        updateEntryFilterContext(type, type ? typeMeta(type).label : "");
        renderEntries();
    });

    // Recurring filter toggle
    byId("recurring-filter-toggle")?.addEventListener("click", event => {
        state.showOnlyRecurring = !state.showOnlyRecurring;
        const btn = event.currentTarget;
        btn.setAttribute("aria-pressed", String(state.showOnlyRecurring));
        btn.classList.toggle("is-active", state.showOnlyRecurring);
        renderEntries();
    });
}

function updateEntryFilterContext(type, label) {
    const context = byId("entry-filter-context");
    if (!context) return;
    context.hidden = !type;
    setText("entry-filter-label", type ? `Exibindo somente ${String(label).toLowerCase()} deste mês.` : "");
    setText("entries-title", type ? label : "Lançamentos do mês");
}

function renderEntries() {
    const search = normalizeText(byId("entry-search")?.value ?? "");
    const type   = byId("entry-type-filter")?.value ?? "";

    const filtered = [...state.entries]
        .filter(entry => !type || entry.tipo === type)
        .filter(entry => !state.showOnlyRecurring || (entry.recorrenciaId && !entry.recorrenciaExcecao))
        .filter(entry => !search || normalizeText(`${entry.descricao} ${entry.categoria || ""} ${entry.observacao || ""}`).includes(search))
        .sort((a, b) => String(b.data).localeCompare(String(a.data)) || b.id - a.id);

    const tbody = byId("entries-table-body");
    const empty = byId("entries-empty");
    if (!tbody || !empty) return;

    tbody.innerHTML = filtered.map(entry => {
        const meta         = typeMeta(entry.tipo);
        const isRecorrente = entry.recorrenciaId && !entry.recorrenciaExcecao;
        const badge        = isRecorrente ? `<span class="badge-recorrente" title="Recorrente">🔄</span>` : "";
        return `<tr>
            <td data-label="Data">${formatDate(entry.data)}</td>
            <td class="table-description" data-label="Descrição">
                <strong>${escapeHtml(entry.descricao)}</strong>${badge}
                <small>${escapeHtml(entry.observacao || "")}</small>
            </td>
            <td data-label="Tipo"><span class="type-badge ${meta.className}">${meta.label}</span></td>
            <td data-label="Categoria">${escapeHtml(entry.categoria || "—")}</td>
            <td class="amount-column" data-label="Valor"><strong class="${meta.className}">${entrySign(entry)}${money(entry.valor)}</strong></td>
            <td><div class="table-actions">
                <button class="icon-button" type="button" data-edit-entry="${entry.id}" aria-label="Editar ${escapeHtml(entry.descricao)}">${icon("icon-edit")}</button>
                <button class="icon-button delete" type="button" data-delete-entry="${entry.id}" aria-label="Excluir ${escapeHtml(entry.descricao)}">${icon("icon-trash")}</button>
            </div></td>
        </tr>`;
    }).join("");

    empty.hidden = filtered.length > 0;
    const tableWrap = document.querySelector(".table-wrap");
    if (tableWrap) tableWrap.hidden = filtered.length === 0;

    setText("entry-count", filtered.length === 1 ? "1 lançamento" : `${filtered.length} lançamentos`);
    const total = filtered.reduce((sum, e) => sum + number(e.valor) * typeMeta(e.tipo).sign, 0);
    setText("entry-net-total", `Saldo listado: ${money(total)}`);

    tbody.querySelectorAll("[data-edit-entry]").forEach(btn =>
        btn.addEventListener("click", () => openEntryDialog(Number(btn.dataset.editEntry)))
    );
    tbody.querySelectorAll("[data-delete-entry]").forEach(btn =>
        btn.addEventListener("click", () => requestEntryDeletion(Number(btn.dataset.deleteEntry)))
    );
}

/* ── Entry Form ────────────────────────────────────────────── */
function bindEntryForm() {
    ["new-entry-top", "new-entry-page", "new-entry-empty"].forEach(id =>
        byId(id)?.addEventListener("click", () => openEntryDialog())
    );
    byId("close-entry-dialog")?.addEventListener("click", closeEntryDialog);
    byId("cancel-entry")?.addEventListener("click", closeEntryDialog);
    byId("entry-form")?.addEventListener("submit", saveEntry);
}

function openEntryDialog(id = null) {
    const form             = byId("entry-form");
    const escopoContainer  = byId("escopo-edicao-container");
    const recurringSection = byId("recurring-section");   // ← declarado antes de qualquer uso
    if (!form) return;

    form.reset();
    byId("entry-id").value    = "";
    byId("entry-month").value = state.month;
    byId("entry-date").value  = defaultDateForMonth(state.month);
    setText("entry-dialog-title", "Novo lançamento");
    const saveBtn = byId("save-entry");
    if (saveBtn) saveBtn.innerHTML = "<span>Salvar lançamento</span>";

    // Sync Alpine checkbox state + garante disabled limpo após reset
    const recCheck = form.elements.recorrente;
    if (recCheck) {
        recCheck.checked  = false;
        recCheck.disabled = false;
        recCheck.dispatchEvent(new Event("change", { bubbles: true }));
    }

    if (id !== null) {
        // Edição: oculta seção de criação de recorrência
        if (recurringSection) recurringSection.hidden = true;
        const entry = state.entries.find(e => e.id === id);
        if (entry) {
            if (escopoContainer) escopoContainer.hidden = !entry.recorrenciaId;
            form.elements.tipo.value        = entry.tipo;
            byId("entry-description").value = entry.descricao;
            byId("entry-value").value       = String(entry.valor).replace(".", ",");
            byId("entry-date").value        = entry.data;
            byId("entry-category").value    = entry.categoria || "";
            byId("entry-month").value       = entry.mesReferencia;
            byId("entry-notes").value       = entry.observacao || "";
            setText("entry-dialog-title", "Editar lançamento");
            if (saveBtn) saveBtn.innerHTML = "<span>Salvar alterações</span>";

            // Mostra flag de recorrente (read-only) se o lançamento pertence a uma recorrência
            if (recurringSection && entry.recorrenciaId) {
                recurringSection.hidden = false;
                if (recCheck) {
                    recCheck.checked  = true;
                    recCheck.disabled = true;   // só leitura — escopo cuida da edição
                    recCheck.dispatchEvent(new Event("change", { bubbles: true }));
                }
            }
        } else {
            if (escopoContainer) escopoContainer.hidden = true;
        }
    } else {
        // Novo lançamento: exibe seção de recorrência disponível
        if (recurringSection) recurringSection.hidden = false;
        if (escopoContainer)  escopoContainer.hidden  = true;
    }

    byId("entry-dialog")?.showModal();
    setTimeout(() => byId("entry-description")?.focus(), 50);
}

function closeEntryDialog() {
    byId("entry-dialog")?.close();
}

async function saveEntry(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;

    const id           = byId("entry-id").value;
    const isRecorrente = !id && (form.elements.recorrente?.checked ?? false);

    const button   = byId("save-entry");
    const original = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Salvando`;

    const valorDecimal = parseMoney(byId("entry-value").value);
    if (!(valorDecimal > 0)) {
        toast("Informe um valor maior que zero.", "error");
        button.disabled  = false;
        button.innerHTML = original;
        return;
    }

    try {
        let recorrenciaId = null;

        // Passo 1 — criar a recorrência e capturar o id gerado
        if (isRecorrente) {
            const recorrenciaPayload = {
                tipo:            form.elements.tipo.value,
                descricao:       byId("entry-description").value.trim(),
                valorCentavos:   Math.round(valorDecimal * 100),
                categoria:       nullIfBlank(byId("entry-category").value),
                observacao:      nullIfBlank(byId("entry-notes").value),
                mesInicio:       form.elements.mesInicio?.value || "",
                mesFim:          nullIfBlank(form.elements.mesFim?.value),
                diaPreferencial: Number(form.elements.diaPreferencial?.value || 1),
                frequencia:      form.elements.frequencia?.value  || "MENSAL",
                status:          form.elements.statusRecorrencia?.value || "ATIVA",
            };

            const recorrencia = await apiFetch(API.recorrencias, {
                method:  "POST",
                headers: { "Content-Type": "application/json" },
                body:    JSON.stringify(recorrenciaPayload)
            });

            recorrenciaId = recorrencia?.id ?? null;
        }

        // Passo 2 — criar/editar o lançamento, vinculando ao id da recorrência se houver
        const lancamentoPayload = {
            tipo:          form.elements.tipo.value,
            descricao:     byId("entry-description").value.trim(),
            valor:         valorDecimal,
            data:          byId("entry-date").value,
            mesReferencia: byId("entry-month").value,
            categoria:     nullIfBlank(byId("entry-category").value),
            observacao:    nullIfBlank(byId("entry-notes").value),
            escopoEdicao:  form.elements.escopoEdicao?.value || null,
            recorrenciaId: recorrenciaId,
        };

        await apiFetch(id ? `${API.entries}/${id}` : API.entries, {
            method:  id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(lancamentoPayload)
        });

        closeEntryDialog();
        if (lancamentoPayload.mesReferencia !== state.month) {
            state.month = lancamentoPayload.mesReferencia;
            const picker = byId("month-picker");
            if (picker) picker.value = state.month;
            updateMonthLabels();
        }
        const msg = isRecorrente
            ? "Recorrência e lançamento criados."
            : id ? "Lançamento atualizado." : "Lançamento criado.";
        toast(msg);
        await loadMonth();
    } catch (error) {
        toast(error.message || "Não foi possível salvar o lançamento.", "error");
    } finally {
        button.disabled  = false;
        button.innerHTML = original;
    }
}

/* ── Delete Entry ──────────────────────────────────────────── */
function requestEntryDeletion(id) {
    const entry = state.entries.find(e => e.id === id);
    if (!entry) return;
    setText("confirm-title",   "Excluir lançamento?");
    setText("confirm-message", `"${entry.descricao}" será removido permanentemente.`);
    setText("confirm-action",  "Excluir");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.entries}/${id}`, { method: "DELETE" });
        toast("Lançamento excluído.");
        await loadMonth();
    };
    byId("confirm-dialog")?.showModal();
}

function bindConfirmation() {
    byId("confirm-dialog")?.addEventListener("close", async event => {
        if (event.target.returnValue !== "confirm" || !state.pendingConfirmation) {
            state.pendingConfirmation = null;
            return;
        }
        const action = state.pendingConfirmation;
        state.pendingConfirmation = null;
        try {
            await action();
        } catch (error) {
            toast(error.message || "Não foi possível concluir a exclusão.", "error");
        }
    });
}

/* ── Import ────────────────────────────────────────────────── */
function bindImport() {
    const input = byId("csv-file");
    const zone  = byId("upload-zone");
    if (!input || !zone) return;

    input.addEventListener("change", () => selectFile(input.files[0]));
    ["dragenter", "dragover"].forEach(name => zone.addEventListener(name, event => {
        event.preventDefault();
        zone.classList.add("is-dragging");
    }));
    ["dragleave", "drop"].forEach(name => zone.addEventListener(name, event => {
        event.preventDefault();
        zone.classList.remove("is-dragging");
    }));
    zone.addEventListener("drop", event => selectFile(event.dataTransfer.files[0]));
    byId("remove-file")?.addEventListener("click", () => selectFile(null));
    byId("upload-button")?.addEventListener("click", uploadCsv);
    byId("refresh-imports")?.addEventListener("click", loadImports);
}

function selectFile(file) {
    if (file && !file.name.toLowerCase().endsWith(".csv")) {
        toast("Escolha um arquivo no formato CSV.", "error");
        return;
    }
    state.selectedFile = file || null;
    const uploadZone   = byId("upload-zone");
    const selectedFile = byId("selected-file");
    const uploadBtn    = byId("upload-button");
    if (uploadZone)   uploadZone.hidden   = Boolean(file);
    if (selectedFile) selectedFile.hidden = !file;
    if (uploadBtn)    uploadBtn.disabled  = !file;
    if (file) {
        setText("selected-file-name", file.name);
        setText("selected-file-size", formatFileSize(file.size));
    } else {
        const csvFile = byId("csv-file");
        if (csvFile) csvFile.value = "";
    }
}

async function uploadCsv() {
    if (!state.selectedFile) return;
    const button   = byId("upload-button");
    const original = button.innerHTML;
    button.disabled  = true;
    button.innerHTML = `<span class="spinner"></span> Processando arquivo`;

    const data = new FormData();
    data.append("file", state.selectedFile);

    try {
        const result    = await apiFetch(`${API.imports}/nubank`, { method: "POST", body: data });
        const successes = result.sucessos?.length || 0;
        const failures  = result.falhas?.length || 0;
        toast(`${successes} lançamento(s) importado(s)${failures ? ` e ${failures} linha(s) rejeitada(s)` : ""}.`);
        selectFile(null);
        await Promise.all([loadImports(), loadMonth()]);
    } catch (error) {
        toast(error.message || "Não foi possível importar o arquivo.", "error");
    } finally {
        button.disabled  = !state.selectedFile;
        button.innerHTML = original;
    }
}

async function loadImports() {
    const container = byId("imports-list");
    if (container) container.innerHTML = `<div class="loading-panel"><span class="spinner"></span> Carregando...</div>`;

    try {
        const imports   = await apiFetch(API.imports);
        state.imports   = Array.isArray(imports) ? imports : [];
        renderImports();
    } catch (error) {
        if (container) {
            container.innerHTML = `<div class="empty-state"><p>${escapeHtml(error.message || "Não foi possível carregar o histórico.")}</p></div>`;
        }
    }
}

function renderImports() {
    const container = byId("imports-list");
    if (!container) return;

    if (!state.imports.length) {
        container.innerHTML = `<div class="empty-state"><span>${icon("icon-file")}</span><h3>Nenhuma importação</h3><p>Os lotes enviados aparecerão aqui.</p></div>`;
        return;
    }
    container.innerHTML = [...state.imports].reverse().map(item => `<div class="import-item">
        <span>${icon("icon-file")}</span>
        <div>
            <strong>${escapeHtml(item.origem)} · ${escapeHtml(item.idLote)}</strong>
            <small>${item.qtdSucessos} importados · ${item.qtdFalhas} falhas · ${item.totalProcessado} linhas</small>
        </div>
        <button class="icon-button" type="button" data-delete-import="${escapeHtml(item.idLote)}" aria-label="Excluir lote">${icon("icon-trash")}</button>
    </div>`).join("");

    container.querySelectorAll("[data-delete-import]").forEach(btn =>
        btn.addEventListener("click", () => requestImportDeletion(btn.dataset.deleteImport))
    );
}

function requestImportDeletion(idLote) {
    setText("confirm-title",   "Excluir lote importado?");
    setText("confirm-message", "Todos os lançamentos criados por esta importação também serão removidos.");
    setText("confirm-action",  "Excluir lote");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.imports}/${encodeURIComponent(idLote)}`, { method: "DELETE" });
        toast("Lote e lançamentos removidos.");
        await Promise.all([loadImports(), loadMonth()]);
    };
    byId("confirm-dialog")?.showModal();
}

/* ── Health Check ──────────────────────────────────────────── */
async function checkHealth() {
    const status = byId("connection-status");
    try {
        await apiFetch("/api/sistema/saude");
        status?.classList.remove("is-offline");
        if (status?.lastChild) status.lastChild.textContent = " Local";
    } catch {
        status?.classList.add("is-offline");
        if (status?.lastChild) status.lastChild.textContent = " Indisponível";
    }
}

/* ── API Fetch ─────────────────────────────────────────────── */
async function apiFetch(url, options = {}) {
    const response = await fetch(url, options);
    if (response.ok) {
        if (response.status === 204) return null;
        const contentType = response.headers.get("content-type") || "";
        return contentType.includes("application/json") ? response.json() : response.text();
    }

    let message = `Erro ${response.status}`;
    try {
        const body = await response.json();
        message = body.detail || body.message || body.title || message;
        if (body.errors && typeof body.errors === "object") {
            message = Object.values(body.errors).join(" ");
        }
    } catch {
        const text = await response.text();
        if (text) message = text;
    }
    throw new Error(message);
}

/* ── Helpers ───────────────────────────────────────────────── */
function normalizeRate(value) { return number(value); }
function typeMeta(type) { return TYPE_META[type] || TYPE_META.AJUSTE_SALDO; }
function entrySign(entry) { return typeMeta(entry.tipo).sign > 0 ? "+ " : "− "; }

function currentYearMonth() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}
function monthToDate(month) {
    const [year, value] = month.split("-").map(Number);
    return new Date(year, value - 1, 1);
}
function monthName(month) {
    return new Intl.DateTimeFormat("pt-BR", { month: "long" }).format(monthToDate(month));
}
function defaultDateForMonth(month) {
    const now = new Date();
    if (month === currentYearMonth()) {
        return `${month}-${String(now.getDate()).padStart(2, "0")}`;
    }
    return `${month}-01`;
}
function summaryReferenceDate(month) {
    const [year, value] = month.split("-").map(Number);
    const now     = new Date();
    const isCurrent = month === currentYearMonth();
    const day     = isCurrent ? now.getDate() : new Date(year, value, 0).getDate();
    return `${month}-${String(day).padStart(2, "0")}`;
}

function money(value) { return moneyFormatter.format(number(value)); }
function percent(value) { return percentFormatter.format(number(value)); }
function number(value) { const p = Number(value); return Number.isFinite(p) ? p : 0; }

function parseMoney(value) {
    const cleaned = String(value).trim().replace(/\s/g, "").replace(/^R\$/, "");
    if (cleaned.includes(",")) return Number(cleaned.replace(/\./g, "").replace(",", "."));
    return Number(cleaned);
}
function formatDate(value) {
    if (!value) return "—";
    const [year, month, day] = value.split("-").map(Number);
    return dateFormatter.format(new Date(year, month - 1, day)).replace(".", "");
}
function formatFileSize(bytes) {
    if (bytes < 1024) return `${bytes} bytes`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1).replace(".", ",")} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1).replace(".", ",")} MB`;
}
function normalizeText(value) {
    return String(value || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}
function nullIfBlank(value) { const t = value.trim(); return t || null; }
function icon(id) { return `<svg aria-hidden="true"><use href="#${id}"></use></svg>`; }
function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, c =>
        ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" })[c]
    );
}
function toast(message, type = "success") {
    const el = document.createElement("div");
    el.className   = `toast ${type}`;
    el.textContent = message;
    byId("toast-region")?.appendChild(el);
    setTimeout(() => el.remove(), 4500);
}

/** Safe getElementById — returns null instead of throwing */
function byId(id) { return document.getElementById(id); }

/** Safe setText — no-op if element doesn't exist */
function setText(id, text) {
    const el = byId(id);
    if (el) el.textContent = text;
}