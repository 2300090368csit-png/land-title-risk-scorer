// Drives add.html: submits the form to POST /api/parcels and redirects to
// the new property's detail page once the backend has scored it.

requireAuth();

document.getElementById("add-form").addEventListener("submit", async (event) => {
    event.preventDefault();

    const errorEl = document.getElementById("form-error");
    const submitBtn = document.getElementById("submit-btn");
    errorEl.hidden = true;

    const form = event.target;
    const payload = Object.fromEntries(new FormData(form).entries());

    submitBtn.disabled = true;
    submitBtn.textContent = "Calculating…";

    try {
        const res = await fetch("/api/parcels", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            let message = `Something went wrong (HTTP ${res.status}).`;
            try {
                const errBody = await res.json();
                if (errBody && errBody.message) message = errBody.message;
            } catch {
                // response wasn't JSON — keep the generic message
            }
            throw new Error(message);
        }

        const created = await res.json();
        window.location.href = `parcel.html?id=${created.id}`;
    } catch (err) {
        errorEl.textContent = err.message;
        errorEl.hidden = false;
        submitBtn.disabled = false;
        submitBtn.textContent = "Calculate risk score";
    }
});
