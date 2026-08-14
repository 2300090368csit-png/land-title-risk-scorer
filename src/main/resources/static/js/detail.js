// Drives the parcel detail page (parcel.html?id=N): loads one parcel's full
// score breakdown from the backend and renders the score panel + factor cards.

/** Same banding the backend uses for the overall score, applied per-factor for card color. */
function bandFor(rawScore) {
    if (rawScore > 70) return "low";
    if (rawScore >= 40) return "medium";
    return "high";
}

async function loadParcel() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const main = document.querySelector("main");

    if (!id) {
        main.innerHTML = "<p>No parcel id given — go back and pick a parcel from the list.</p>";
        return;
    }

    let parcel;
    try {
        parcel = await fetchJson(`/api/parcels/${encodeURIComponent(id)}`);
    } catch (err) {
        if (err.status === 404) {
            main.innerHTML = `<p>No parcel with id ${escapeHtml(id)}. <a href="/">Back to the list.</a></p>`;
        } else {
            main.innerHTML = "<p>Couldn't load this parcel — is the backend running?</p>";
            console.error(err);
        }
        return;
    }

    document.title = `Parcel ${parcel.surveyNo} — Land Title Risk Scorer`;
    document.getElementById("page-subtitle").textContent = `Survey No. ${parcel.surveyNo} — ${parcel.locationArea}`;

    const badge = document.getElementById("score-badge");
    badge.textContent = parcel.score;
    badge.className = `score-badge ${parcel.riskBand}`;

    document.getElementById("meta-seller").textContent = parcel.sellerName;
    document.getElementById("meta-location").textContent = parcel.locationArea;
    document.getElementById("meta-survey").textContent = parcel.surveyNo;

    const cardsContainer = document.getElementById("factor-cards");
    for (const factor of parcel.factors) {
        const card = document.createElement("div");
        card.className = `factor-card ${bandFor(factor.rawScore)}`;
        card.innerHTML = `
            <div class="factor-head">
                <h3>${escapeHtml(factor.factorName)}</h3>
                <span class="factor-stats">Raw score: ${factor.rawScore.toFixed(1)} / 100 &middot; Weight: ${factor.weightPercent}%</span>
            </div>
            <p>${escapeHtml(factor.explanation)}</p>
        `;
        cardsContainer.appendChild(card);
    }
}

loadParcel();
