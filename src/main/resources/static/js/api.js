export async function fetchRecentEvents(hours) {
    const response = await fetch(`/api/v1/events/recent?hours=${encodeURIComponent(hours)}`);
    if (!response.ok) {
        throw new Error(`Recent events request failed with ${response.status}`);
    }
    return normalizeEvents(await response.json());
}

function normalizeEvents(payload) {
    if (Array.isArray(payload)) {
        return payload;
    }

    if (payload && Array.isArray(payload.content)) {
        return payload.content;
    }

    return [];
}
