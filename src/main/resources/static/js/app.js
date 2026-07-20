const API = {
    entries:      "/api/lancamentos",
    recorrencias: "/api/recorrencias",
    summary:      "/api/orcamentos/mensal",
    annualSummary:"/api/orcamentos/anual",
    imports:      "/api/importacao",
    metas:        "/api/configuracao/metas",
    termometro:   "/api/configuracao/termometro",
    snapshot:     "/api/configuracoes/termometros/snapshots",
    projecoes:    "/api/projecoes",
    consultor:    "/api/consultor",
    backups:      "/api/backups"
};

const VIEWS = new Set(["dashboard", "lancamentos", "anual", "importacao", "metas", "compromissos", "projecoes", "consultor", "backups"]);

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
    annualSummary: null,
    annualMonths: [],
    imports: [],
    importPreview: null,
    recorrencias: [],
    parcelamentos: [],
    projecoes: [],
    consultorResultado: null,
    backupInfo: null,
    selectedBackupFile: null,
    selectedFile: null,
    selectedEntryIds: new Set(),
    importAbortController: null,
    pendingConfirmation: null,
    activeView: "dashboard",
    showOnlyRecurring: false
};

const moneyFormatter   = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const percentFormatter = new Intl.NumberFormat("pt-BR", { style: "percent", minimumFractionDigits: 0, maximumFractionDigits: 1 });
const monthFormatter   = new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" });
const dateFormatter    = new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "short" });

/* ── Alpine Shell ──────────────────────────────────────────── */
/* ── Alpine Shell ──────────────────────────────────────────── */
document.addEventListener("alpine:init", () => {

    // 1. COMPONENTE ORIGINAL (Não pode ser apagado, controla o menu e as telas)
    Alpine.data("appShell", () => ({
        view: "dashboard",
        sidebarOpen: false,
        init() {
            window.addEventListener("app:navigate", event => {
                this.view = event.detail.view;
                this.sidebarOpen = false;
                if (this.view === "importacao")    loadImports();
                if (this.view === "anual")          loadAnnual();
                if (this.view === "metas")          loadMetaDoMes();
                if (this.view === "compromissos")   loadCompromissos();
                if (this.view === "projecoes")      loadProjecoes();
                if (this.view === "consultor")      syncConsultorMonth();
                if (this.view === "backups")        loadBackupInfo();
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

    // 2. NOVO COMPONENTE (Para a formatação em Reais)
    Alpine.data("mascaraMoeda", () => ({
        valorLimpo: "",
        valorFormatado: "",

        mascarar(event) {
            let apenasDigitos = event.target.value.replace(/\D/g, "");
            this.formatar(apenasDigitos);
        },

        formatar(digitos) {
            if (!digitos || digitos === "0") {
                this.valorFormatado = "";
                this.valorLimpo = "";
                return;
            }

            const valorDecimal = parseInt(digitos, 10) / 100;
            this.valorLimpo = valorDecimal.toFixed(2);

            this.valorFormatado = new Intl.NumberFormat("pt-BR", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }).format(valorDecimal);
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
    bindBackups();
    bindAnnual();
    bindConfirmation();
    bindMetaForm();
    bindCompromissos();
    bindMvp6();
    updateMonthLabels();
    loadMonth();
    checkHealth();
    bindTermometroForm();
    loadTermometro();
}

/* ── Bind Compromissos Shell ───────────────────────────────── */
function bindCompromissos() {
    document.querySelectorAll(".comp-tab").forEach(btn =>
        btn.addEventListener("click", () => switchCompromissosTab(btn.dataset.tab))
    );
    byId("recorrencia-edit-form")?.addEventListener("submit", saveRecorrenciaEdit);
    byId("close-rec-edit-dialog")?.addEventListener("click", () =>
        byId("recorrencia-edit-dialog")?.close()
    );
    byId("close-rec-edit-dialog-foot")?.addEventListener("click", () =>
        byId("recorrencia-edit-dialog")?.close()
    );
    byId("refresh-compromissos")?.addEventListener("click", loadCompromissos);
}

function bindMvp6() {
    byId("refresh-projecoes")?.addEventListener("click", loadProjecoes);
    byId("projection-months")?.addEventListener("change", loadProjecoes);
    byId("consultor-form")?.addEventListener("submit", simularCompra);
    syncConsultorMonth();
}

function syncConsultorMonth() {
    const field = byId("consultor-mes-inicio");
    if (field && !field.value) field.value = state.month;
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
        state.annualSummary = response;
        state.annualMonths = response.meses || [];
        renderAnnual();
    } catch (error) {
        state.annualSummary = null;
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
        performanceContraMeta: 0, gastoDiarioEsperadoAtual: 0, gastoDiarioRestante: 0,
        parcelamentos: 0, totalCompromissosProjetados: 0, saldoProjetado: 0,
        taxaComprometimento: 0
    };
}

function emptyAnnualSummary() {
    return {
        ano: state.annualYear,
        totalEntradas: 0,
        saidaTotal: 0,
        totalInvestido: 0,
        saldoAcumulado: 0,
        percentualInvestidoAnual: 0,
        mediaMensalEntradasCalendario: 0,
        mediaMensalEntradasAtiva: 0,
        mediaMensalSaidasCalendario: 0,
        mediaMensalSaidasAtiva: 0,
        mediaMensalInvestidaCalendario: 0,
        mediaMensalInvestidaAtiva: 0,
        mesesComMovimentacao: 0,
        maiorEntrada: null,
        maiorInvestimento: null,
        maiorSaida: null,
        melhorMes: null,
        piorMes: null,
        totalParcelamentos: 0,
        totalCompromissosProjetados: 0,
        mediaComprometimento: 0,
        meses: []
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
    const summary = state.annualSummary || emptyAnnualSummary();
    const months = summary.meses?.length
        ? summary.meses
        : state.annualMonths.length
        ? state.annualMonths
        : Array.from({ length: 12 }, (_, i) => ({
            month: `${state.annualYear}-${String(i + 1).padStart(2, "0")}`,
            entriesCount: 0,
            ...emptySummary()
        }));

    setText("annual-income",          money(summary.totalEntradas));
    setText("annual-income-average",  `Média ativa: ${money(summary.mediaMensalEntradasAtiva)}`);
    setText("annual-out",             money(summary.saidaTotal));
    setText("annual-out-average",     `Média ativa: ${money(summary.mediaMensalSaidasAtiva)}`);
    setText("annual-invested",        money(summary.totalInvestido));
    setText("annual-invested-rate",   `${percent(summary.percentualInvestidoAnual)} das entradas · média ${money(summary.mediaMensalInvestidaAtiva)}`);
    setText("annual-balance",         money(summary.saldoAcumulado));
    setText("annual-active-months",   `${summary.mesesComMovimentacao} ${summary.mesesComMovimentacao === 1 ? "mês com movimentação" : "meses com movimentação"}`);
    setText("annual-installments",    money(summary.totalParcelamentos));
    setText("annual-commitment",      money(summary.totalCompromissosProjetados));
    setText("annual-commitment-rate", `Média comprometida: ${percent(summary.mediaComprometimento)}`);

    renderAnnualChart(months);
    renderAnnualHighlights(summary);
    renderAnnualMonths(months);
}

function renderAnnualHighlights(summary) {
    setAnnualHighlight("annual-best-income",      "annual-best-income-value",      summary.maiorEntrada,      "somaEntradas");
    setAnnualHighlight("annual-best-investment",  "annual-best-investment-value",  summary.maiorInvestimento, "totalInvestido");
    setAnnualHighlight("annual-highest-out",      "annual-highest-out-value",      summary.maiorSaida,        "saidaTotal");
    setAnnualHighlight("annual-best-month",       "annual-best-month-value",       summary.melhorMes,         "saldoMes");
    setAnnualHighlight("annual-worst-month",      "annual-worst-month-value",      summary.piorMes,           "saldoMes");
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
        const commitmentRate = number(m.taxaComprometimento);

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
                <span>Parcelas<strong>${money(m.parcelamentos)}</strong></span>
                <span>Comprometido<strong>${percent(commitmentRate)}</strong></span>
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
    updateDataExportPeriod();
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

    // Balance caption
    const balance = number(s.saldoMes);
    setText("balance-caption", balance < 0 ? "Seu mês está no negativo" : "Resultado até agora");

    // Temperature gauge
    const performance = normalizeRate(s.performanceContraMeta);
    renderTemperature(performance, number(s.somaEntradas) > 0);
    renderDailyBudget(s);
    renderRecentEntries();
    return;

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
function renderDailyBudget(summary) {
    const daily = calculateDailyBudget(summary);
    const spent = Math.max(0, daily.spent);
    const expected = Math.max(0, daily.expectedToDate);
    const ratio = expected > 0 ? spent / expected : 0;

    setText("metric-daily-remaining", money(daily.monthlyBudget));
    setText("daily-budget-caption", `${money(daily.remainingBudget)} ainda disponivel para gastos variaveis reais`);
    setText("daily-base-limit", money(daily.baseDailyLimit));
    setText("daily-adjusted-limit", money(daily.adjustedDailyLimit));
    setText("daily-spent", money(spent));
    setText("daily-expected", money(expected));
    setText("daily-days-left", String(daily.remainingDays));
    setText("daily-weekends-left", String(daily.remainingWeekends));
    setText("daily-weekend-limit", money(daily.weekendLimit));

    const barFill = byId("daily-progress");
    if (barFill) {
        barFill.style.width = `${Math.min(ratio * 100, 100)}%`;
        barFill.style.background = ratio > 1 ? "var(--red)" : ratio > 0.8 ? "var(--amber)" : "var(--green)";
    }

    setText("daily-progress-label", daily.monthlyBudget <= 0
        ? "Defina entradas e saidas fixas para calcular o orcamento variavel"
        : expected <= 0
            ? "Aguardando gastos reais registrados"
            : ratio <= 1
                ? `${percent(ratio)} do ritmo esperado utilizado`
                : `${percent(ratio - 1)} acima do ritmo esperado`
    );

    const healthPill = byId("daily-health-pill");
    if (healthPill) {
        healthPill.className = `status-pill ${daily.accelerated ? "danger" : ratio > 0.85 ? "warning" : "good"}`;
        healthPill.textContent = daily.accelerated ? "Rapido demais" : ratio > 0.85 ? "Atencao" : "No ritmo";
    }

    const deltaEl = byId("daily-delta");
    if (deltaEl) {
        if (daily.monthlyBudget > 0) {
            const delta = expected - spent;
            deltaEl.hidden = false;
            deltaEl.className = `daily-delta ${daily.accelerated ? "behind" : "ahead"}`;
            deltaEl.textContent = daily.accelerated
                ? `Orcamento sendo consumido rapido demais. Novo limite: ${money(daily.adjustedDailyLimit)} por dia e ${money(daily.weekendLimit)} por fim de semana para fechar o mes.`
                : delta >= 0
                    ? `${money(delta)} abaixo do ritmo planejado. Mantenha ate ${money(daily.adjustedDailyLimit)} por dia.`
                    : `${money(Math.abs(delta))} acima do planejado. Ajuste para ${money(daily.adjustedDailyLimit)} por dia.`;
        } else {
            deltaEl.hidden = true;
        }
    }
}

function calculateDailyBudget(summary) {
    const reference = referenceDateForMonth(state.month);
    const totalDays = daysInMonth(state.month);
    const dayOfMonth = reference.getDate();
    const remainingDays = Math.max(totalDays - dayOfMonth + 1, 1);
    const metaValue = number(summary.somaEntradas) * normalizeRate(summary.metaInvestimento);
    const monthlyBudget = Math.max(0, number(summary.somaEntradas) - number(summary.somaSaidasFixas) - metaValue);
    const spent = Math.max(0, number(summary.totalGastoDiario));
    const expectedToDate = Math.max(0, number(summary.gastoDiarioEsperadoAtual));
    const remainingBudget = Math.max(0, monthlyBudget - spent);
    const baseDailyLimit = totalDays > 0 ? monthlyBudget / totalDays : 0;
    const adjustedDailyLimit = remainingBudget / remainingDays;
    const weekendInfo = remainingWeekendInfo(reference, state.month);
    const averageWeekendDays = weekendInfo.groups > 0 ? weekendInfo.days / weekendInfo.groups : 0;
    const weekendLimit = weekendInfo.groups > 0
        ? Math.min(remainingBudget, adjustedDailyLimit * averageWeekendDays)
        : 0;
    const recentAverage = recentDailySpendAverage(reference, 3);
    const accelerated = monthlyBudget > 0 && (
        spent > expectedToDate * 1.05 ||
        recentAverage > baseDailyLimit * 1.1
    );

    return {
        monthlyBudget,
        spent,
        expectedToDate,
        remainingBudget,
        baseDailyLimit,
        adjustedDailyLimit,
        remainingDays,
        remainingWeekends: weekendInfo.groups,
        weekendLimit,
        accelerated
    };
}

function referenceDateForMonth(month) {
    const [year, value] = month.split("-").map(Number);
    if (month === currentYearMonth()) {
        const now = new Date();
        return new Date(now.getFullYear(), now.getMonth(), now.getDate());
    }
    return new Date(year, value, 0);
}

function daysInMonth(month) {
    const [year, value] = month.split("-").map(Number);
    return new Date(year, value, 0).getDate();
}

function remainingWeekendInfo(startDate, month) {
    const [year, value] = month.split("-").map(Number);
    const end = new Date(year, value, 0);
    let days = 0;
    let groups = 0;
    let inWeekend = false;

    for (let date = new Date(startDate); date <= end; date.setDate(date.getDate() + 1)) {
        const isWeekend = date.getDay() === 0 || date.getDay() === 6;
        if (isWeekend) {
            days += 1;
            if (!inWeekend) groups += 1;
        }
        inWeekend = isWeekend;
    }

    return { days, groups };
}

function recentDailySpendAverage(referenceDate, windowDays) {
    const start = new Date(referenceDate);
    start.setDate(start.getDate() - windowDays + 1);
    const total = state.entries
        .filter(entry => entry.tipo === "GASTO_DIARIO")
        .filter(entry => {
            const date = localDate(entry.data);
            return date >= start && date <= referenceDate;
        })
        .reduce((sum, entry) => sum + Math.max(0, number(entry.valor)), 0);
    return total / Math.max(windowDays, 1);
}

function localDate(value) {
    const [year, month, day] = String(value).split("-").map(Number);
    return new Date(year, month - 1, day);
}

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
    byId("entry-select-all")?.addEventListener("change", toggleAllVisibleEntries);
    byId("delete-selected-entries")?.addEventListener("click", requestSelectedEntriesDeletion);
    byId("clear-selected-entries")?.addEventListener("click", clearEntrySelection);

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
    const filtered = getVisibleEntriesForSelection()
        .sort((a, b) => String(b.data).localeCompare(String(a.data)) || b.id - a.id);

    const tbody = byId("entries-table-body");
    const empty = byId("entries-empty");
    if (!tbody || !empty) return;

    state.selectedEntryIds = new Set(
        [...state.selectedEntryIds].filter(id => state.entries.some(entry => entry.id === id))
    );

    tbody.innerHTML = filtered.map(entry => {
        const meta = typeMeta(entry.tipo);
        const isRecorrente = entry.recorrenciaId && !entry.recorrenciaExcecao;

        // 1. Cria o badge do ícone condicionalmente
        const badgeRecorrente = isRecorrente ? `
            <span class="badge-recorrente tooltip-trigger" 
                  aria-label="Lançamento gerado por uma recorrência" 
                  data-tooltip="Lançamento recorrente">
                <svg aria-hidden="true"><use href="#icon-refresh"></use></svg>
            </span>
        ` : "";

        const checked = state.selectedEntryIds.has(entry.id) ? "checked" : "";

        // 2. Retorna a linha completa, já com a nova estrutura da Descrição embutida
        return `<tr>
            <td class="select-column" data-label="Selecionar"><input class="entry-select-check" type="checkbox" data-select-entry="${entry.id}" ${checked} aria-label="Selecionar ${escapeHtml(entry.descricao)}"></td>
            <td data-label="Data">${formatDate(entry.data)}</td>
            
            <td class="table-description" data-label="Descrição">
                <div class="description-header">
                    <strong>${escapeHtml(entry.descricao)}</strong>
                    ${badgeRecorrente}
                </div>
                <small>${escapeHtml(entry.observacao || "")}</small>
            </td>
            
            <td data-label="Tipo"><span class="type-badge ${meta.className}">${meta.label}</span></td>
            <td data-label="Categoria">${escapeHtml(entry.categoria || "-")}</td>
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

    setText("entry-count", filtered.length === 1 ? "1 lancamento" : `${filtered.length} lancamentos`);
    const total = filtered.reduce((sum, e) => sum + number(e.valor) * typeMeta(e.tipo).sign, 0);
    setText("entry-net-total", `Saldo listado: ${money(total)}`);
    updateEntryBulkActions(filtered);

    tbody.querySelectorAll("[data-select-entry]").forEach(input =>
        input.addEventListener("change", event => {
            const id = Number(event.currentTarget.dataset.selectEntry);
            if (event.currentTarget.checked) {
                state.selectedEntryIds.add(id);
            } else {
                state.selectedEntryIds.delete(id);
            }
            updateEntryBulkActions(filtered);
        })
    );
    tbody.querySelectorAll("[data-edit-entry]").forEach(btn =>
        btn.addEventListener("click", () => openEntryDialog(Number(btn.dataset.editEntry)))
    );
    tbody.querySelectorAll("[data-delete-entry]").forEach(btn =>
        btn.addEventListener("click", () => requestEntryDeletion(Number(btn.dataset.deleteEntry)))
    );
}

function getVisibleEntriesForSelection() {
    const search = normalizeText(byId("entry-search")?.value ?? "");
    const type = byId("entry-type-filter")?.value ?? "";
    return [...state.entries]
        .filter(entry => !type || entry.tipo === type)
        .filter(entry => !state.showOnlyRecurring || (entry.recorrenciaId && !entry.recorrenciaExcecao))
        .filter(entry => !search || normalizeText(`${entry.descricao} ${entry.categoria || ""} ${entry.observacao || ""}`).includes(search));
}

function updateEntryBulkActions(visibleEntries = []) {
    const selected = state.selectedEntryIds.size;
    const bulkBar = byId("entry-bulk-actions");
    if (bulkBar) bulkBar.hidden = selected === 0;
    setText("entry-selected-count", selected === 1 ? "1 selecionado" : `${selected} selecionados`);

    const selectAll = byId("entry-select-all");
    if (!selectAll) return;

    const visibleIds = visibleEntries.map(entry => entry.id);
    const selectedVisible = visibleIds.filter(id => state.selectedEntryIds.has(id)).length;
    selectAll.checked = visibleIds.length > 0 && selectedVisible === visibleIds.length;
    selectAll.indeterminate = selectedVisible > 0 && selectedVisible < visibleIds.length;
}

function toggleAllVisibleEntries(event) {
    getVisibleEntriesForSelection().forEach(entry => {
        if (event.currentTarget.checked) {
            state.selectedEntryIds.add(entry.id);
        } else {
            state.selectedEntryIds.delete(entry.id);
        }
    });
    renderEntries();
}

function clearEntrySelection() {
    state.selectedEntryIds.clear();
    renderEntries();
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
            byId("entry-id").value = entry.id;
            if (escopoContainer) escopoContainer.hidden = !entry.recorrenciaId;
            form.elements.tipo.value        = entry.tipo;
            // Sync Alpine's `tipo` state via the fieldset @change listener
            form.querySelector(`input[name="tipo"][value="${entry.tipo}"]`)
                ?.dispatchEvent(new Event("change", { bubbles: true }));
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
    const tipo         = form.elements.tipo.value;
    const isParcelado  = isRecorrente
        && tipo === "GASTO_DIARIO"
        && form.elements.modoRecorrente?.value === "parcelado";

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
            const mesInicioRec = form.elements.mesInicio?.value || "";
            const mesFimRec = nullIfBlank(form.elements.mesFim?.value);
            const totalParcelasRec = isParcelado && mesInicioRec && mesFimRec ? monthsBetweenInclusive(mesInicioRec, mesFimRec) : null;
            const recorrenciaPayload = {
                tipo:            tipo,
                descricao:       byId("entry-description").value.trim(),
                valorCentavos:   Math.round(valorDecimal * 100),
                categoria:       isParcelado ? "PARCELAMENTO" : nullIfBlank(byId("entry-category").value),
                observacao:      nullIfBlank(byId("entry-notes").value),
                mesInicio:       mesInicioRec,
                mesFim:          mesFimRec,
                diaPreferencial: Number(form.elements.diaPreferencial?.value || 1),
                parcelaInicio:   isParcelado ? 1 : null,
                parcelaTotal:    totalParcelasRec,
                frequencia:      isParcelado ? "MENSAL" : (form.elements.frequencia?.value  || "MENSAL"),
                status:          isParcelado ? "ATIVO"  : (form.elements.statusRecorrencia?.value || "ATIVO"),
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
            tipo:          tipo,
            descricao:     byId("entry-description").value.trim(),
            valor:         valorDecimal,
            data:          byId("entry-date").value,
            mesReferencia: byId("entry-month").value,
            categoria:     isParcelado ? "PARCELAMENTO" : nullIfBlank(byId("entry-category").value),
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
        const msg = isParcelado
            ? "Parcelamento e lançamento criados."
            : isRecorrente
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

function requestSelectedEntriesDeletion() {
    const ids = [...state.selectedEntryIds];
    if (!ids.length) return;

    setText("confirm-title", "Excluir lancamentos selecionados?");
    setText("confirm-message", `${ids.length} lancamento(s) serao removidos permanentemente.`);
    setText("confirm-action", "Excluir selecionados");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.entries}/lote`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ ids })
        });
        state.selectedEntryIds.clear();
        toast("Lancamentos removidos.");
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
    byId("upload-button")?.addEventListener("click", uploadCsvPreview);
    byId("confirm-import-button")?.addEventListener("click", confirmarImportacao);
    byId("cancel-import-button")?.addEventListener("click", cancelarImportacao);
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

async function uploadCsvPreview(event) {
    event?.preventDefault();
    if (!state.selectedFile) return;
    const button = byId("upload-button");
    const original = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Gerando preview`;

    const data = new FormData();
    data.append("file", state.selectedFile);
    state.importAbortController = new AbortController();

    try {
        const result = await apiFetch(`${API.imports}/preview`, {
            method: "POST",
            body: data,
            signal: state.importAbortController.signal
        });
        state.importPreview = result;
        renderImportPreview();
        toast(`${result.linhas?.length || 0} linha(s) prontas para revisao.`);
        selectFile(null);
    } catch (error) {
        if (error.name === "AbortError") {
            toast("Importacao cancelada.");
            return;
        }
        toast(error.message || "Nao foi possivel importar o arquivo.", "error");
    } finally {
        state.importAbortController = null;
        button.disabled = !state.selectedFile;
        button.innerHTML = original;
    }
}

async function cancelarImportacao() {
    const preview = state.importPreview;
    if (state.importAbortController) {
        state.importAbortController.abort();
    }

    state.importPreview = null;
    renderImportPreview();
    selectFile(null);

    if (!preview?.loteId) return;

    try {
        await apiFetch(`${API.imports}/${encodeURIComponent(preview.loteId)}`, { method: "DELETE" });
        await loadImports();
        toast("Importacao cancelada.");
    } catch (error) {
        toast(error.message || "Nao foi possivel cancelar a importacao.", "error");
    }
}
function renderImportPreview() {
    const panel = byId("import-preview-panel");
    const rows = byId("import-preview-rows");
    if (!panel || !rows) return;

    const preview = state.importPreview;
    panel.hidden = !preview;
    if (!preview) {
        rows.innerHTML = "";
        return;
    }

    setText("import-preview-summary", "Revise categorias, desmarque duplicidades e confirme apenas as linhas corretas.");
    setText("import-preview-lote", preview.loteId);
    setText("import-preview-total", preview.linhas?.length || 0);
    setText("import-preview-duplicates", (preview.linhas || []).filter(linha => linha.isDuplicidade).length);
    rows.innerHTML = (preview.linhas || []).map(linha => {
        const parcela = linha.isParcelamento ? `${linha.parcelaAtual || "-"} / ${linha.parcelaTotal || "-"}` : "-";
        const ajusteFatura = isAjusteFatura(linha.categoriaSugerida);
        const checked = linha.isDuplicidade || ajusteFatura ? "" : "checked";
        const status = ajusteFatura
            ? `<span class="import-review-pill warning">Fatura</span>`
            : linha.isDuplicidade
            ? `<span class="import-review-pill warning">Revisar</span>`
            : `<span class="import-review-pill good">Novo</span>`;
        return `<tr class="${linha.isDuplicidade || ajusteFatura ? "is-duplicate" : ""}" data-preview-line="${linha.id}">
            <td data-label="Importar"><input class="import-review-check" type="checkbox" data-preview-importar="${linha.id}" ${checked}></td>
            <td class="import-review-date" data-label="Data">${escapeHtml(formatDate(linha.data))}</td>
            <td class="import-review-description" data-label="Descricao">
                <strong>${escapeHtml(linha.descricaoLimpa)}</strong>
                ${linha.descricaoOriginal && linha.descricaoOriginal !== linha.descricaoLimpa ? `<small>Original: ${escapeHtml(linha.descricaoOriginal)}</small>` : ""}
            </td>
            <td class="import-review-value" data-label="Valor">${money(Number(linha.valor || 0))}</td>
            <td data-label="Categoria">
                <select class="import-review-category" data-preview-categoria="${linha.id}">
                    ${renderCategoriaOptions(linha.categoriaSugerida)}
                </select>
            </td>
            <td data-label="Parcela"><span class="import-review-pill">${escapeHtml(parcela)}</span></td>
            <td data-label="Status">${status}</td>
        </tr>`;
    }).join("");

    rows.querySelectorAll("[data-preview-importar]").forEach(input =>
        input.addEventListener("change", updateImportPreviewSelection)
    );
    updateImportPreviewSelection();
}

function renderCategoriaOptions(selected) {
    const categorias = [
        "Cartao Nubank",
        "SAIDA_FIXA",
        "PARCELAMENTO",
        "PAGAMENTO_FATURA",
        "DESCONTO_ANTECIPACAO",
        "GASTO_DIARIO",
        "ENTRADA",
        "INVESTIMENTO"
    ];
    const atual = selected || "Cartao Nubank";
    return categorias.map(categoria =>
        `<option value="${escapeHtml(categoria)}" ${categoria === atual ? "selected" : ""}>${escapeHtml(categoria)}</option>`
    ).join("");
}

function isAjusteFatura(categoria) {
    return categoria === "PAGAMENTO_FATURA" || categoria === "DESCONTO_ANTECIPACAO";
}

async function confirmarImportacao() {
    const preview = state.importPreview;
    if (!preview) return;

    const button = byId("confirm-import-button");
    const original = button?.innerHTML || "";
    if (button) {
        button.disabled = true;
        button.innerHTML = `<span class="spinner"></span> Confirmando`;
    }

    const rows = byId("import-preview-rows");
    const linhas = (preview.linhas || []).map(linha => ({
        id: linha.id,
        categoria: rows?.querySelector(`[data-preview-categoria="${linha.id}"]`)?.value || linha.categoriaSugerida || "Cartao Nubank",
        importar: Boolean(rows?.querySelector(`[data-preview-importar="${linha.id}"]`)?.checked)
    }));

    try {
        await apiFetch(`${API.imports}/lotes/${encodeURIComponent(preview.loteId)}/confirmar`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ linhas })
        });
        toast("Importacao confirmada.");
        state.importPreview = null;
        renderImportPreview();
        await Promise.all([loadImports(), loadMonth()]);
    } catch (error) {
        toast(error.message || "Nao foi possivel confirmar a importacao.", "error");
    } finally {
        if (button) {
            button.disabled = false;
            button.innerHTML = original;
        }
    }
}

function updateImportPreviewSelection() {
    const rows = byId("import-preview-rows");
    if (!rows) return;

    const checks = Array.from(rows.querySelectorAll("[data-preview-importar]"));
    const selected = checks.filter(check => check.checked).length;
    const total = checks.length;

    checks.forEach(check => {
        const row = check.closest("tr");
        row?.classList.toggle("is-skipped", !check.checked);
    });

    setText("import-preview-selected", selected);
    setText("import-preview-action-summary", selected
        ? `${selected} de ${total} linha(s) serao importadas.`
        : "Nenhuma linha selecionada."
    );

    const button = byId("confirm-import-button");
    if (button) button.disabled = selected === 0;
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
/* Backup */
function bindBackups() {
    byId("backup-export-form")?.addEventListener("submit", exportBackup);
    byId("data-export-form")?.addEventListener("submit", exportLancamentosCsv);
    byId("data-export-scope")?.addEventListener("change", updateDataExportPeriod);
    byId("backup-restore-form")?.addEventListener("submit", requestBackupRestore);
    byId("backup-file")?.addEventListener("change", event => selectBackupFile(event.target.files[0]));
    byId("backup-refresh")?.addEventListener("click", loadBackupInfo);
    updateDataExportPeriod();
}

async function loadBackupInfo() {
    const container = byId("backup-info");
    if (container) container.innerHTML = `<div class="loading-panel"><span class="spinner"></span> Carregando...</div>`;

    try {
        state.backupInfo = await apiFetch(`${API.backups}/info`);
        renderBackupInfo();
    } catch (error) {
        if (container) {
            container.innerHTML = `<div class="empty-state"><p>${escapeHtml(error.message || "Nao foi possivel carregar os dados do banco.")}</p></div>`;
        }
    }
}

function renderBackupInfo() {
    const container = byId("backup-info");
    const info = state.backupInfo;
    if (!container || !info) return;

    container.innerHTML = `
        <div class="backup-info-row"><span>Banco</span><strong>${escapeHtml(info.caminhoBanco)}</strong></div>
        <div class="backup-info-row"><span>Status</span><strong>${info.bancoExiste ? "Encontrado" : "Nao encontrado"}</strong></div>
        <div class="backup-info-row"><span>Tamanho</span><strong>${formatFileSize(Number(info.tamanhoBytes || 0))}</strong></div>
        <div class="backup-info-row"><span>Atualizado</span><strong>${escapeHtml(formatDateTime(info.atualizadoEm))}</strong></div>
        <div class="backup-info-row"><span>Schema</span><strong>V${escapeHtml(info.schemaVersion)}</strong></div>
        <div class="backup-info-row"><span>Backups automaticos</span><strong>${escapeHtml(info.diretorioBackupsAutomaticos)}</strong></div>
    `;
}

async function exportBackup(event) {
    event.preventDefault();
    const senha = byId("backup-export-password")?.value || "";
    const button = byId("backup-export-button");
    if (senha.length < 8) {
        toast("Use uma senha com pelo menos 8 caracteres.", "error");
        return;
    }

    const original = button?.innerHTML || "";
    if (button) {
        button.disabled = true;
        button.innerHTML = `<span class="spinner"></span> Exportando`;
    }

    try {
        const response = await fetch(`${API.backups}/exportar`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ senha })
        });
        if (!response.ok) throw new Error(await readErrorMessage(response));
        const blob = await response.blob();
        const filename = filenameFromDisposition(response.headers.get("content-disposition")) || "contas-termometro.ctbackup";
        downloadBlob(blob, filename);
        byId("backup-export-password").value = "";
        toast("Backup exportado.");
        await loadBackupInfo();
    } catch (error) {
        toast(error.message || "Nao foi possivel exportar o backup.", "error");
    } finally {
        if (button) {
            button.disabled = false;
            button.innerHTML = original;
        }
    }
}

async function exportLancamentosCsv(event) {
    event.preventDefault();
    const escopo = byId("data-export-scope")?.value || "MES";
    const params = new URLSearchParams({ mes: state.month, escopo });
    window.location.assign(`${API.entries}/exportar?${params.toString()}`);
}

function updateDataExportPeriod() {
    const el = byId("data-export-period");
    if (!el) return;

    const monthsByScope = {
        MES: 1,
        TRES_MESES: 3,
        SEIS_MESES: 6,
        UM_ANO: 12,
    };
    const escopo = byId("data-export-scope")?.value || "MES";
    const quantidadeMeses = monthsByScope[escopo] || 1;
    const inicio = addMonths(state.month, -(quantidadeMeses - 1));
    el.textContent = `${formatYearMonth(inicio)} ate ${formatYearMonth(state.month)}`;
}

function selectBackupFile(file) {
    if (file && !file.name.toLowerCase().endsWith(".ctbackup")) {
        toast("Escolha um arquivo .ctbackup.", "error");
        byId("backup-file").value = "";
        state.selectedBackupFile = null;
        return;
    }
    state.selectedBackupFile = file || null;
    setText("backup-file-name", file ? `${file.name} (${formatFileSize(file.size)})` : "Nenhum arquivo selecionado");
}

function requestBackupRestore(event) {
    event.preventDefault();
    if (!state.selectedBackupFile) {
        toast("Selecione um arquivo .ctbackup.", "error");
        return;
    }
    const senha = byId("backup-restore-password")?.value || "";
    if (senha.length < 8) {
        toast("Informe a senha usada na exportacao.", "error");
        return;
    }

    setText("confirm-title", "Restaurar backup?");
    setText("confirm-message", "Os dados atuais serao substituidos. Um backup automatico sera criado antes da restauracao.");
    setText("confirm-action", "Restaurar");
    state.pendingConfirmation = () => restoreBackup(senha);
    byId("confirm-dialog")?.showModal();
}

async function restoreBackup(senha) {
    const button = byId("backup-restore-button");
    const original = button?.innerHTML || "";
    if (button) {
        button.disabled = true;
        button.innerHTML = `<span class="spinner"></span> Restaurando`;
    }

    const data = new FormData();
    data.append("file", state.selectedBackupFile);
    data.append("senha", senha);

    try {
        const result = await apiFetch(`${API.backups}/restaurar`, {
            method: "POST",
            body: data
        });
        byId("backup-restore-password").value = "";
        byId("backup-file").value = "";
        selectBackupFile(null);
        toast(result.mensagem || "Backup restaurado.");
        await Promise.all([loadBackupInfo(), loadMonth(), loadAnnual()]);
    } finally {
        if (button) {
            button.disabled = false;
            button.innerHTML = original;
        }
    }
}

async function readErrorMessage(response) {
    try {
        const body = await response.json();
        return body.detail || body.message || body.title || `Erro ${response.status}`;
    } catch {
        return (await response.text()) || `Erro ${response.status}`;
    }
}

function filenameFromDisposition(disposition) {
    const match = /filename="?([^"]+)"?/i.exec(disposition || "");
    return match ? match[1] : null;
}

function downloadBlob(blob, filename) {
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
}

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

/* ── Compromissos (Recorrências + Parcelamentos) ───────────── */

/* MVP6: projecoes futuras e consultor */

async function loadProjecoes() {
    const meses = Math.max(1, Math.min(24, Number(byId("projection-months")?.value || 6)));
    const inicio = addMonths(state.month, 1);

    setProjectionLoading(true);
    try {
        const data = await apiFetch(`${API.projecoes}/mensal?mesInicio=${inicio}&meses=${meses}`);
        state.projecoes = Array.isArray(data) ? data : [];
        renderProjecoes();
    } catch (error) {
        state.projecoes = [];
        renderProjecoes();
        toast(error.message || "Nao foi possivel carregar projecoes.", "error");
    } finally {
        setProjectionLoading(false);
    }
}

function setProjectionLoading(loading) {
    const loader = byId("projecoes-loading");
    if (loader) loader.hidden = !loading;
}

function renderProjecoes() {
    const list = byId("projecoes-list");
    const empty = byId("projecoes-empty");
    if (!list) return;

    if (!state.projecoes.length) {
        list.innerHTML = "";
        if (empty) empty.hidden = false;
        updateProjectionTotals([]);
        return;
    }

    if (empty) empty.hidden = true;
    updateProjectionTotals(state.projecoes);

    const maxTotal = Math.max(1, ...state.projecoes.map(p => number(p.totalCompromissos)));
    list.innerHTML = state.projecoes.map(p => {
        const comprometimento = p.entradasRecorrentes > 0 ? p.totalCompromissos / p.entradasRecorrentes : 0;
        const barWidth = Math.min(100, (number(p.totalCompromissos) / maxTotal) * 100);
        const riskClass = p.saldoProjectado < 0 ? "danger" : comprometimento >= 0.85 ? "warning" : "good";
        return `
        <article class="projection-card ${riskClass}">
            <header>
                <div>
                    <strong>${monthName(p.mes)}</strong>
                    <span>Futuro separado do realizado</span>
                </div>
                <b>${money(p.saldoProjectado / 100)}</b>
            </header>
            <div class="projection-bar"><span style="width:${barWidth}%"></span></div>
            <div class="projection-breakdown">
                <span>Entradas recorrentes<strong>${money(p.entradasRecorrentes / 100)}</strong></span>
                <span>Saidas fixas<strong>${money(p.saidasFixas / 100)}</strong></span>
                <span>Parcelamentos<strong>${money(p.parcelamentos / 100)}</strong></span>
                <span>Outros recorrentes<strong>${money(p.outrosGastosRecorrentes / 100)}</strong></span>
                <span>Comprometimento<strong>${percent(comprometimento)}</strong></span>
            </div>
        </article>`;
    }).join("");
}

function updateProjectionTotals(items) {
    const total = key => items.reduce((sum, item) => sum + number(item[key]), 0);
    const entradas = total("entradasRecorrentes");
    const compromissos = total("totalCompromissos");
    const saldo = total("saldoProjectado");
    setText("projection-total-commitment", money(compromissos / 100));
    setText("projection-average-commitment", money(items.length ? compromissos / items.length / 100 : 0));
    setText("projection-balance", money(saldo / 100));
    setText("projection-commitment-rate", entradas > 0 ? percent(compromissos / entradas) : "0%");
}

async function simularCompra(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;

    const button = byId("consultor-submit");
    const original = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Simulando`;

    const payload = {
        descricao: byId("consultor-descricao").value.trim(),
        valorTotalCentavos: Math.round(parseMoney(byId("consultor-valor").value) * 100),
        numeroParcelas: Number(byId("consultor-parcelas").value),
        mesInicio: byId("consultor-mes-inicio").value
    };

    try {
        state.consultorResultado = await apiFetch(`${API.consultor}/simular-compra`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        renderConsultorResultado();
    } catch (error) {
        state.consultorResultado = null;
        renderConsultorResultado();
        toast(error.message || "Nao foi possivel simular a compra.", "error");
    } finally {
        button.disabled = false;
        button.innerHTML = original;
    }
}

function renderConsultorResultado() {
    const panel = byId("consultor-result");
    const result = state.consultorResultado;
    if (!panel) return;
    if (!result) {
        panel.hidden = true;
        panel.innerHTML = "";
        return;
    }

    const cls = {
        VIAVEL: "good",
        ARRISCADO: "warning",
        NAO_RECOMENDADO: "danger"
    }[result.recomendacao] || "warning";

    const criticos = result.mesesCriticos || [];
    panel.hidden = false;
    panel.className = `panel consultor-result ${cls}`;
    panel.innerHTML = `
        <div class="panel-heading">
            <div>
                <p class="panel-kicker">Resultado</p>
                <h2>${formatRecommendation(result.recomendacao)}</h2>
            </div>
            <span class="status-pill ${cls}">${criticos.length} mes(es) critico(s)</span>
        </div>
        <p>${escapeHtml(result.motivacao)}</p>
        <div class="consultor-metrics">
            <span>Parcela<strong>${money(result.valorParcelaCentavos / 100)}</strong></span>
            <span>Media atual<strong>${money(result.comprometimentoMedioAtualCentavos / 100)}</strong></span>
            <span>Media com compra<strong>${money(result.comprometimentoMedioComCompraCentavos / 100)}</strong></span>
        </div>
        ${criticos.length ? `<div class="critical-months">${criticos.map(m => `
            <div>
                <strong>${monthName(m.mes)}</strong>
                <span>Compromisso: ${money(m.comprometimentoComCompraCentavos / 100)} - saldo: ${money(m.saldoComCompraCentavos / 100)}</span>
            </div>
        `).join("")}</div>` : ""}`;
}

function formatRecommendation(value) {
    return {
        VIAVEL: "Compra viavel",
        ARRISCADO: "Compra arriscada",
        NAO_RECOMENDADO: "Nao recomendado"
    }[value] || value;
}

async function loadCompromissos() {
    ["recorrencias", "parcelamentos"].forEach(id => {
        byId(`${id}-loading`).hidden  = false;
        byId(`${id}-list`).innerHTML  = "";
        const empty = byId(`${id}-empty`);
        if (empty) empty.hidden = true;
    });

    try {
        const [recorrencias, parcelamentos] = await Promise.all([
            apiFetch(`${API.recorrencias}/mensal?mes=${state.month}`),
            apiFetch(`${API.recorrencias}/parcelamentos?categoria=PARCELAMENTO`)
        ]);
        state.recorrencias  = recorrencias  || [];
        state.parcelamentos = parcelamentos || [];
        renderRecorrencias(state.recorrencias);
        renderParcelamentos(state.parcelamentos);
    } catch (error) {
        toast(error.message || "Erro ao carregar compromissos.", "error");
        byId("recorrencias-loading").hidden  = true;
        byId("parcelamentos-loading").hidden = true;
    }
}

function renderRecorrencias(list) {
    byId("recorrencias-loading").hidden = true;
    const container = byId("recorrencias-list");
    const empty     = byId("recorrencias-empty");
    if (!container) return;

    if (list.length === 0) {
        if (empty) empty.hidden = false;
        return;
    }
    if (empty) empty.hidden = true;

    const typeOrder  = ["ENTRADA", "SAIDA_FIXA", "GASTO_DIARIO"];
    const typeLabels = { ENTRADA: "Entradas recorrentes", SAIDA_FIXA: "Saídas fixas", GASTO_DIARIO: "Gastos recorrentes" };
    const freqLabel  = { MENSAL: "mensal", BIMESTRAL: "bimestral", TRIMESTRAL: "trimestral", SEMESTRAL: "semestral", ANUAL: "anual" };

    const groups = {};
    list.forEach(r => { (groups[r.tipo] = groups[r.tipo] || []).push(r); });

    container.innerHTML = typeOrder
        .filter(t => groups[t])
        .map(tipo => {
            const meta = typeMeta(tipo);
            return `
            <div class="comp-group">
                <h3 class="comp-group-title">
                    <span class="comp-group-dot ${meta.className}"></span>
                    ${typeLabels[tipo] || tipo}
                    <span class="comp-group-count">${groups[tipo].length}</span>
                </h3>
                ${groups[tipo].map(r => {
                const inicio = formatYearMonth(r.mesInicio);
                const fim    = r.mesFim ? formatYearMonth(r.mesFim) : "sem data de fim";
                const valor  = money(r.valorCentavos / 100);
                return `
                    <div class="comp-card">
                        <div class="comp-card-body">
                            <div class="comp-card-icon ${meta.className}">
                                <svg aria-hidden="true"><use href="#icon-refresh"></use></svg>
                            </div>
                            <div class="comp-card-info">
                                <strong>${escapeHtml(r.descricao)}</strong>
                                <span class="comp-card-sub">${valor} · ${freqLabel[r.frequencia] || r.frequencia}</span>
                                <span class="comp-card-period">De ${inicio} até ${fim}</span>
                            </div>
                        </div>
                        <div class="comp-card-actions">
                            <span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span>
                            <div class="comp-actions-right">
                                <button class="secondary-button sm" type="button" data-edit-recorrencia="${r.id}">Editar</button>
                                <button class="danger-button sm" type="button" data-cancel-recorrencia="${r.id}">Cancelar</button>
                            </div>
                        </div>
                    </div>`;
            }).join("")}
            </div>`;
        }).join("");

    bindCompromissosEdit(container);
}

function renderParcelamentos(list) {
    byId("parcelamentos-loading").hidden = true;
    const container = byId("parcelamentos-list");
    const empty     = byId("parcelamentos-empty");
    if (!container) return;

    if (list.length === 0) {
        if (empty) empty.hidden = false;
        return;
    }
    if (empty) empty.hidden = true;

    const active   = list.filter(r => r.status === "ATIVO");
    const inactive = list.filter(r => r.status !== "ATIVO");

    container.innerHTML = [
        active.length   ? renderParcelamentoGroup("Em andamento", active)   : "",
        inactive.length ? renderParcelamentoGroup("Encerrados / pausados", inactive) : "",
    ].join("");

    bindCompromissosEdit(container);
}

function renderParcelamentoGroup(title, list) {
    return `
        <div class="comp-group">
            <h3 class="comp-group-title">
                <span class="comp-group-dot daily"></span>
                ${title}
                <span class="comp-group-count">${list.length}</span>
            </h3>
            ${list.map(r => renderParcelamentoCard(r)).join("")}
        </div>`;
}

function renderParcelamentoCard(r) {
    const valor = money(r.valorCentavos / 100);
    const info  = parcelaInfo(r);

    if (!info) {
        return `
        <div class="comp-card parc-card">
            <div class="comp-card-body">
                <div class="comp-card-icon daily">
                    <svg aria-hidden="true"><use href="#icon-layers"></use></svg>
                </div>
                <div class="comp-card-info">
                    <strong>${escapeHtml(r.descricao)}</strong>
                    <span class="comp-card-sub">${valor} / mês — sem data de fim</span>
                </div>
            </div>
            <div class="comp-card-actions">
                <span class="status-badge status-${r.status.toLowerCase()}">${r.status}</span>
                <div class="comp-actions-right">
                    <button class="secondary-button sm" type="button" data-edit-recorrencia="${r.id}">Editar</button>
                    <button class="danger-button sm" type="button" data-cancel-recorrencia="${r.id}">Cancelar</button>
                </div>
            </div>
        </div>`;
    }

    const pctCapped  = Math.min(info.pct, 100);
    const pctClass   = pctCapped >= 100 ? "pct-good" : pctCapped >= 50 ? "pct-warn" : "pct-start";
    const saldoFmt   = money(info.saldoRestante / 100);
    const fimFmt     = formatYearMonth(r.mesFim);
    const doneLabel  = info.remaining <= 0
        ? `<span class="parc-done">✓ Quitado</span>`
        : `<span class="parc-remain">${info.remaining} ${info.remaining === 1 ? "parcela restante" : "parcelas restantes"}</span>`;

    return `
    <div class="comp-card parc-card">
        <div class="comp-card-body">
            <div class="comp-card-icon daily">
                <svg aria-hidden="true"><use href="#icon-layers"></use></svg>
            </div>
            <div class="comp-card-info">
                <strong>${escapeHtml(r.descricao)}</strong>
                <span class="comp-card-sub">${valor} / mês · Parcela ${info.elapsed} de ${info.total}</span>
            </div>
            <div class="comp-actions-right">
                <button class="secondary-button sm" type="button" data-edit-recorrencia="${r.id}">Editar</button>
                <button class="secondary-button sm" type="button" data-antecipar-parcelamento="${r.id}">Antecipar</button>
            </div>
        </div>
        <div class="parc-progress-wrap">
            <div class="parc-progress-bar">
                <div class="parc-progress-fill ${pctClass}" style="width: ${pctCapped}%"></div>
            </div>
            <span class="parc-pct ${pctClass}">${info.pct}%</span>
        </div>
        <div class="comp-card-footer">
            <small>Restam <strong>${saldoFmt}</strong> · até ${fimFmt}</small>
            ${doneLabel}
        </div>
    </div>`;
}

/* parcela math — compara meses em formato YYYY-MM lexicograficamente (válido para ISO) */
function parcelaInfo(rec) {
    if (!rec.mesFim) return null;
    const toN   = ym => { const [y, m] = ym.split("-").map(Number); return y * 12 + m - 1; };
    const startN = toN(rec.mesInicio);
    const endN   = toN(rec.mesFim);
    const nowN   = toN(state.month);
    const spanTotal = Math.max(endN - startN + 1, 1);
    const total = Number(rec.parcelaTotal) || spanTotal;
    const startInstallment = Number(rec.parcelaInicio) || 1;
    const monthOffset = Math.min(Math.max(nowN - startN, 0), spanTotal - 1);
    const current = Math.min(Math.max(startInstallment + monthOffset, 1), total);
    const remaining = rec.status === "ATIVO" ? Math.max(total - current + 1, 0) : 0;
    const paid = Math.max(total - remaining, 0);
    const pct = total > 0 ? Math.round((paid / total) * 100) : 0;
    return { total, elapsed: current, current, remaining, pct, saldoRestante: remaining * rec.valorCentavos };
}

function formatYearMonth(ym) {
    if (!ym) return "—";
    const [y, m] = ym.split("-").map(Number);
    return new Date(y, m - 1, 1).toLocaleDateString("pt-BR", { month: "short", year: "numeric" });
}

/* Tab switching */
function bindCompromissosEdit(container) {
    container.querySelectorAll("[data-edit-recorrencia]").forEach(btn =>
        btn.addEventListener("click", () => openRecorrenciaEditDialog(Number(btn.dataset.editRecorrencia)))
    );
    container.querySelectorAll("[data-cancel-recorrencia]").forEach(btn =>
        btn.addEventListener("click", () => requestCancelarRecorrencia(Number(btn.dataset.cancelRecorrencia)))
    );
    container.querySelectorAll("[data-antecipar-parcelamento]").forEach(btn =>
        btn.addEventListener("click", () => requestAnteciparParcelamento(Number(btn.dataset.anteciparParcelamento)))
    );
}

function requestCancelarRecorrencia(id) {
    const rec = [...state.recorrencias, ...state.parcelamentos].find(item => item.id === id);
    if (!rec) return;

    setText("confirm-title", "Cancelar compromisso?");
    setText("confirm-message", `A recorrencia "${rec.descricao}" deixara de projetar valores a partir de ${formatYearMonth(state.month)}.`);
    setText("confirm-action", "Cancelar compromisso");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.recorrencias}/${id}/cancelar`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ aPartirDe: state.month })
        });
        toast("Compromisso cancelado.");
        await Promise.all([loadCompromissos(), loadProjecoes()]);
    };
    byId("confirm-dialog")?.showModal();
}

async function requestAnteciparParcelamento(id) {
    const rec = state.parcelamentos.find(item => item.id === id);
    if (!rec) return;

    const info = parcelaInfo(rec);
    const mesQuitacao = state.month;
    const saldo = info ? money(info.saldoRestante / 100) : money(0);

    setText("confirm-title", "Antecipar parcelamento?");
    setText("confirm-message", `O parcelamento "${rec.descricao}" sera encerrado em ${formatYearMonth(mesQuitacao)} e as projecoes futuras restantes (${saldo}) serao removidas.`);
    setText("confirm-action", "Antecipar parcelamento");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.recorrencias}/${id}/antecipar`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ mesQuitacao })
        });
        toast("Parcelamento antecipado.");
        await Promise.all([loadCompromissos(), loadProjecoes()]);
    };
    byId("confirm-dialog")?.showModal();
}

function switchCompromissosTab(tab) {
    document.querySelectorAll(".comp-tab").forEach(b => b.classList.toggle("is-active", b.dataset.tab === tab));
    document.querySelectorAll(".comp-tab-panel").forEach(p => p.classList.toggle("is-active", p.dataset.panel === tab));
}

/* ── Recorrência Edit Dialog ───────────────────────────────── */
function openRecorrenciaEditDialog(id) {
    const all = [...state.recorrencias, ...state.parcelamentos];
    const rec = all.find(r => r.id === id);
    if (!rec) { toast("Recorrência não encontrada.", "error"); return; }

    const form = byId("recorrencia-edit-form");
    if (!form) return;

    byId("rec-edit-id").value          = rec.id;
    byId("rec-edit-descricao").value   = rec.descricao;
    byId("rec-edit-valor").value       = (rec.valorCentavos / 100).toFixed(2).replace(".", ",");
    byId("rec-edit-mes-inicio").value  = rec.mesInicio;
    byId("rec-edit-mes-fim").value     = rec.mesFim || "";
    byId("rec-edit-dia").value         = rec.diaPreferencial;
    byId("rec-edit-frequencia").value  = rec.frequencia;
    byId("rec-edit-status").value      = rec.status;

    setText("rec-edit-tipo-label", typeMeta(rec.tipo).label);

    byId("recorrencia-edit-dialog")?.showModal();
}

async function saveRecorrenciaEdit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;

    const id     = byId("rec-edit-id").value;
    const button = byId("rec-edit-save");
    const orig   = button.innerHTML;
    const original = state.recorrencias.concat(state.parcelamentos).find(r => r.id === Number(id));
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Salvando`;

    const payload = {
        tipo:            original?.tipo || "GASTO_DIARIO",
        descricao:       byId("rec-edit-descricao").value.trim(),
        valorCentavos:   Math.round(parseMoney(byId("rec-edit-valor").value) * 100),
        categoria:       original?.categoria || null,
        observacao:      original?.observacao || null,
        idLote:          original?.idLote || null,
        mesInicio:       byId("rec-edit-mes-inicio").value,
        mesFim:          nullIfBlank(byId("rec-edit-mes-fim").value),
        diaPreferencial: Number(byId("rec-edit-dia").value),
        parcelaInicio:   original?.parcelaInicio || null,
        parcelaTotal:    original?.parcelaTotal || null,
        frequencia:      byId("rec-edit-frequencia").value,
        status:          byId("rec-edit-status").value,
    };

    try {
        await apiFetch(`${API.recorrencias}/${id}`, {
            method:  "PUT",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(payload)
        });
        byId("recorrencia-edit-dialog").close();
        toast("Recorrência atualizada.");
        await loadCompromissos();
    } catch (error) {
        toast(error.message || "Não foi possível salvar.", "error");
    } finally {
        button.disabled  = false;
        button.innerHTML = orig;
    }
}

/* ── Helpers ───────────────────────────────────────────────── */
function normalizeRate(value) { return number(value); }
function typeMeta(type) { return TYPE_META[type] || TYPE_META.AJUSTE_SALDO; }
function entrySign(entry) { return typeMeta(entry.tipo).sign > 0 ? "+ " : "− "; }

function currentYearMonth() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}
function addMonths(month, offset) {
    const [year, value] = month.split("-").map(Number);
    const date = new Date(year, value - 1 + offset, 1);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}
function monthsBetweenInclusive(start, end) {
    const [startYear, startMonth] = start.split("-").map(Number);
    const [endYear, endMonth] = end.split("-").map(Number);
    return Math.max((endYear * 12 + endMonth) - (startYear * 12 + startMonth) + 1, 1);
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
function formatDateTime(value) {
    if (!value) return "-";
    return new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short"
    }).format(new Date(value));
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
