// Small shared helpers used by every page's own script. Kept framework-free
// on purpose — this is a plain fetch()-driven frontend talking to the REST
// endpoints exposed by the backend controllers.

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
 * Line icons, drawn as inline SVG rather than emoji.
 *
 * Emoji render differently on every OS (and read as informal), so each of the
 * 5 factors gets a real drawn glyph instead. They inherit `currentColor`, so
 * the same markup picks up whichever factor colour it's placed in.
 */
const ICONS = {
    // certificate / document — Encumbrance Certificate
    document: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>',
    // balance scales — Litigation
    scales: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v18"/><path d="M8 21h8"/><path d="M4 7h16"/><path d="M7 7l-3 7a3 3 0 0 0 6 0z"/><path d="M17 7l-3 7a3 3 0 0 0 6 0z"/></svg>',
    // building — Layout approval
    building: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 21h18"/><path d="M5 21V7l7-4 7 4v14"/><path d="M10 21v-5h4v5"/><path d="M9 10h.01"/><path d="M15 10h.01"/></svg>',
    // shield with check — RERA registration
    shield: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><path d="M9 12l2 2 4-4"/></svg>',
    // map — MeeBhoomi digital record
    map: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M1 6v16l7-4 8 4 7-4V2l-7 4-8-4-7 4z"/><path d="M8 2v16"/><path d="M16 6v16"/></svg>',
    // fallback
    search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>',
    arrowLeft: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:.9em;height:.9em"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>',
    plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:.95em;height:.95em"><path d="M12 5v14"/><path d="M5 12h14"/></svg>'
};

/**
 * Plain-English glossary for each of the 5 factors the backend can return.
 * This is what turns "RERA Registration" from jargon into something a
 * first-time visitor understands at a glance — an icon, an identity colour
 * (used consistently in the bar chart and card accents), and a one-line
 * "what this actually checks" blurb, independent of the specific per-parcel
 * explanation the backend already provides.
 */
const FACTOR_INFO = {
    "Encumbrance Certificate": {
        icon: ICONS.document,
        color: "#1e40af",
        blurb: "Government record of any loan or legal claim still attached to this land."
    },
    "Litigation Status": {
        icon: ICONS.scales,
        color: "#6d28d9",
        blurb: "Whether the property is currently tied up in any court case."
    },
    "Layout Approval": {
        icon: ICONS.building,
        color: "#0f766e",
        blurb: "Whether the planning authority (CRDA, VMRDA or DTCP) approved this layout."
    },
    "RERA Registration": {
        icon: ICONS.shield,
        color: "#b91c1c",
        blurb: "Registration under India's Real Estate Regulation Act, required for marketed projects."
    },
    "MeeBhoomi Digital Record": {
        icon: ICONS.map,
        color: "#a16207",
        blurb: "Whether paper records match Andhra Pradesh's official digital land database."
    }
};

/** Fallback so an unrecognised factor name (e.g. a future 6th factor) still renders sensibly. */
function factorInfo(factorName) {
    return FACTOR_INFO[factorName] ?? { icon: ICONS.search, color: "#475569", blurb: "" };
}

/** One short sentence translating a risk band into plain advice. */
function verdictFor(riskBand) {
    if (riskBand === "low") return "Paperwork looks clean across all five checks. A reasonable candidate to proceed with standard legal verification.";
    if (riskBand === "medium") return "Some real issues found. Review the flagged checks below before committing to this property.";
    return "Multiple serious flags. A full legal review is strongly recommended before taking this any further.";
}

/** Human label for a risk band. */
function bandLabel(riskBand) {
    if (riskBand === "low") return "Low risk";
    if (riskBand === "medium") return "Medium risk";
    return "High risk";
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

/**
 * Every protected page (dashboard, detail, add, history) calls this first.
 * It asks the backend "am I logged in?" via GET /api/auth/me — that endpoint
 * requires a session (see SecurityConfig), so an expired or missing session
 * comes back 401 and this sends the browser to login.html. On success it
 * also paints the shared nav bar, since every protected page needs one and
 * none of them should have to duplicate that markup.
 *
 * @returns {Promise<string|null>} the signed-in username, or null if this
 *          function has already redirected the browser away (nothing further
 *          should run on the calling page in that case).
 */
async function requireAuth() {
    try {
        const res = await fetch("/api/auth/me");
        if (!res.ok) {
            window.location.href = "login.html";
            return null;
        }
        const data = await res.json();
        renderNav(data.username);
        return data.username;
    } catch (err) {
        window.location.href = "login.html";
        return null;
    }
}

/** Fills the #app-nav placeholder every protected page has right after <body>. */
function renderNav(username) {
    const root = document.getElementById("app-nav");
    if (!root) return;

    // Highlight whichever nav entry matches the page we're on.
    const page = window.location.pathname.split("/").pop() || "dashboard.html";
    const isDash = page === "dashboard.html" || page === "parcel.html" || page === "add.html";
    const isHistory = page === "history.html";

    root.innerHTML = `
        <nav class="app-nav">
            <div class="app-nav-left">
                <a href="dashboard.html" class="app-nav-brand">
                    <span class="app-nav-mark">LT</span>
                    Land Title Risk Scorer
                </a>
                <a href="dashboard.html" class="app-nav-link${isDash ? " is-active" : ""}">Properties</a>
                <a href="history.html" class="app-nav-link${isHistory ? " is-active" : ""}">History</a>
            </div>
            <div class="app-nav-right">
                <span class="app-nav-user">Signed in as <b>${escapeHtml(username)}</b></span>
                <button id="logout-btn" class="nav-logout-btn" type="button">Log out</button>
            </div>
        </nav>
    `;

    document.getElementById("logout-btn").addEventListener("click", async () => {
        try {
            await fetch("/api/auth/logout", { method: "POST" });
        } finally {
            window.location.href = "login.html";
        }
    });
}
