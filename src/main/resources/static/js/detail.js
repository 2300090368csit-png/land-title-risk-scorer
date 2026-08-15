// Drives parcel.html (?id=N): loads one property's full score breakdown and
// renders the gauge, the record facts, the weighted contribution bar, and a
// glossary-enriched card per factor.

/** Same banding the backend uses for the overall score, applied per-factor for card colour. */
function bandFor(rawScore) {
    if (rawScore > 70) return "low";
    if (rawScore >= 40) return "medium";
    return "high";
}

/** Turns an enum value like ACTIVE_SUIT into "Active suit" for display. */
function humanizeEnum(value) {
    const spaced = String(value).replace(/_/g, " ").toLowerCase();
    return spaced.charAt(0).toUpperCase() + spaced.slice(1);
}

function renderScorePanel(parcel) {
    document.getElementById("gauge-svg-slot").innerHTML = buildGaugeSvg(parcel.score, parcel.riskBand);
    document.getElementById("gauge-number").textContent = parcel.score;

    const label = document.getElementById("band-label");
    label.textContent = bandLabel(parcel.riskBand);
    label.className = `score-pill ${parcel.riskBand}`;

    document.getElementById("verdict-text").textContent = verdictFor(parcel.riskBand);

    // When a factor reported a disqualifying condition the displayed number is a
    // cap, not the weighted sum — say so, otherwise the arithmetic on the
    // breakdown bar below won't add up to the score above it and the whole
    // "every point is explainable" promise breaks.
    const capEl = document.getElementById("cap-note");
    if (parcel.ceilingReason) {
        capEl.innerHTML = `${escapeHtml(parcel.ceilingReason)}
            <span class="cap-math">Weighted total was ${parcel.uncappedScore}.</span>`;
        capEl.hidden = false;
    } else {
        capEl.hidden = true;
    }
}

function renderFacts(parcel) {
    const facts = [
        ["Survey number", parcel.surveyNo],
        ["Location", parcel.locationArea],
        ["Seller", parcel.sellerName],
        ["Section 22A list", humanizeEnum(parcel.prohibitedStatus)],
        ["Land classification", humanizeEnum(parcel.landClassification)],
        ["Encumbrance certificate", humanizeEnum(parcel.ecStatus)],
        ["Litigation", humanizeEnum(parcel.litigationStatus)],
        ["Pattadar / ROR-1B", humanizeEnum(parcel.pattadarMatch)],
        ["Layout approval", humanizeEnum(parcel.layoutApproval)],
        ["NALA conversion", humanizeEnum(parcel.nalaStatus)],
        ["RERA", humanizeEnum(parcel.reraStatus)],
        ["MeeBhoomi record", humanizeEnum(parcel.meeBhoomiMatch)]
    ];

    document.getElementById("facts").innerHTML = facts
        .map(([k, v]) => `<div class="fact-row"><span class="k">${escapeHtml(k)}</span><span class="v">${escapeHtml(v)}</span></div>`)
        .join("");
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
            <span>${escapeHtml(factor.factorName)}</span>
            <span class="amount">${contribution.toFixed(1)}</span>
        `;
        legend.appendChild(item);
    }
}

function renderFactorCards(factors) {
    const container = document.getElementById("factor-cards");
    container.innerHTML = "";

    for (const factor of factors) {
        const info = factorInfo(factor.factorName);

        const card = document.createElement("div");
        card.className = "factor-card";
        card.innerHTML = `
            <div class="factor-head">
                <div class="factor-icon" style="background:${info.color}14; color:${info.color}">${info.icon}</div>
                <div class="factor-titles">
                    <h3>${escapeHtml(factor.factorName)}</h3>
                    <div class="factor-blurb">${escapeHtml(info.blurb)}</div>
                </div>
                <div class="factor-stats">
                    <div class="raw">${factor.rawScore.toFixed(0)}<span>/100</span></div>
                    <div class="weight">Weight ${factor.weightPercent}%</div>
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
    const username = await requireAuth();
    if (!username) return;

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const main = document.querySelector("main");

    if (!id) {
        main.innerHTML = '<div class="empty-state"><strong>No property selected</strong>Go back and pick one from the list.</div>';
        return;
    }

    let parcel;
    try {
        parcel = await fetchJson(`/api/parcels/${encodeURIComponent(id)}`);
    } catch (err) {
        if (err.status === 404) {
            main.innerHTML = `<div class="empty-state"><strong>Property not found</strong>No property with id ${escapeHtml(id)}. <a href="dashboard.html">Back to the list.</a></div>`;
        } else {
            main.innerHTML = '<div class="empty-state"><strong>Couldn\'t load this property</strong>Is the backend still running?</div>';
            console.error(err);
        }
        return;
    }

    document.title = `${parcel.surveyNo} — Land Title Risk Scorer`;
    document.getElementById("page-title").textContent = `Survey no. ${parcel.surveyNo}`;
    document.getElementById("page-sub").textContent = `${parcel.locationArea} · Seller: ${parcel.sellerName}`;

    renderScorePanel(parcel);
    renderFacts(parcel);
    renderContributionBar(parcel.factors);
    renderFactorCards(parcel.factors);
}

loadParcel();
