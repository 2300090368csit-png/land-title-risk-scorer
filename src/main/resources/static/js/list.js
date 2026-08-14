// Drives the parcel list page (index.html): loads every parcel from the
// backend and renders one table row per parcel, each linking to its detail page.

async function loadParcels() {
    const countEl = document.getElementById("count");
    const tbody = document.querySelector("#parcel-table tbody");

    let rows;
    try {
        rows = await fetchJson("/api/parcels");
    } catch (err) {
        countEl.textContent = "Couldn't load parcels — is the backend running?";
        console.error(err);
        return;
    }

    countEl.textContent = `${rows.length} parcels on record.`;

    for (const row of rows) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${escapeHtml(row.surveyNo)}</td>
            <td>${escapeHtml(row.locationArea)}</td>
            <td>${escapeHtml(row.sellerName)}</td>
            <td>
                <a href="parcel.html?id=${encodeURIComponent(row.id)}">
                    <span class="score-badge ${escapeHtml(row.riskBand)}">${row.score}</span>
                </a>
            </td>
        `;
        tbody.appendChild(tr);
    }
}

loadParcels();
