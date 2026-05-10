import { eventKey } from "./format.js";

const initialState = {
    events: [],
    selectedKey: null,
    selectedType: "All",
    onlyUnprocessed: false,
    livePaused: false,
    liveCount: 0,
    message: "Waiting for stored events.",
    connection: "connecting"
};

export const state = structuredClone(initialState);

export const knownTypes = [
    "All",
    "PushEvent",
    "PullRequestEvent",
    "IssuesEvent",
    "WatchEvent",
    "ForkEvent",
    "CreateEvent"
];

export function replaceEvents(events, source = "stored") {
    state.events = [];
    mergeEvents(events, source);
}

export function mergeEvents(events, source = "stored") {
    const byKey = new Map(state.events.map((event) => [eventKey(event), event]));
    events.forEach((event) => byKey.set(eventKey(event), { ...event, source }));

    state.events = Array.from(byKey.values())
        .sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt));

    if (!state.selectedKey && state.events.length > 0) {
        state.selectedKey = eventKey(state.events[0]);
    }
}

export function filteredEvents(searchText) {
    const query = searchText.trim().toLowerCase();

    return state.events.filter((event) => {
        const searchable = [
            event.type,
            event.repoName,
            event.actorLogin,
            event.gitHubEventId,
            event.payload
        ].filter(Boolean).join(" ").toLowerCase();

        return (!query || searchable.includes(query))
            && (state.selectedType === "All" || event.type === state.selectedType)
            && (!state.onlyUnprocessed || !event.processed);
    });
}
