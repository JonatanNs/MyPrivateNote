async function updateImg(){

    const formUpdateImg = document.getElementById("formUpdateImg");

    if(formUpdateImg){
        formUpdateImg.addEventListener("submit", async function(event){
            event.preventDefault();

            const password = document.querySelector("input[name='password']").value;
            const fileInput = document.getElementById("fileImg");

            // Vérifier si un fichier est sélectionné
            if (!fileInput.files.length) {
                alert("Veuillez sélectionner une image !");
                return;
            }

            const file = fileInput.files[0];

            // Utilisation de FormData pour envoyer un fichier
            const formData = new FormData();
            formData.append("password", password);
            formData.append("fileImg", file);

            try{
                const response = await fetch("/update-user-img",{
                    method : "POST",
                    body : formData
                });
                const data = await response.json();
                const messageElement = document.getElementById("message");

                if (!response.ok) {
                    messageElement.style.color = "red";
                    messageElement.textContent = data.message;
                    setTimeout(() => { messageElement.textContent = ""; }, 2000);
                    return;
                }
                messageElement.style.color = "green";
                messageElement.textContent = data.message;
                setTimeout(() => { messageElement.textContent = ""; }, 2000);

                window.location.href = "/profil";
            } catch (error){
                console.error("Erreur lors de la modification:", error);
            }
        });
    }
}

function showImg() {
    document.getElementById('fileImg').addEventListener('change', function(event) {
        let file = event.target.files[0]; // Récupère le fichier sélectionné
        let imgPreview = document.getElementById('imgPreview');

        if (!file) return; // Aucun fichier sélectionné

        // Vérifie si c'est bien une image
        if (!file.type.startsWith('image/')) {
            alert("Veuillez sélectionner un fichier image !");
            return;
        }

        // Si l'élément image n'existe pas, le créer
        if (!imgPreview) {
            imgPreview = document.createElement("img");
            imgPreview.id = "imgPreview";
            imgPreview.style.display = "none"; // Cachée au départ
            // Insérer l'image après l'input file
            document.getElementById('fileImg').after(imgPreview);
        }

        let reader = new FileReader();
        reader.onload = function(e) {
            imgPreview.src = e.target.result;
            imgPreview.style.display = 'block'; // Affiche l'image
        };
        reader.readAsDataURL(file);
    });
}

document.addEventListener("DOMContentLoaded", function(){
    showImg();
    updateImg();
});