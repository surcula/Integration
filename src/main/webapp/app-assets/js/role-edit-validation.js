(function () {
    function getForm() {
        return document.querySelector("form[id$='roleForm']");
    }

    function getInput(form) {
        return form ? form.querySelector(".role-edit-input") : null;
    }

    function getSaveButton(form) {
        return form ? form.querySelector(".role-edit-save-button") : null;
    }

    function getFeedback(input) {
        return input.parentElement.querySelector(".invalid-feedback");
    }

    function showError(input, message) {
        const feedback = getFeedback(input);

        input.classList.add("is-invalid");

        if (feedback) {
            feedback.textContent = message;
            feedback.style.display = "block";
        }
    }

    function hideError(input) {
        const feedback = getFeedback(input);

        input.classList.remove("is-invalid");

        if (feedback) {
            feedback.textContent = "";
            feedback.style.display = "";
        }
    }

    function validateRoleName(input, showMessage) {
        const value = input.value.trim();
        const minLength = parseInt(input.dataset.minLength || "0", 10);
        const maxLength = parseInt(input.dataset.maxLength || "0", 10);

        if (value.length === 0) {
            if (showMessage) {
                showError(input, input.dataset.requiredMessage);
            }
            return false;
        }

        if (value.length < minLength || value.length > maxLength) {
            if (showMessage) {
                showError(input, input.dataset.lengthMessage);
            }
            return false;
        }

        if (showMessage) {
            hideError(input);
        }

        return true;
    }

    function updateSubmitButton(form) {
        const input = getInput(form);
        const button = getSaveButton(form);

        if (!input || !button || input.disabled) {
            return;
        }

        const isValid = validateRoleName(input, false);
        button.disabled = !isValid;
        button.classList.toggle("disabled", !isValid);
    }

    window.validateRoleFormBeforeSubmit = function () {
        const form = getForm();
        const input = getInput(form);

        if (!form || !input || input.disabled) {
            return true;
        }

        const isValid = validateRoleName(input, true);
        updateSubmitButton(form);

        return isValid;
    };

    window.initRoleEditValidation = function () {
        const form = getForm();
        const input = getInput(form);

        if (!form || !input || form.dataset.roleEditValidationInitialized === "true") {
            return;
        }

        form.dataset.roleEditValidationInitialized = "true";

        input.addEventListener("blur", function () {
            validateRoleName(input, true);
            updateSubmitButton(form);
        });

        input.addEventListener("input", function () {
            hideError(input);
            updateSubmitButton(form);
        });

        updateSubmitButton(form);
    };

    document.addEventListener("DOMContentLoaded", window.initRoleEditValidation);
})();
