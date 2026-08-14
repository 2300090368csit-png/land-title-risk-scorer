// Drives the parcel list page (index.html): loads every parcel from the
// backend, renders the summary stat tiles, and shows one card per parcel.

function renderStats(rows) {
    const statsRow = document.getElementById("stats-row");
    const total = rows.length;
    const avg = total === 0 ? 0 : Math.round(rows.reduce((sum, r) => sum + r.score, 0) / total);
    const lowCount = rows.filter(r => r.riskBand === "low").length;
    const mediumCount = rows.filter(r => r.riskBand === "medium").length;
    const highCount = rows.filter(r => r.riskBand === "high").length;

    statsRow.innerHTML = `
        <div class="stat-tile">
            <div class="value">${total}</div>
            <div class="label">Properties scored</div>
        </div>
        <div class="stat-tile">
            <div class="value">${avg}</div>
            <div class="label">Average risk score</div>
        </div>
        <div class="stat-tile low">
            <div class="value">${lowCount}</div>
            <div class="label">Low risk (&gt; 70)</div>
        </div>
        <div class="stat-tile medium">
            <div class="value">${mediumCount}</div>
            <div class="label">Medium risk (40&ndash;70)</div>
        </div>
        <div class="stat-tile high">
            <div class="value">${highCount}</div>
            <div class="label">High risk (&lt; 40)</div>
        </div>
    `;
}

function renderCards(rows) {
    const grid = document.getElementById("parcel-grid");
    grid.innerHTML = "";

    for (const row of rows) {
        const card = document.createElement("a");
        card.className = "parcel-card";
        card.href = `parcel.html?id=${encodeURIComponent(row.id)}`;
        card.innerHTML = `
            <div class="row-top">
                <div>
                    <div class="survey">${escapeHtml(row.surveyNo)}</div>
                    <div class="location">${escapeHtml(row.locationArea)}</div>
                </div>
                <span class="score-chip ${escapeHtml(row.riskBand)}">${row.score}</span>
            </div>
            <div class="seller">Seller: ${escapeHtml(row.sellerName)}</div>
        `;
        grid.appendChild(card);
    }
}

async function loadParcels() {
    const countEl = document.getElementById("count");
    let rows;
    try {
        rows = await fetchJson("/api/parcels");
    } catch (err) {
        countEl.textContent = "Couldn't load properties — is the backend running?";
        console.error(err);
        return;
    }

    countEl.textContent = `${rows.length} properties analyzed below. Click any one to see the full breakdown.`;
    renderStats(rows);
    renderCards(rows);
}

loadParcels();
