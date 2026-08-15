// Drives history.html: shows the signed-in user's own past score checks,
// most recent first. Uses the same data table as the dashboard so it reads
// as "the same list, filtered to what you've actually looked at."

function renderHistory(rows) {
    const content = document.getElementById("history-content");

    if (rows.length === 0) {
        content.innerHTML = `
            <div class="empty-state">
                <strong>No score checks yet</strong>
                Open any property from the <a href="dashboard.html">Properties</a> tab and it will appear here.
            </div>
        `;
        return;
    }

    const rowsHtml = rows.map(row => {
        const href = `parcel.html?id=${encodeURIComponent(row.parcelId)}`;
        const checkedAt = new Date(row.viewedAt).toLocaleString(undefined, {
            dateStyle: "medium",
            timeStyle: "short"
        });
        return `
            <tr data-href="${href}">
                <td><a class="row-link cell-primary" href="${href}">${escapeHtml(row.surveyNo)}</a></td>
                <td class="cell-muted">${escapeHtml(row.locationArea)}</td>
                <td class="cell-muted">${escapeHtml(checkedAt)}</td>
                <td class="num"><span class="score-pill ${escapeHtml(row.riskBand)}">${row.score}</span></td>
            </tr>
        `;
    }).join("");

    content.innerHTML = `
        <div class="section-head">
            <h2>${rows.length} score check${rows.length === 1 ? "" : "s"}</h2>
        </div>
        <div class="table-wrap">
            <table class="data">
                <thead>
                <tr>
                    <th>Survey no.</th>
                    <th>Location</th>
                    <th>Checked</th>
                    <th class="num">Score</th>
                </tr>
                </thead>
                <tbody>${rowsHtml}</tbody>
            </table>
        </div>
    `;

    content.querySelectorAll("tr[data-href]").forEach(tr => {
        tr.addEventListener("click", () => { window.location.href = tr.dataset.href; });
    });
}

async function loadHistory() {
    const username = await requireAuth();
    if (!username) return;

    let rows;
    try {
        rows = await fetchJson("/api/history");
    } catch (err) {
        document.getElementById("count").textContent = "Couldn't load your history — is the backend running?";
        console.error(err);
        return;
    }

    renderHistory(rows);
}

loadHistory();
