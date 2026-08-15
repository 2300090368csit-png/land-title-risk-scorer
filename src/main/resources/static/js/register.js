// Drives register.html.

document.getElementById("register-form").addEventListener("submit", async (event) => {
    event.preventDefault();

    const errorEl = document.getElementById("form-error");
    const submitBtn = document.getElementById("submit-btn");
    errorEl.hidden = true;

    const form = event.target;
    const username = form.username.value.trim();
    const password = form.password.value;
    const confirmPassword = form.confirmPassword.value;

    if (password !== confirmPassword) {
        errorEl.textContent = "Passwords don't match.";
        errorEl.hidden = false;
        return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Creating account…";

    try {
        const res = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            let message = "Couldn't create that account.";
            try {
                const body = await res.json();
                if (body && body.message) message = body.message;
            } catch {
                // keep the generic message
            }
            throw new Error(message);
        }

        // Registering also signs the new account in server-side, so this
        // goes straight to the dashboard rather than back to the login form.
        window.location.href = "dashboard.html";
    } catch (err) {
        errorEl.textContent = err.message;
        errorEl.hidden = false;
        submitBtn.disabled = false;
        submitBtn.textContent = "Create account";
    }
});
