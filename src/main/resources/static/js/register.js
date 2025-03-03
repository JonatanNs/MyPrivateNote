function checkConfirmPassword() {
    let passwordField = document.getElementById("password");
    let confirmPasswordField = document.getElementById("confirmPassword");
    let errorMessage = document.getElementById("errorMessage");
    let submitButton = document.querySelector("#registerForm button[type='submit']");

    if (!passwordField || !confirmPasswordField || !errorMessage || !submitButton) return;

    submitButton.disabled = true;

    function validatePasswordMatch() {
        let password = passwordField.value;
        let confirmPassword = confirmPasswordField.value;

        if (password !== confirmPassword) {
            errorMessage.textContent = "Les mots de passe ne correspondent pas.";
            errorMessage.style.color = "red";
            submitButton.disabled = true; // Désactiver le bouton d'inscription
        } else {
            errorMessage.textContent = "";
            submitButton.disabled = false; // Réactiver le bouton si c'est bon
        }
    }
    confirmPasswordField.addEventListener("input", validatePasswordMatch);
    confirmPasswordField.addEventListener("blur", validatePasswordMatch);
}

function responseMessage(){
    const registerForm = document.getElementById("registerForm");
    if(registerForm){
        registerForm.addEventListener("submit", async function(event) {
            event.preventDefault();

            const username = document.getElementById("username").value;
            const email = document.getElementById("email").value;
            const password = document.getElementById("password").value;
            const messageElement = document.getElementById("message");

            try {
                const response = await fetch("/auth/register", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ username, email, password })
                });

                const message = await response.text(); // Récupérer la réponse du backend

                if (!response.ok) {
                        messageElement.style.color = "red";
                        setTimeout(() => {
                            messageElement.textContent = "";
                        }, 3000); //3s
                } else {
                        messageElement.style.color = "green";

                        setTimeout(() => {
                            messageElement.textContent = "";
                        }, 3000); //3s

                    // Vider les champs du formulaire
                    document.getElementById("username").value = "";
                    document.getElementById("email").value = "";
                    document.getElementById("password").value = "";
                    document.getElementById("confirmPassword").value = "";

                    setTimeout(() => {
                        window.location.href = "/connexion";
                    }, 2000); //2s
                }

                messageElement.textContent = message;

            } catch (error) {
                messageElement.textContent = "Erreur de connexion au serveur.";
                messageElement.style.color = "red";
            }
        });
    }
}

document.addEventListener("DOMContentLoaded", function() {
    responseMessage()
    checkConfirmPassword();
});
