import { renderActivityChart, renderTypeChart } from "./charts.js";
import { escapeHtml, eventKey, eventTone, formatDate, formatPayload } from "./format.js";
import { filteredEvents, knownTypes, state } from "./state.js";

export function render(elements) {
    const visibleEvents = filteredEvents(elements.searchInput.value);

    renderFilters(elements);
    renderMetrics(elements, visibleEvents);
    renderActivityChart(elements.activityChart, visibleEvents);
    renderTypeChart(elements.typeChart, visibleEvents);
    renderTable(elements, visibleEvents);
    renderInspector(elements);
    renderConnection(elements);
    renderRuntime(elements);
}

function renderFilters(elements) {
    const counts = state.events.reduce((acc, event) => {
        acc.All += 1;
        acc[event.type] = (acc[event.type] || 0) + 1;
        return acc;
    }, { All: 0 });

    elements.typeFilters.innerHTML = knownTypes.map((type) => `
        <button class="filter-button ${state.selectedType === type ? "is-active" : ""}" type="button" data-type="${type}">
            <span>${type}</span>
            <span class="filter-count">${counts[type] || 0}</span>
        </button>
    `).join("");

    elements.unprocessedCount.textContent = state.events.filter((event) => !event.processed).length;
    elements.processedToggle.classList.toggle("is-active", state.onlyUnprocessed);
    elements.processedToggle.setAttribute("aria-pressed", String(state.onlyUnprocessed));
}

function renderMetrics(elements, events) {
    const repos = new Set(events.map((event) => event.repoName).filter(Boolean));
    const processed = events.filter((event) => event.processed).length;

    elements.visibleMetric.textContent = events.length.toLocaleString();
    elements.loadedMetric.textContent = `${state.events.length.toLocaleString()} loaded`;
    elements.liveMetric.textContent = state.liveCount.toLocaleString();
    elements.repoMetric.textContent = repos.size.toLocaleString();
    elements.processedMetric.textContent = events.length === 0 ? "0%" : `${Math.round((processed / events.length) * 100)}%`;
}

function renderTable(elements, events) {
    elements.tableCount.textContent = `${events.length.toLocaleString()} rows`;
    elements.tableSubtitle.textContent = events.length === 0
        ? "No events loaded yet"
        : "Sorted by GitHub event creation time";
    elements.emptyTable.classList.toggle("is-hidden", events.length !== 0);

    elements.eventsTable.innerHTML = events.slice(0, 200).map((event) => {
        const key = eventKey(event);
        const tone = eventTone(event.type);
        return `
            <tr data-key="${escapeHtml(key)}" class="${state.selectedKey === key ? "is-selected" : ""}">
                <td><span class="type-token ${tone}">${escapeHtml(event.type || "Unknown")}</span></td>
                <td><div class="repo-cell" title="${escapeHtml(event.repoName)}">${escapeHtml(event.repoName || "Unknown")}</div></td>
                <td>${escapeHtml(event.actorLogin || "Unknown")}</td>
                <td>${formatDate(event.createdAt)}</td>
                <td class="mono">${escapeHtml(event.gitHubEventId || "not exposed")}</td>
                <td><span class="status-token ${event.processed ? "processed" : ""}">${event.processed ? "Processed" : "Pending"}</span></td>
            </tr>
        `;
    }).join("");
}

function renderInspector(elements) {
    const selected = state.events.find((event) => eventKey(event) === state.selectedKey);

    if (!selected) {
        elements.detailTitle.textContent = "No selection";
        elements.detailType.textContent = "None";
        elements.detailType.className = "type-token neutral";
        elements.detailBody.innerHTML = `<div class="empty-state">Select a row to inspect repository, actor, timestamps, event ID, and payload.</div>`;
        return;
    }

    elements.detailTitle.textContent = selected.repoName || "Unknown repository";
    elements.detailType.textContent = selected.type || "Unknown";
    elements.detailType.className = `type-token ${eventTone(selected.type)}`;
    elements.detailBody.innerHTML = `
        <div class="detail-list">
            ${detailField("Actor", escapeHtml(selected.actorLogin || "Unknown"))}
            ${detailField("Created at", formatDate(selected.createdAt))}
            ${detailField("GitHub event ID", `<code>${escapeHtml(selected.gitHubEventId || "not exposed")}</code>`)}
            ${detailField("Internal row ID", `<code>${escapeHtml(selected.id || "unknown")}</code>`)}
        </div>
        <pre class="payload-view">${escapeHtml(formatPayload(selected.payload))}</pre>
    `;
}

function renderConnection(elements) {
    elements.connectionStatus.className = `connection-state is-${state.connection}`;
    elements.connectionStatus.querySelector("[data-label]").textContent = {
        connected: "Live connected",
        error: "Live offline",
        connecting: "Connecting"
    }[state.connection] || "Connecting";
}

function renderRuntime(elements) {
    elements.messageBox.textContent = state.message;
}

function detailField(label, value) {
    return `
        <div class="detail-field">
            <span>${label}</span>
            <strong>${value}</strong>
        </div>
    `;
}
