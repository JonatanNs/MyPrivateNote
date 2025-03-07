
function saveNote() {
    // Vérifie si l'éditeur existe dans la page avant de l'initialiser
    const editorElement = document.getElementById("editor");
    if (!editorElement) {
        console.error("Erreur : L'élément #editor n'existe pas !");
        return;
    }

    // Initialisation de Quill
    const Font = Quill.import('formats/font');
    const Delta = Quill.import('delta');

    Font.whitelist = [
        'arial', 'comic-sans', 'courier-new', 'georgia', 'helvetica', 'times-new-roman', 'trebuchet-ms', 'verdana',
        'impact', 'lucida-console', 'monospace', 'serif', 'sans-serif', 'fantasy', 'cursive', 'garamond', 'tahoma',
        'brush-script-mt', 'calibri', 'cambria', 'candara', 'consolas', 'futura', 'franklin-gothic', 'rockwell'
    ];
    Quill.register(Font, true);

    const quill = new Quill('#editor', {
        modules: {
            toolbar: [
                [{ header: [1, 2, 3, 4, 5, 6, false] }],
                [{ font: Font.whitelist.map(font => font) }],
                [{ size: ['small', false, 'large', 'huge'] }],
                [{ color: [] }, { background: [] }],
                ['bold', 'italic', 'underline', 'strike'],
                [{ script: 'sub' }, { script: 'super' }],
                [{ blockquote: true }, { code: true }],
                [{ list: 'ordered' }, { list: 'bullet' }],
                [{ indent: '-1' }, { indent: '+1' }],
                [{ align: [] }],
                ['link', 'image', 'video'],
                ['clean']
            ],
            clipboard: {
                matchers: [
                    ['img', (node, delta) => {
                        node.setAttribute('target', '_blank');
                        return delta;
                    }]
                ]
            }
        },
        placeholder: 'Rédigez votre texte ici...',
        theme: 'snow',
    });

    quill.on('text-change', function(delta, oldDelta, source) {
        const editorElement = document.querySelector('#editor');
        if (editorElement && editorElement.classList.contains('ql-editor')) {
            editorElement.classList.remove('ql-editor');
        }
    });

    // Intercepter l'ajout de classes d'alignement pour les titres
    quill.on('text-change', function (delta, oldDelta, source) {
        const headerElements = document.querySelectorAll('#editor h1, #editor h2, #editor h3, #editor h4, #editor h5, #editor h6');
        headerElements.forEach(header => {
            // Supprimer la classe ql-align-justify des titres
            header.classList.remove('ql-align-justify');
        });
    });

    // Limite la taille des images
    quill.clipboard.addMatcher('img', (node, delta) => {
        let imageUrl = delta.ops[0].insert.image;
        return new Delta().insert({ image: imageUrl });
    });

    // Limite la taille des vidéos
    quill.clipboard.addMatcher('iframe', (node, delta) => {
        let videoUrl = delta.ops[0].insert.video;
        return new Delta().insert({ video: videoUrl });
    });

    // Applique les polices aux sélecteurs
    document.querySelectorAll('.ql-font .ql-picker-item, .ql-font .ql-picker-label').forEach(item => {
        const fontName = item.getAttribute('data-value');
        if (fontName) {
            item.style.fontFamily = fontName;
        }
    });

    const form = document.getElementById("saveForm");
    const hiddenInput = document.getElementById("hiddenNote");
    const previewElement = document.getElementById("note-preview");

    if (form) {
        form.addEventListener("submit", async function (event) {
            event.preventDefault();

            const fullContent = quill.root.innerHTML;

            hiddenInput.value = fullContent;
            console.log("Contenu envoyé :", hiddenInput.value);

            // Affiche l'aperçu sans images avant d'envoyer la requête
            if (previewElement) {
                previewElement.innerHTML = previewContent;
            }

            const response = await fetch("/save-note", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ note: hiddenInput.value })
            });

            if (!response.ok) {
                console.error("Erreur lors de l'enregistrement de la note");
                return;
            }

            const data = await response.json();
            window.location.href = `/visuel-note?id=${data.id}`; // Rediriger vers la page de la note
        });
    }
}

async function deleteNote() {
    const noteForm = document.getElementById("deleteNote");
    if (!noteForm) {
        console.error("Erreur : L'élément #deleteNote n'existe pas !");
        return;
    }

    noteForm.addEventListener("submit", async function(event) {
        event.preventDefault();

        const noteId = noteForm.querySelector("input[name='id']").value;
        if (!noteId) {
            console.error("Erreur : ID de la note manquant !");
            return;
        }

        try {
            const response = await fetch("/delete-note", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ id: noteId }) // Envoi de l'ID uniquement
            });

            const messageElement = document.getElementById("message");

            if (!response.ok) {
                const data = await response.json();
                messageElement.style.color = "red";
                messageElement.textContent = data.message || "Erreur lors de la suppression.";
                setTimeout(() => { messageElement.textContent = ""; }, 2000);
                return;
            }

            messageElement.style.color = "green";
            messageElement.textContent = "Note supprimée avec succès !";

            // Attendre un peu avant la redirection
            setTimeout(() => {
                window.location.href = "/mesNotes";
            }, 1000);
        } catch (error) {
            console.error("Erreur lors de la suppression :", error);
        }
    });
}
function updateNote() {
    const btnUpdate = document.getElementById("updateNote");
    const note = document.getElementById("note");
    const formUpdate = document.getElementById("saveFormUpdate");

    if (btnUpdate) {
        btnUpdate.addEventListener("click", function () {
            note.classList.toggle("hidden");
            formUpdate.classList.toggle("hidden");
        });
    }
}

async function pastNoteUpdate() {
    const formUpdate = document.getElementById("saveFormUpdate");
    const contentUpdate = document.getElementById("contentUpdate");
    const editor = document.getElementById("editor");

    if (formUpdate) {
        formUpdate.addEventListener("submit", async function (event) {
            event.preventDefault(); // Empêche le rechargement de la page

            contentUpdate.value = editor.innerHTML; // Copie le contenu HTML dans l'input caché

            const noteId = formUpdate.querySelector("input[name='note_id']").value;
            const content = contentUpdate.value;

            try {
                const response = await fetch("/update-note", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: new URLSearchParams({ note_id: noteId, content: content })
                });

                const message = document.getElementById("message");

                if (!response.ok) {
                    const data = await response.json();
                    message.textContent = data.message || "Erreur lors de la mise à jour.";
                    message.style.color = "red";
                } else {
                    message.textContent = "Note mise à jour avec succès !";
                    message.style.color = "green";
                    setTimeout(() => { window.location.href = "/note?id=" + noteId; }, 2000);
                }
                setTimeout(() => { message.textContent = ""; }, 2000);

            } catch (error) {
                console.error("Erreur lors de la mise à jour :", error);
            }
        });
    }
}

document.addEventListener("DOMContentLoaded", function() {
    saveNote()
    deleteNote()
    updateNote()
    pastNoteUpdate()
});
