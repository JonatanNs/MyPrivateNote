function formLogin() {
    let passwordField = document.getElementById("password");
    let usernameField = document.getElementById("username");
    let submitButton = document.querySelector("#loginForm button[type='submit']");

    if (!passwordField || !usernameField || !submitButton) return;

    // Fonction de validation pour activer/désactiver le bouton
    function validateForm() {
        let password = passwordField.value.trim();
        let username = usernameField.value.trim();

        // Désactiver le bouton si un des champs est vide
        submitButton.disabled = (password === "" || username === "");
    }

    // Ajouter des écouteurs d'événements sur les champs
    passwordField.addEventListener("input", validateForm);
    usernameField.addEventListener("input", validateForm);

    validateForm();
}

async function dataLogin() {
    document.getElementById("loginForm").addEventListener("submit", async function (event) {
        event.preventDefault();

        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;
        const messageElement = document.getElementById("message");

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });
            const data = await response.json().catch(() => {
                throw new Error("Réponse du serveur invalide (non JSON).");
            });
            if (!response.ok) {
                messageElement.style.color = "red";
                messageElement.textContent = data.message;
                setTimeout(() => { messageElement.textContent = ""; }, 2000);
                return;
            }
            // Stocke le token
            sessionStorage.setItem("token", data.token);

            if (!sessionStorage.getItem("token")) {
                throw new Error("⚠️ Aucun token stocké !");
            }

            document.getElementById("username").value = "";
            document.getElementById("password").value = "";

           fetch("/mesNotes", {
                 method: "GET",
                 headers: {
                     "Authorization": "Bearer " + sessionStorage.getItem("token"),
                     "Content-Type": "application/json"
                 }
             })
             .then(response => {
                 if (!response.ok) throw new Error("Accès refusé (403)");
                 return response.text();
             })
             .then(data => {
                 document.open();
                 document.write(data);  // Remplace le contenu de la page avec la réponse
                 document.close();
             })
             .catch(error => {
                 alert("Erreur d'accès aux notes !");
             });

        } catch (error) {
            messageElement.textContent = "Erreur de connexion au serveur.";
            messageElement.style.color = "red";
        }
    });
}

document.addEventListener("DOMContentLoaded", function(){
    formLogin();
    dataLogin();
});