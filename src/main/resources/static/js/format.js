export function eventKey(event) {
    return event.gitHubEventId || event.id || `${event.type}-${event.repoName}-${event.createdAt}`;
}

export function eventTone(type = "") {
    const normalized = type.toLowerCase();
    if (normalized.includes("push") || normalized.includes("watch")) return "push";
    if (normalized.includes("pull") || normalized.includes("create")) return "pull";
    if (normalized.includes("issue") || normalized.includes("delete")) return "issue";
    if (normalized.includes("fork")) return "fork";
    return "other";
}

export function formatDate(value) {
    const date = new Date(value);
    if (!value || Number.isNaN(date.getTime())) {
        return "Unknown";
    }

    return new Intl.DateTimeFormat(undefined, {
        month: "short",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}

export function formatPayload(payload) {
    if (!payload) {
        return "{}";
    }

    try {
        return JSON.stringify(JSON.parse(payload), null, 2);
    } catch {
        return payload;
    }
}

export function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}
