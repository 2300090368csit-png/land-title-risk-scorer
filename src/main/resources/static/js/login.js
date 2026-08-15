// Drives login.html.

// If there's already a valid session (e.g. the user hit the back button
// after logging in), skip straight past the form instead of making them
// log in again.
(async function redirectIfAlreadySignedIn() {
    try {
        const res = await fetch("/api/auth/me");
        if (res.ok) {
            window.location.href = "dashboard.html";
        }
    } catch {
        // network hiccup — just let them use the form normally
    }
})();

document.getElementById("login-form").addEventListener("submit", async (event) => {
    event.preventDefault();

    const errorEl = document.getElementById("form-error");
    const submitBtn = document.getElementById("submit-btn");
    errorEl.hidden = true;

    const payload = Object.fromEntries(new FormData(event.target).entries());

    submitBtn.disabled = true;
    submitBtn.textContent = "Signing in…";

    try {
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            let message = "Invalid username or password.";
            try {
                const body = await res.json();
                if (body && body.message) message = body.message;
            } catch {
                // keep the generic message
            }
            throw new Error(message);
        }

        window.location.href = "dashboard.html";
    } catch (err) {
        errorEl.textContent = err.message;
        errorEl.hidden = false;
        submitBtn.disabled = false;
        submitBtn.textContent = "Sign in";
    }
});
