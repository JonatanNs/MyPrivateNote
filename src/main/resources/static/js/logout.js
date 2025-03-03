function logout(){
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async function (event) {
            event.preventDefault(); // Empêche le lien de suivre son href

            try {
                const response = await fetch("/auth/logout", { method: "POST" });

                if (response.ok) {
                    localStorage.removeItem("token"); // Supprime le token
                    window.location.href = "/"; // Redirige vers l'accueil
                } else {
                    alert("Erreur lors de la déconnexion");
                }
            } catch (error) {
                console.error("Erreur de déconnexion :", error);
                alert("Erreur de déconnexion");
            }
        });
    }
}
document.addEventListener("DOMContentLoaded", function () {
    logout();
});
