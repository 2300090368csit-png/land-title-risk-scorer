// Small shared helpers used by both list.js and detail.js. Kept framework-free
// on purpose — this is a plain fetch()-driven frontend talking to the
// /api/parcels REST endpoints exposed by ParcelApiController.

/**
 * GETs a URL and parses it as JSON, throwing (with the HTTP status attached)
 * on anything other than a 2xx response.
 */
async function fetchJson(url) {
    const res = await fetch(url);
    if (!res.ok) {
        const err = new Error(`Request to ${url} failed with status ${res.status}`);
        err.status = res.status;
        throw err;
    }
    return res.json();
}

/** Escapes text before it goes into innerHTML, so parcel/seller data can't break the page. */
function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}

/**
 * Plain-English glossary for each of the 5 factors the backend can return.
 * This is what turns "RERA Registration" from jargon into something a
 * first-time visitor understands at a glance — an icon, an identity color
 * (used consistently in the bar chart and card accents), and a one-line
 * "what this actually checks" blurb, independent of the specific per-parcel
 * explanation the backend already provides.
 */
const FACTOR_INFO = {
    "Encumbrance Certificate": {
        icon: "📜",
        color: "#4f46e5",
        blurb: "Checks government records for any old loans or legal claims still attached to this land."
    },
    "Litigation Status": {
        icon: "⚖️",
        color: "#7c3aed",
        blurb: "Checks whether the property is currently tied up in any court case."
    },
    "Layout Approval": {
        icon: "🏛️",
        color: "#0d9488",
        blurb: "Checks whether the local planning authority (CRDA, VMRDA, or DTCP) approved this residential layout."
    },
    "RERA Registration": {
        icon: "📋",
        color: "#2563eb",
        blurb: "Checks registration under India's Real Estate Regulation Act, required for projects marketed for sale."
    },
    "MeeBhoomi Digital Record": {
        icon: "🗺️",
        color: "#db2777",
        blurb: "Checks whether the paper records match Andhra Pradesh's official digital land database."
    }
};

/** Fallback so an unrecognized factor name (e.g. a future 6th factor) still renders sensibly. */
function factorInfo(factorName) {
    return FACTOR_INFO[factorName] ?? { icon: "🔎", color: "#6b7280", blurb: "" };
}

/** One short sentence translating a risk band into plain advice. */
function verdictFor(riskBand) {
    if (riskBand === "low") return "This property's paperwork looks clean across the board — a good candidate to proceed with standard legal checks.";
    if (riskBand === "medium") return "Some real issues here. Worth a closer look at the flagged factors below before proceeding.";
    return "Multiple serious flags. Recommend a full legal review before taking this any further.";
}

/** Builds the inline SVG for the circular score gauge (0-100 -> ring fill). */
function buildGaugeSvg(score, riskBand) {
    const radius = 52;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference * (1 - Math.max(0, Math.min(100, score)) / 100);
    return `
        <svg viewBox="0 0 120 120">
            <circle class="gauge-track" cx="60" cy="60" r="${radius}"></circle>
            <circle class="gauge-progress ${riskBand}" cx="60" cy="60" r="${radius}"
                    stroke-dasharray="${circumference}" stroke-dashoffset="${offset}"></circle>
        </svg>
    `;
}
