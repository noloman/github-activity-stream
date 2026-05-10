import { fetchRecentEvents } from "./api.js";
import { eventKey } from "./format.js";
import { mergeEvents, replaceEvents, state } from "./state.js";
import { render } from "./views.js";

const elements = {
    activityChart: document.getElementById("activityChart"),
    clearTypeButton: document.getElementById("clearTypeButton"),
    connectionStatus: document.getElementById("connectionStatus"),
    detailBody: document.getElementById("detailBody"),
    detailTitle: document.getElementById("detailTitle"),
    detailType: document.getElementById("detailType"),
    emptyTable: document.getElementById("emptyTable"),
    eventsTable: document.getElementById("eventsTable"),
    loadedMetric: document.getElementById("loadedMetric"),
    liveMetric: document.getElementById("liveMetric"),
    messageBox: document.getElementById("messageBox"),
    pauseButton: document.getElementById("pauseButton"),
    pauseIcon: document.getElementById("pauseIcon"),
    processedMetric: document.getElementById("processedMetric"),
    processedToggle: document.getElementById("processedToggle"),
    refreshButton: document.getElementById("refreshButton"),
    repoMetric: document.getElementById("repoMetric"),
    searchInput: document.getElementById("searchInput"),
    tableCount: document.getElementById("tableCount"),
    tableSubtitle: document.getElementById("tableSubtitle"),
    timeRange: document.getElementById("timeRange"),
    typeChart: document.getElementById("typeChart"),
    typeFilters: document.getElementById("typeFilters"),
    unprocessedCount: document.getElementById("unprocessedCount"),
    visibleMetric: document.getElementById("visibleMetric")
};

async function loadStoredEvents() {
    state.message = "Loading stored events.";
    render(elements);

    try {
        const events = await fetchRecentEvents(elements.timeRange.value);
        replaceEvents(events);
        state.message = events.length === 0
            ? "No stored events in this window yet."
            : `Loaded ${events.length.toLocaleString()} stored events.`;
    } catch (error) {
        state.message = error.message;
    }

    render(elements);
}

function connectLiveStream() {
    if (!window.SockJS || !window.Stomp) {
        state.connection = "error";
        state.message = "SockJS or STOMP could not be loaded. Use Refresh for stored data.";
        render(elements);
        return;
    }

    const socket = new SockJS("/ws");
    const client = Stomp.over(socket);
    client.debug = null;

    client.connect({}, () => {
        state.connection = "connected";
        render(elements);

        client.subscribe("/topic/github-events", (message) => {
            if (state.livePaused) return;

            const event = JSON.parse(message.body);
            state.liveCount += 1;
            mergeEvents([event], "live");
            state.selectedKey = eventKey(event);
            state.message = `Received ${event.type || "event"} from ${event.repoName || "unknown repository"}.`;
            render(elements);
        });
    }, () => {
        state.connection = "error";
        state.message = "Live stream disconnected. Stored data is still available.";
        render(elements);
    });
}

function bindEvents() {
    elements.refreshButton.addEventListener("click", loadStoredEvents);
    elements.searchInput.addEventListener("input", () => render(elements));
    elements.timeRange.addEventListener("change", loadStoredEvents);
    elements.clearTypeButton.addEventListener("click", () => {
        state.selectedType = "All";
        render(elements);
    });

    elements.typeFilters.addEventListener("click", (event) => {
        const button = event.target.closest("[data-type]");
        if (!button) return;

        state.selectedType = button.dataset.type;
        render(elements);
    });

    elements.processedToggle.addEventListener("click", () => {
        state.onlyUnprocessed = !state.onlyUnprocessed;
        render(elements);
    });

    elements.eventsTable.addEventListener("click", (event) => {
        const row = event.target.closest("[data-key]");
        if (!row) return;

        state.selectedKey = row.dataset.key;
        render(elements);
    });

    elements.pauseButton.addEventListener("click", () => {
        state.livePaused = !state.livePaused;
        elements.pauseButton.setAttribute("aria-label", state.livePaused ? "Resume live updates" : "Pause live updates");
        elements.pauseButton.setAttribute("title", state.livePaused ? "Resume live updates" : "Pause live updates");
        elements.pauseIcon.innerHTML = state.livePaused
            ? `<path d="m8 5 11 7-11 7V5Z" fill="currentColor"></path>`
            : `<path d="M8 5v14M16 5v14" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"></path>`;
        state.message = state.livePaused ? "Live updates paused in this browser." : "Live updates resumed.";
        render(elements);
    });
}

bindEvents();
render(elements);
loadStoredEvents();
connectLiveStream();
