// Drives index.html, the animated splash screen that plays whenever the
// site is opened fresh. Purely cosmetic — it is not a security gate, that's
// what login.html and requireAuth() are for. Auto-advances to the login
// page once the ring-draw animation has had time to finish; the visible
// "Skip →" link covers anyone who doesn't want to wait.

// Timed to land just after the last element finishes animating in
// (ring draw 1.9s; tagline fades in at 1.15s + 0.9s = ~2.05s).
setTimeout(() => {
    window.location.href = "login.html";
}, 2800);
