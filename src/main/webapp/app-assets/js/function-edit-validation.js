(function () {
    function getForm() {
        return document.querySelector("form[id$='functionForm']");
    }

    function getFields(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".function-edit-input"));
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

    function validateInput(input, showMessage) {
        const value = input.value.trim();
        const minLength = parseInt(input.dataset.minLength || "0", 10);
        const maxLength = parseInt(input.dataset.maxLength || "0", 10);

        if (input.classList.contains("function-edit-required") && value.length === 0) {
            if (showMessage) {
                showError(input, input.dataset.requiredMessage);
            }
            return false;
        }

        if (input.classList.contains("function-edit-number") && value.length > 0) {
            const numberValue = parseInt(value, 10);

            if (isNaN(numberValue) || numberValue < 0) {
                if (showMessage) {
                    showError(input, input.dataset.invalidMessage);
                }
                return false;
            }
        }

        if (value.length > 0 && minLength > 0 && value.length < minLength) {
            if (showMessage) {
                showError(input, input.dataset.lengthMessage);
            }
            return false;
        }

        if (maxLength > 0 && value.length > maxLength) {
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
        const saveButton = form.querySelector(".function-edit-save-button");

        if (!saveButton) {
            return;
        }

        const isValid = getFields(form).every(function (field) {
            return validateInput(field, false);
        });

        saveButton.disabled = !isValid;
        saveButton.classList.toggle("disabled", !isValid);
    }

    window.validateFunctionFormBeforeSubmit = function () {
        const form = getForm();

        if (!form) {
            return true;
        }

        const isValid = getFields(form).every(function (field) {
            return validateInput(field, true);
        });

        updateSubmitButton(form);
        return isValid;
    };

    window.initFunctionEditValidation = function () {
        const form = getForm();

        if (!form || form.dataset.functionEditValidationInitialized === "true") {
            return;
        }

        form.dataset.functionEditValidationInitialized = "true";

        getFields(form).forEach(function (field) {
            field.addEventListener("blur", function () {
                validateInput(field, true);
                updateSubmitButton(form);
            });

            field.addEventListener("input", function () {
                hideError(field);
                updateSubmitButton(form);
            });
        });

        updateSubmitButton(form);
    };

    document.addEventListener("DOMContentLoaded", window.initFunctionEditValidation);
})();
