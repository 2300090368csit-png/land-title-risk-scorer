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
