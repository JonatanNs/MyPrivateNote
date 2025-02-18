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

document.getElementById("loginForm").addEventListener("submit", async function(event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const messageElement = document.getElementById("message");

    try{
        const response = await fetch("/auth/login", {
            method : "POST",
            headers :  { "Content-Type": "application/json" },
            body : JSON.stringify({ username, password })
        });

        const message = await response.text();

        if(!response.ok){
            messageElement.style.color = "red";

            setTimeout(() => {
                messageElement.textContent = "";
            }, 2000);
        } else{
            messageElement.style.color = "green";

            document.getElementById("username").value = "";
            document.getElementById("password").value = "";

            setTimeout(() => {
                window.location.href = "/mesNotes";
            }, 1000); //1s
        }
        messageElement.textContent = message;

    } catch (error) {
        messageElement.textContent = "Erreur de connexion au serveur.";
        messageElement.style.color = "red";
    }
});

document.addEventListener("DOMContentLoaded", function(){
    formLogin();
});
