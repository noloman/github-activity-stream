export function renderActivityChart(container, events) {
    const buckets = groupByHour(events).slice(-16);
    if (buckets.length === 0) {
        container.innerHTML = `<div class="empty-state">Throughput appears after matching events are stored or streamed.</div>`;
        return;
    }

    const width = 720;
    const height = 220;
    const pad = 24;
    const max = Math.max(...buckets.map((bucket) => bucket.count), 1);
    const step = buckets.length === 1 ? 0 : (width - pad * 2) / (buckets.length - 1);
    const points = buckets.map((bucket, index) => {
        const x = pad + index * step;
        const y = height - pad - (bucket.count / max) * (height - pad * 2);
        return { x, y, count: bucket.count };
    });
    const line = points.map((point, index) => `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`).join(" ");
    const area = `${line} L ${points.at(-1).x} ${height - pad} L ${points[0].x} ${height - pad} Z`;

    container.innerHTML = `
        <svg class="chart" viewBox="0 0 ${width} ${height}" role="img" aria-label="Event throughput chart">
            <line class="chart-grid" x1="${pad}" y1="${pad}" x2="${width - pad}" y2="${pad}"></line>
            <line class="chart-grid" x1="${pad}" y1="${height / 2}" x2="${width - pad}" y2="${height / 2}"></line>
            <line class="chart-grid" x1="${pad}" y1="${height - pad}" x2="${width - pad}" y2="${height - pad}"></line>
            <path class="chart-area" d="${area}"></path>
            <path class="chart-line" d="${line}"></path>
            ${points.map((point) => `<circle cx="${point.x}" cy="${point.y}" r="3.5" fill="var(--surface)" stroke="var(--accent)" stroke-width="2"><title>${point.count} events</title></circle>`).join("")}
            <text class="chart-label" x="${pad}" y="${height - 5}">${buckets.length} buckets</text>
            <text class="chart-label" x="${width - pad}" y="${height - 5}" text-anchor="end">max ${max}</text>
        </svg>
    `;
}

export function renderTypeChart(container, events) {
    const rows = Object.entries(events.reduce((counts, event) => {
        counts[event.type] = (counts[event.type] || 0) + 1;
        return counts;
    }, {})).sort((left, right) => right[1] - left[1]).slice(0, 7);

    if (rows.length === 0) {
        container.innerHTML = `<div class="empty-state">Type distribution needs matching events.</div>`;
        return;
    }

    const width = 430;
    const height = 220;
    const pad = 18;
    const labelWidth = 128;
    const max = Math.max(...rows.map(([, count]) => count), 1);
    const gap = 9;
    const barHeight = Math.max(17, (height - pad * 2 - gap * (rows.length - 1)) / rows.length);

    container.innerHTML = `
        <svg class="chart" viewBox="0 0 ${width} ${height}" role="img" aria-label="Event type distribution chart">
            ${rows.map(([type, count], index) => {
                const y = pad + index * (barHeight + gap);
                const barWidth = ((width - pad * 2 - labelWidth - 44) * count) / max;
                return `
                    <text class="chart-label" x="${pad}" y="${y + barHeight / 2 + 4}">${type.replace("Event", "")}</text>
                    <rect class="chart-bar" x="${labelWidth}" y="${y}" width="${Math.max(6, barWidth)}" height="${barHeight}" rx="4"></rect>
                    <text class="chart-label" x="${width - pad}" y="${y + barHeight / 2 + 4}" text-anchor="end">${count}</text>
                `;
            }).join("")}
        </svg>
    `;
}

function groupByHour(events) {
    const buckets = new Map();

    events.forEach((event) => {
        const date = new Date(event.createdAt);
        if (Number.isNaN(date.getTime())) return;

        date.setMinutes(0, 0, 0);
        const key = date.toISOString();
        buckets.set(key, (buckets.get(key) || 0) + 1);
    });

    return Array.from(buckets.entries())
        .sort(([left], [right]) => new Date(left) - new Date(right))
        .map(([hour, count]) => ({ hour, count }));
}
