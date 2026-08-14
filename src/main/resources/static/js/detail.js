// Drives the parcel detail page (parcel.html?id=N): loads one parcel's full
// score breakdown and renders the gauge, the weighted contribution bar, and
// a glossary-enriched card per factor.

/** Same banding the backend uses for the overall score, applied per-factor for the bar/card color. */
function bandFor(rawScore) {
    if (rawScore > 70) return "low";
    if (rawScore >= 40) return "medium";
    return "high";
}

function renderScoreSummary(parcel) {
    document.getElementById("gauge-svg-slot").innerHTML = buildGaugeSvg(parcel.score, parcel.riskBand);
    document.getElementById("gauge-number").textContent = parcel.score;

    const bandLabel = document.getElementById("band-label");
    bandLabel.textContent = parcel.riskBand === "low" ? "Low risk"
        : parcel.riskBand === "medium" ? "Medium risk"
        : "High risk";
    bandLabel.className = `score-badge ${parcel.riskBand}`;

    document.getElementById("verdict-text").textContent = verdictFor(parcel.riskBand);
}

function renderContributionBar(factors) {
    const bar = document.getElementById("contribution-bar");
    const legend = document.getElementById("contribution-legend");
    bar.innerHTML = "";
    legend.innerHTML = "";

    for (const factor of factors) {
        const info = factorInfo(factor.factorName);
        const contribution = factor.rawScore * (factor.weightPercent / 100);

        const segment = document.createElement("div");
        segment.className = "segment";
        segment.style.width = `${contribution}%`;
        segment.style.background = info.color;
        segment.title = `${factor.factorName}: ${contribution.toFixed(1)} points`;
        bar.appendChild(segment);

        const item = document.createElement("div");
        item.className = "item";
        item.innerHTML = `
            <span class="swatch" style="background:${info.color}"></span>
            <span>${info.icon} ${escapeHtml(factor.factorName)}</span>
            <span class="amount">${contribution.toFixed(1)} pts</span>
        `;
        legend.appendChild(item);
    }
}

function renderFactorCards(factors) {
    const container = document.getElementById("factor-cards");
    container.innerHTML = "";

    for (const factor of factors) {
        const info = factorInfo(factor.factorName);
        const band = bandFor(factor.rawScore);

        const card = document.createElement("div");
        card.className = "factor-card";
        card.style.borderLeftColor = info.color;
        card.innerHTML = `
            <div class="factor-head">
                <div class="icon-chip" style="background:${info.color}22; color:${info.color}">${info.icon}</div>
                <div class="factor-titles">
                    <h3>${escapeHtml(factor.factorName)}</h3>
                    <div class="factor-blurb">${escapeHtml(info.blurb)}</div>
                </div>
                <div class="factor-stats">
                    <div class="raw">${factor.rawScore.toFixed(0)}<span style="font-weight:400;color:var(--text-faint)">/100</span></div>
                    <div>Weight: ${factor.weightPercent}%</div>
                </div>
            </div>
            <div class="factor-bar-track">
                <div class="factor-bar-fill" style="width:${factor.rawScore}%; background:${info.color}"></div>
            </div>
            <p class="explanation">${escapeHtml(factor.explanation)}</p>
        `;
        container.appendChild(card);
    }
}

async function loadParcel() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const main = document.querySelector("main");

    if (!id) {
        main.innerHTML = '<div class="error-box">No property selected &mdash; go back and pick one from the list.</div>';
        return;
    }

    let parcel;
    try {
        parcel = await fetchJson(`/api/parcels/${encodeURIComponent(id)}`);
    } catch (err) {
        if (err.status === 404) {
            main.innerHTML = `<div class="error-box">No property found with id ${escapeHtml(id)}. <a href="/">Back to the list.</a></div>`;
        } else {
            main.innerHTML = '<div class="error-box">Couldn\'t load this property &mdash; is the backend running?</div>';
            console.error(err);
        }
        return;
    }

    document.title = `Property ${parcel.surveyNo} — Land Title Risk Scorer`;
    document.getElementById("page-tagline").textContent = `Survey No. ${parcel.surveyNo} — ${parcel.locationArea}`;

    document.getElementById("meta-seller").textContent = parcel.sellerName;
    document.getElementById("meta-location").textContent = parcel.locationArea;
    document.getElementById("meta-survey").textContent = parcel.surveyNo;

    renderScoreSummary(parcel);
    renderContributionBar(parcel.factors);
    renderFactorCards(parcel.factors);
}

loadParcel();
