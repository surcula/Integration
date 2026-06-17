(function () {
    function getForm() {
        return document.querySelector("form[id$='cityForm']");
    }

    function getFields(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".city-edit-input"));
    }

    function getFeedback(input) {
        return input.parentElement.querySelector(".invalid-feedback");
    }

    function showError(input, message) {
        const feedback = getFeedback(input);
        input.classList.add("is-invalid");

        if (feedback) {
            feedback.textContent = message || "";
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

        if (input.classList.contains("city-edit-required") && value.length === 0) {
            if (showMessage) {
                showError(input, input.dataset.requiredMessage);
            }
            return false;
        }

        if (input.classList.contains("city-edit-zip") && value.length > 0 && !/^[0-9]{4}$/.test(value)) {
            if (showMessage) {
                showError(input, input.dataset.invalidMessage);
            }
            return false;
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
        const saveButton = form.querySelector(".city-edit-save-button");

        if (!saveButton) {
            return;
        }

        const isValid = getFields(form).every(function (field) {
            return validateInput(field, false);
        });

        saveButton.disabled = !isValid;
        saveButton.classList.toggle("disabled", !isValid);
    }

    window.validateCityFormBeforeSubmit = function () {
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

    window.initCityEditValidation = function () {
        const form = getForm();

        if (!form || form.dataset.cityEditValidationInitialized === "true") {
            return;
        }

        form.dataset.cityEditValidationInitialized = "true";

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

    document.addEventListener("DOMContentLoaded", window.initCityEditValidation);
})();
