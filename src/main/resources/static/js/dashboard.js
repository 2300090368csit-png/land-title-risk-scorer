// Drives dashboard.html: loads every property from the backend, renders the
// summary stat strip, and fills the data table. Requires a signed-in session
// — requireAuth() bounces to login.html if there isn't one.

function renderStats(rows) {
    const total = rows.length;
    const avg = total === 0 ? 0 : Math.round(rows.reduce((sum, r) => sum + r.score, 0) / total);
    const count = band => rows.filter(r => r.riskBand === band).length;

    document.getElementById("stats-row").innerHTML = `
        <div class="stat-tile">
            <div class="label">Properties</div>
            <div class="value">${total}</div>
        </div>
        <div class="stat-tile">
            <div class="label">Average score</div>
            <div class="value">${avg}</div>
        </div>
        <div class="stat-tile low">
            <div class="label">Low risk</div>
            <div class="value">${count("low")}</div>
        </div>
        <div class="stat-tile medium">
            <div class="label">Medium risk</div>
            <div class="value">${count("medium")}</div>
        </div>
        <div class="stat-tile high">
            <div class="label">High risk</div>
            <div class="value">${count("high")}</div>
        </div>
    `;
}

function renderRows(rows) {
    const tbody = document.getElementById("parcel-rows");
    tbody.innerHTML = "";

    for (const row of rows) {
        const tr = document.createElement("tr");
        const href = `parcel.html?id=${encodeURIComponent(row.id)}`;

        tr.innerHTML = `
            <td><a class="row-link cell-primary" href="${href}">${escapeHtml(row.surveyNo)}</a></td>
            <td class="cell-muted">${escapeHtml(row.locationArea)}</td>
            <td class="cell-muted">${escapeHtml(row.sellerName)}</td>
            <td class="num"><span class="score-pill ${escapeHtml(row.riskBand)}">${row.score}</span></td>
            <td>
                <span class="band-tag">
                    <span class="band-dot ${escapeHtml(row.riskBand)}"></span>${escapeHtml(bandLabel(row.riskBand))}
                </span>
            </td>
        `;

        // The whole row is clickable, not just the survey-number link — the
        // cursor:pointer on tr in the stylesheet advertises that.
        tr.addEventListener("click", () => { window.location.href = href; });
        tbody.appendChild(tr);
    }
}

async function loadParcels() {
    const username = await requireAuth();
    if (!username) return; // requireAuth() already redirected to login.html

    const countEl = document.getElementById("count");
    let rows;
    try {
        rows = await fetchJson("/api/parcels");
    } catch (err) {
        countEl.textContent = "Couldn't load properties — is the backend running?";
        console.error(err);
        return;
    }

    countEl.textContent = `All properties (${rows.length})`;
    renderStats(rows);
    renderRows(rows);
}

loadParcels();
