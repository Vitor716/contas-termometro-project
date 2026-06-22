const API = {
    entries: "/api/lancamentos",
    summary: "/api/orcamento",
    imports: "/api/importacao"
};

const VIEWS = new Set(["dashboard", "lancamentos", "anual", "importacao"]);

const TYPE_META = {
    ENTRADA: { label: "Entrada", className: "income", icon: "icon-arrow-down", sign: 1 },
    SAIDA_FIXA: { label: "Saída fixa", className: "fixed", icon: "icon-home", sign: -1 },
    GASTO_DIARIO: { label: "Gasto diário", className: "daily", icon: "icon-wallet", sign: -1 },
    INVESTIMENTO: { label: "Investimento", className: "investment", icon: "icon-leaf", sign: -1 },
    AJUSTE_SALDO: { label: "Ajuste", className: "adjustment", icon: "icon-sliders", sign: 1 }
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
    activeView: "dashboard"
};

const moneyFormatter = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const percentFormatter = new Intl.NumberFormat("pt-BR", { style: "percent", minimumFractionDigits: 0, maximumFractionDigits: 1 });
const monthFormatter = new Intl.DateTimeFormat("pt-BR", { month: "long", year: "numeric" });
const dateFormatter = new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "short" });

document.addEventListener("alpine:init", () => {
    Alpine.data("appShell", () => ({
        view: "dashboard",
        sidebarOpen: false,
        init() {
            window.addEventListener("app:navigate", event => {
                this.view = event.detail.view;
                this.sidebarOpen = false;
                if (this.view === "importacao") loadImports();
                if (this.view === "anual") loadAnnual();
                window.scrollTo({ top: 0, behavior: "smooth" });
            });
        },
        navigate(view) {
            navigate(view);
        },
        goBack() {
            goBack();
        },
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
    updateMonthLabels();
    loadMonth();
    checkHealth();
}

function bindAnnual() {
    byId("year-picker").value = state.annualYear;
    byId("annual-year-label").textContent = state.annualYear;
    byId("year-picker").addEventListener("change", event => {
        const year = Number(event.target.value);
        if (!Number.isInteger(year) || year < 2000 || year > 2100) return;
        state.annualYear = year;
        byId("annual-year-label").textContent = year;
        loadAnnual();
    });
    byId("previous-year").addEventListener("click", () => changeAnnualYear(-1));
    byId("next-year").addEventListener("click", () => changeAnnualYear(1));
    byId("refresh-annual").addEventListener("click", loadAnnual);
}

function changeAnnualYear(offset) {
    state.annualYear += offset;
    byId("year-picker").value = state.annualYear;
    byId("annual-year-label").textContent = state.annualYear;
    loadAnnual();
}

async function loadAnnual() {
    setAnnualLoading(true);
    try {
        state.annualMonths = await Promise.all(
            Array.from({ length: 12 }, (_, index) => loadAnnualMonth(state.annualYear, index + 1))
        );
        renderAnnual();
    } catch (error) {
        state.annualMonths = [];
        renderAnnual();
        toast(error.message || "Não foi possível consolidar o ano.", "error");
    } finally {
        setAnnualLoading(false);
    }
}

async function loadAnnualMonth(year, monthNumber) {
    const month = `${year}-${String(monthNumber).padStart(2, "0")}`;
    const entries = await apiFetch(`${API.entries}?mes=${month}`);
    if (!Array.isArray(entries) || entries.length === 0) {
        return { month, entriesCount: 0, ...emptySummary() };
    }
    const lastDay = new Date(year, monthNumber, 0).getDate();
    let summary;
    try {
        summary = await apiFetch(`${API.summary}?mes=${month}-${String(lastDay).padStart(2, "0")}`);
    } catch {
        summary = summarizeEntriesForAnnual(entries);
    }
    return { month, entriesCount: entries.length, ...summary };
}

function summarizeEntriesForAnnual(entries) {
    const sumByType = type => entries
        .filter(entry => entry.tipo === type)
        .reduce((sum, entry) => sum + number(entry.valor), 0);
    const income = sumByType("ENTRADA");
    const fixed = sumByType("SAIDA_FIXA");
    const daily = sumByType("GASTO_DIARIO");
    const invested = sumByType("INVESTIMENTO");
    const out = fixed + daily;
    return {
        ...emptySummary(),
        somaEntradas: income,
        somaSaidasFixas: fixed,
        totalGastoDiario: daily,
        totalInvestido: invested,
        saidaTotal: out,
        saldoMes: income - out,
        porcentagemInvestida: income > 0 ? invested / income * 100 : 0
    };
}

function emptySummary() {
    return {
        somaEntradas: 0,
        somaSaidasFixas: 0,
        totalGastoDiario: 0,
        totalInvestido: 0,
        saidaTotal: 0,
        saldoMes: 0,
        porcentagemInvestida: 0,
        metaInvestimento: 0,
        performanceContraMeta: 0,
        gastoDiarioEsperadoAtual: 0,
        gastoDiarioRestante: 0
    };
}

function setAnnualLoading(loading) {
    byId("annual-loading").hidden = !loading;
    byId("annual-content").hidden = loading;
}

function renderAnnual() {
    const months = state.annualMonths.length ? state.annualMonths : Array.from({ length: 12 }, (_, index) => ({
        month: `${state.annualYear}-${String(index + 1).padStart(2, "0")}`,
        entriesCount: 0,
        ...emptySummary()
    }));
    const total = key => months.reduce((sum, item) => sum + number(item[key]), 0);
    const income = total("somaEntradas");
    const out = total("saidaTotal");
    const invested = total("totalInvestido");
    const balance = total("saldoMes");
    const activeMonths = months.filter(item => item.entriesCount > 0).length;
    const divisor = activeMonths || 12;

    setText("annual-income", money(income));
    setText("annual-income-average", `Média mensal: ${money(income / divisor)}`);
    setText("annual-out", money(out));
    setText("annual-out-average", `Média mensal: ${money(out / divisor)}`);
    setText("annual-invested", money(invested));
    setText("annual-invested-rate", `${percent(income > 0 ? invested / income : 0)} das entradas`);
    setText("annual-balance", money(balance));
    setText("annual-active-months", `${activeMonths} ${activeMonths === 1 ? "mês com movimentação" : "meses com movimentação"}`);

    renderAnnualChart(months);
    renderAnnualHighlights(months);
    renderAnnualMonths(months);
}

function renderAnnualChart(months) {
    const maxValue = Math.max(1, ...months.flatMap(item => [
        number(item.somaEntradas),
        number(item.saidaTotal),
        number(item.totalInvestido)
    ]));
    byId("annual-chart").innerHTML = months.map(item => {
        const date = monthToDate(item.month);
        const label = new Intl.DateTimeFormat("pt-BR", { month: "short" }).format(date).replace(".", "");
        const bars = [
            ["income", number(item.somaEntradas), "Entradas"],
            ["out", number(item.saidaTotal), "Saídas"],
            ["investment", number(item.totalInvestido), "Investido"]
        ].map(([className, value, title]) =>
            `<i class="chart-bar ${className}" style="height:${value > 0 ? Math.max(value / maxValue * 100, 2) : 0}%" title="${title}: ${money(value)}"></i>`
        ).join("");
        return `<div class="chart-month"><div class="chart-bars">${bars}</div><span>${label}</span></div>`;
    }).join("");
}

function renderAnnualHighlights(months) {
    const active = months.filter(item => item.entriesCount > 0);
    const bestIncome = maxMonth(active, "somaEntradas");
    const bestInvestment = maxMonth(active, "totalInvestido");
    const highestOut = maxMonth(active, "saidaTotal");
    setAnnualHighlight("annual-best-income", "annual-best-income-value", bestIncome, "somaEntradas");
    setAnnualHighlight("annual-best-investment", "annual-best-investment-value", bestInvestment, "totalInvestido");
    setAnnualHighlight("annual-highest-out", "annual-highest-out-value", highestOut, "saidaTotal");
}

function maxMonth(months, key) {
    return months.reduce((best, item) => !best || number(item[key]) > number(best[key]) ? item : best, null);
}

function setAnnualHighlight(labelId, valueId, item, key) {
    setText(labelId, item ? monthName(item.month) : "—");
    setText(valueId, money(item ? item[key] : 0));
}

function renderAnnualMonths(months) {
    const container = byId("annual-months-grid");
    container.innerHTML = months.map(item => {
        const balance = number(item.saldoMes);
        return `<article class="annual-month-card ${item.entriesCount ? "" : "is-empty"}" role="button" tabindex="0" data-annual-month="${item.month}">
            <header><strong>${monthName(item.month)}</strong><span>${item.entriesCount} ${item.entriesCount === 1 ? "lançamento" : "lançamentos"}</span></header>
            <strong class="month-balance ${balance < 0 ? "negative" : ""}">${money(balance)}</strong>
            <div class="month-summary">
                <span>Entradas<strong>${money(item.somaEntradas)}</strong></span>
                <span>Saídas<strong>${money(item.saidaTotal)}</strong></span>
                <span>Investido<strong>${money(item.totalInvestido)}</strong></span>
                <span>Gasto diário<strong>${money(item.totalGastoDiario)}</strong></span>
            </div>
        </article>`;
    }).join("");
    container.querySelectorAll("[data-annual-month]").forEach(card => {
        const open = () => openAnnualMonth(card.dataset.annualMonth);
        card.addEventListener("click", open);
        card.addEventListener("keydown", event => {
            if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                open();
            }
        });
    });
}

function openAnnualMonth(month) {
    state.month = month;
    byId("month-picker").value = month;
    byId("entry-type-filter").value = "";
    updateEntryFilterContext("", "");
    updateMonthLabels();
    loadMonth();
    navigate("lancamentos");
}

function bindNavigation() {
    window.addEventListener("app:filter-entries", event => {
        const { type = "", label = "" } = event.detail || {};
        byId("entry-type-filter").value = type;
        updateEntryFilterContext(type, label);
        renderEntries();
    });
    byId("clear-entry-context").addEventListener("click", () => {
        byId("entry-type-filter").value = "";
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
    if (history.state?.previousView) {
        history.back();
        return;
    }
    navigate("dashboard");
}

function viewFromHash() {
    const value = location.hash.replace(/^#/, "");
    return VIEWS.has(value) ? value : "dashboard";
}

function bindMonthControls() {
    byId("month-picker").addEventListener("change", event => {
        if (!event.target.value) return;
        state.month = event.target.value;
        updateMonthLabels();
        loadMonth();
    });
    byId("previous-month").addEventListener("click", () => changeMonth(-1));
    byId("next-month").addEventListener("click", () => changeMonth(1));
    byId("refresh-dashboard").addEventListener("click", loadMonth);
}

function changeMonth(offset) {
    const [year, month] = state.month.split("-").map(Number);
    const date = new Date(year, month - 1 + offset, 1);
    state.month = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    byId("month-picker").value = state.month;
    updateMonthLabels();
    loadMonth();
}

function updateMonthLabels() {
    const date = monthToDate(state.month);
    byId("dashboard-month-name").textContent = monthFormatter.format(date);
}

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

function setDashboardLoading(loading) {
    byId("dashboard-loading").hidden = !loading;
    byId("dashboard-content").hidden = loading;
}

function renderDashboard() {
    const summary = state.summary || {};
    setText("metric-income", money(summary.somaEntradas));
    setText("metric-fixed", money(summary.somaSaidasFixas));
    setText("metric-daily", money(summary.totalGastoDiario));
    setText("metric-balance", money(summary.saldoMes));
    setText("metric-invested", money(summary.totalInvestido));
    setText("metric-total-out", money(summary.saidaTotal));
    setText("metric-performance", percent(normalizeRate(summary.performanceContraMeta)));
    setText("metric-daily-remaining", money(summary.gastoDiarioRestante));
    setText("daily-spent", money(summary.totalGastoDiario));
    setText("daily-expected", money(summary.gastoDiarioEsperadoAtual));

    const balance = number(summary.saldoMes);
    byId("balance-caption").textContent = balance < 0 ? "Seu mês está no negativo" : "Resultado até agora";

    const performance = normalizeRate(summary.performanceContraMeta);
    renderTemperature(performance, number(summary.somaEntradas) > 0);

    const spent = Math.max(0, number(summary.totalGastoDiario));
    const expected = Math.max(0, number(summary.gastoDiarioEsperadoAtual));
    const dailyRatio = expected > 0 ? spent / expected : 0;
    byId("daily-progress").style.width = `${Math.min(dailyRatio * 100, 100)}%`;
    byId("daily-progress").style.background = dailyRatio > 1 ? "var(--red)" : dailyRatio > .8 ? "var(--amber)" : "var(--green)";
    byId("daily-progress-label").textContent = expected <= 0
        ? "Aguardando dados do orçamento"
        : dailyRatio <= 1
            ? `${percent(dailyRatio)} do esperado utilizado`
            : `${percent(dailyRatio - 1)} acima do ritmo esperado`;

    renderRecentEntries();
}

function renderTemperature(performance, hasIncome) {
    const status = byId("temperature-status");
    status.className = "status-pill";

    if (!hasIncome) {
        setText("temperature-title", "Comece pelas entradas");
        setText("temperature-description", "Registre uma entrada para o termômetro comparar seus investimentos com a meta mensal.");
        setText("temperature-status", "Sem dados");
        byId("thermometer-fill").style.width = "100%";
        return;
    }

    const covered = Math.min(Math.max(performance, 0), 1.25);
    byId("thermometer-fill").style.width = `${100 - Math.min(covered / 1.25 * 100, 100)}%`;

    if (performance >= 1) {
        status.classList.add("good");
        setText("temperature-status", "Meta atingida");
        setText("temperature-title", "Seu mês está saudável");
        setText("temperature-description", "O investimento planejado foi alcançado. Preserve esse resultado ao tomar novas decisões.");
    } else if (performance >= .8) {
        status.classList.add("warning");
        setText("temperature-status", "Quase lá");
        setText("temperature-title", "Você está perto da meta");
        setText("temperature-description", "O ritmo é positivo. Revise os gastos variáveis antes de assumir um novo compromisso.");
    } else {
        status.classList.add("danger");
        setText("temperature-status", "Atenção");
        setText("temperature-title", "Sua meta pede atenção");
        setText("temperature-description", "O valor investido ainda está abaixo do planejado. Priorize a margem restante do mês.");
    }
}

function renderRecentEntries() {
    const container = byId("recent-list");
    const recent = [...state.entries].sort((a, b) => String(b.data).localeCompare(String(a.data)) || b.id - a.id).slice(0, 4);
    if (!recent.length) {
        container.innerHTML = `<div class="empty-state"><p>Nenhuma movimentação registrada neste mês.</p></div>`;
        return;
    }
    container.innerHTML = recent.map(entry => {
        const meta = typeMeta(entry.tipo);
        return `<div class="transaction-row">
            <span class="transaction-symbol ${meta.className}">${icon(meta.icon)}</span>
            <div class="transaction-copy"><strong>${escapeHtml(entry.descricao)}</strong><small>${formatDate(entry.data)} · ${escapeHtml(entry.categoria || meta.label)}</small></div>
            <strong class="transaction-amount ${meta.className}">${entrySign(entry)}${money(entry.valor)}</strong>
        </div>`;
    }).join("");
}

function bindEntryFilters() {
    byId("entry-search").addEventListener("input", renderEntries);
    byId("entry-type-filter").addEventListener("change", event => {
        const type = event.target.value;
        updateEntryFilterContext(type, type ? typeMeta(type).label : "");
        renderEntries();
    });
}

function updateEntryFilterContext(type, label) {
    const context = byId("entry-filter-context");
    context.hidden = !type;
    setText("entry-filter-label", type ? `Exibindo somente ${String(label).toLowerCase()} deste mês.` : "");
    setText("entries-title", type ? label : "Lançamentos do mês");
}

function renderEntries() {
    const search = normalizeText(byId("entry-search").value);
    const type = byId("entry-type-filter").value;
    const filtered = [...state.entries]
        .filter(entry => !type || entry.tipo === type)
        .filter(entry => !search || normalizeText(`${entry.descricao} ${entry.categoria || ""} ${entry.observacao || ""}`).includes(search))
        .sort((a, b) => String(b.data).localeCompare(String(a.data)) || b.id - a.id);

    const tbody = byId("entries-table-body");
    const empty = byId("entries-empty");
    tbody.innerHTML = filtered.map(entry => {
        const meta = typeMeta(entry.tipo);
        return `<tr>
            <td>${formatDate(entry.data)}</td>
            <td class="table-description"><strong>${escapeHtml(entry.descricao)}</strong><small>${escapeHtml(entry.observacao || "")}</small></td>
            <td><span class="type-badge ${meta.className}">${meta.label}</span></td>
            <td>${escapeHtml(entry.categoria || "—")}</td>
            <td class="amount-column"><strong class="${meta.className}">${entrySign(entry)}${money(entry.valor)}</strong></td>
            <td><div class="table-actions">
                <button class="icon-button" type="button" data-edit-entry="${entry.id}" aria-label="Editar ${escapeHtml(entry.descricao)}">${icon("icon-edit")}</button>
                <button class="icon-button delete" type="button" data-delete-entry="${entry.id}" aria-label="Excluir ${escapeHtml(entry.descricao)}">${icon("icon-trash")}</button>
            </div></td>
        </tr>`;
    }).join("");

    empty.hidden = filtered.length > 0;
    document.querySelector(".table-wrap").hidden = filtered.length === 0;
    const label = filtered.length === 1 ? "1 lançamento" : `${filtered.length} lançamentos`;
    setText("entry-count", label);
    const total = filtered.reduce((sum, entry) => sum + number(entry.valor) * typeMeta(entry.tipo).sign, 0);
    setText("entry-net-total", `Saldo listado: ${money(total)}`);

    tbody.querySelectorAll("[data-edit-entry]").forEach(button => button.addEventListener("click", () => openEntryDialog(Number(button.dataset.editEntry))));
    tbody.querySelectorAll("[data-delete-entry]").forEach(button => button.addEventListener("click", () => requestEntryDeletion(Number(button.dataset.deleteEntry))));
}

function bindEntryForm() {
    ["new-entry-top", "new-entry-page", "new-entry-empty"].forEach(id => byId(id).addEventListener("click", () => openEntryDialog()));
    byId("close-entry-dialog").addEventListener("click", closeEntryDialog);
    byId("cancel-entry").addEventListener("click", closeEntryDialog);
    byId("entry-form").addEventListener("submit", saveEntry);
}

function openEntryDialog(id = null) {
    const form = byId("entry-form");
    form.reset();
    byId("entry-id").value = "";
    byId("entry-month").value = state.month;
    byId("entry-date").value = defaultDateForMonth(state.month);
    setText("entry-dialog-title", "Novo lançamento");
    setText("save-entry", "");
    byId("save-entry").innerHTML = "<span>Salvar lançamento</span>";

    if (id !== null) {
        const entry = state.entries.find(item => item.id === id);
        if (!entry) return;
        byId("entry-id").value = entry.id;
        form.elements.tipo.value = entry.tipo;
        byId("entry-description").value = entry.descricao;
        byId("entry-value").value = String(entry.valor).replace(".", ",");
        byId("entry-date").value = entry.data;
        byId("entry-category").value = entry.categoria || "";
        byId("entry-month").value = entry.mesReferencia;
        byId("entry-notes").value = entry.observacao || "";
        setText("entry-dialog-title", "Editar lançamento");
        byId("save-entry").innerHTML = "<span>Salvar alterações</span>";
    }
    byId("entry-dialog").showModal();
    setTimeout(() => byId("entry-description").focus(), 50);
}

function closeEntryDialog() {
    byId("entry-dialog").close();
}

async function saveEntry(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const id = byId("entry-id").value;
    const button = byId("save-entry");
    const original = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Salvando`;

    const payload = {
        tipo: form.elements.tipo.value,
        descricao: byId("entry-description").value.trim(),
        valor: parseMoney(byId("entry-value").value),
        data: byId("entry-date").value,
        mesReferencia: byId("entry-month").value,
        categoria: nullIfBlank(byId("entry-category").value),
        observacao: nullIfBlank(byId("entry-notes").value)
    };

    if (!(payload.valor > 0)) {
        toast("Informe um valor maior que zero.", "error");
        button.disabled = false;
        button.innerHTML = original;
        return;
    }

    try {
        await apiFetch(id ? `${API.entries}/${id}` : API.entries, {
            method: id ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        closeEntryDialog();
        if (payload.mesReferencia !== state.month) {
            state.month = payload.mesReferencia;
            byId("month-picker").value = state.month;
            updateMonthLabels();
        }
        toast(id ? "Lançamento atualizado." : "Lançamento criado.");
        await loadMonth();
    } catch (error) {
        toast(error.message || "Não foi possível salvar o lançamento.", "error");
    } finally {
        button.disabled = false;
        button.innerHTML = original;
    }
}

function requestEntryDeletion(id) {
    const entry = state.entries.find(item => item.id === id);
    if (!entry) return;
    setText("confirm-title", "Excluir lançamento?");
    setText("confirm-message", `"${entry.descricao}" será removido permanentemente.`);
    setText("confirm-action", "Excluir");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.entries}/${id}`, { method: "DELETE" });
        toast("Lançamento excluído.");
        await loadMonth();
    };
    byId("confirm-dialog").showModal();
}

function bindConfirmation() {
    byId("confirm-dialog").addEventListener("close", async event => {
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

function bindImport() {
    const input = byId("csv-file");
    const zone = byId("upload-zone");
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
    byId("remove-file").addEventListener("click", () => selectFile(null));
    byId("upload-button").addEventListener("click", uploadCsv);
    byId("refresh-imports").addEventListener("click", loadImports);
}

function selectFile(file) {
    if (file && !file.name.toLowerCase().endsWith(".csv")) {
        toast("Escolha um arquivo no formato CSV.", "error");
        return;
    }
    state.selectedFile = file || null;
    byId("upload-zone").hidden = Boolean(file);
    byId("selected-file").hidden = !file;
    byId("upload-button").disabled = !file;
    if (file) {
        setText("selected-file-name", file.name);
        setText("selected-file-size", formatFileSize(file.size));
    } else {
        byId("csv-file").value = "";
    }
}

async function uploadCsv() {
    if (!state.selectedFile) return;
    const button = byId("upload-button");
    const original = button.innerHTML;
    button.disabled = true;
    button.innerHTML = `<span class="spinner"></span> Processando arquivo`;
    const data = new FormData();
    data.append("file", state.selectedFile);

    try {
        const result = await apiFetch(`${API.imports}/upload/nubank`, { method: "POST", body: data });
        const successes = result.sucessos?.length || 0;
        const failures = result.falhas?.length || 0;
        toast(`${successes} lançamento(s) importado(s)${failures ? ` e ${failures} linha(s) rejeitada(s)` : ""}.`);
        selectFile(null);
        await Promise.all([loadImports(), loadMonth()]);
    } catch (error) {
        toast(error.message || "Não foi possível importar o arquivo.", "error");
    } finally {
        button.disabled = !state.selectedFile;
        button.innerHTML = original;
    }
}

async function loadImports() {
    const container = byId("imports-list");
    container.innerHTML = `<div class="loading-panel"><span class="spinner"></span> Carregando...</div>`;
    try {
        const imports = await apiFetch(`${API.imports}/lotes`);
        state.imports = Array.isArray(imports) ? imports : [];
        renderImports();
    } catch (error) {
        container.innerHTML = `<div class="empty-state"><p>${escapeHtml(error.message || "Não foi possível carregar o histórico.")}</p></div>`;
    }
}

function renderImports() {
    const container = byId("imports-list");
    if (!state.imports.length) {
        container.innerHTML = `<div class="empty-state"><span>${icon("icon-file")}</span><h3>Nenhuma importação</h3><p>Os lotes enviados aparecerão aqui.</p></div>`;
        return;
    }
    container.innerHTML = [...state.imports].reverse().map(item => `<div class="import-item">
        <span>${icon("icon-file")}</span>
        <div><strong>${escapeHtml(item.origem)} · ${escapeHtml(item.idLote)}</strong><small>${item.qtdSucessos} importados · ${item.qtdFalhas} falhas · ${item.totalProcessado} linhas</small></div>
        <button class="icon-button" type="button" data-delete-import="${escapeHtml(item.idLote)}" aria-label="Excluir lote">${icon("icon-trash")}</button>
    </div>`).join("");
    container.querySelectorAll("[data-delete-import]").forEach(button => {
        button.addEventListener("click", () => requestImportDeletion(button.dataset.deleteImport));
    });
}

function requestImportDeletion(idLote) {
    setText("confirm-title", "Excluir lote importado?");
    setText("confirm-message", "Todos os lançamentos criados por esta importação também serão removidos.");
    setText("confirm-action", "Excluir lote");
    state.pendingConfirmation = async () => {
        await apiFetch(`${API.imports}?idLote=${encodeURIComponent(idLote)}`, { method: "DELETE" });
        toast("Lote e lançamentos removidos.");
        await Promise.all([loadImports(), loadMonth()]);
    };
    byId("confirm-dialog").showModal();
}

async function checkHealth() {
    const status = byId("connection-status");
    try {
        await apiFetch("/api/sistema/saude");
        status.classList.remove("is-offline");
        status.lastChild.textContent = " Local";
    } catch {
        status.classList.add("is-offline");
        status.lastChild.textContent = " Indisponível";
    }
}

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

function typeMeta(type) {
    return TYPE_META[type] || TYPE_META.AJUSTE_SALDO;
}

function entrySign(entry) {
    return typeMeta(entry.tipo).sign > 0 ? "+ " : "− ";
}

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
    const now = new Date();
    const isCurrent = month === currentYearMonth();
    const day = isCurrent ? now.getDate() : new Date(year, value, 0).getDate();
    return `${month}-${String(day).padStart(2, "0")}`;
}

function money(value) {
    return moneyFormatter.format(number(value));
}

function percent(value) {
    return percentFormatter.format(number(value));
}

function normalizeRate(value) {
    const numeric = number(value);
    return numeric > 2 ? numeric / 100 : numeric;
}

function number(value) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
}

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

function nullIfBlank(value) {
    const trimmed = value.trim();
    return trimmed || null;
}

function icon(id) {
    return `<svg aria-hidden="true"><use href="#${id}"></use></svg>`;
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
    })[character]);
}

function toast(message, type = "success") {
    const element = document.createElement("div");
    element.className = `toast ${type}`;
    element.textContent = message;
    byId("toast-region").appendChild(element);
    setTimeout(() => element.remove(), 4500);
}

function byId(id) {
    return document.getElementById(id);
}

function setText(id, text) {
    byId(id).textContent = text;
}
