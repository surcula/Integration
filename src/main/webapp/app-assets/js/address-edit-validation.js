(function () {
    function getForm() {
        return document.querySelector("form[id$='addressForm']");
    }

    function getFields(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".address-edit-input"));
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

        if (input.classList.contains("address-edit-required") && value.length === 0) {
            if (showMessage) {
                showError(input, input.dataset.requiredMessage);
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

    function validateCity(form, showMessage) {
        const input = form.querySelector(".address-edit-city");
        const feedback = form.querySelector(".address-city-feedback");

        if (!input) {
            return true;
        }

        const isValid = input.value.trim().length > 0;
        input.classList.toggle("is-invalid", !isValid);

        if (feedback) {
            feedback.textContent = isValid ? "" : input.dataset.requiredMessage;
            feedback.style.display = isValid ? "" : "block";
        }

        return isValid;
    }

    function updateSubmitButton(form) {
        const saveButton = form.querySelector(".address-edit-save-button");

        if (!saveButton) {
            return;
        }

        const fieldsValid = getFields(form).every(function (field) {
            return validateInput(field, false);
        });
        const cityValid = validateCity(form, false);
        const isValid = fieldsValid && cityValid;

        saveButton.disabled = !isValid;
        saveButton.classList.toggle("disabled", !isValid);
    }

    window.validateAddressFormBeforeSubmit = function () {
        const form = getForm();

        if (!form) {
            return true;
        }

        const fieldsValid = getFields(form).every(function (field) {
            return validateInput(field, true);
        });
        const cityValid = validateCity(form, true);

        updateSubmitButton(form);
        return fieldsValid && cityValid;
    };

    window.initAddressEditValidation = function () {
        const form = getForm();

        if (!form || form.dataset.addressEditValidationInitialized === "true") {
            return;
        }

        form.dataset.addressEditValidationInitialized = "true";

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

        const cityInput = form.querySelector(".address-edit-city");
        if (cityInput) {
            cityInput.addEventListener("blur", function () {
                validateCity(form, true);
                updateSubmitButton(form);
            });
            cityInput.addEventListener("input", function () {
                updateSubmitButton(form);
            });
        }

        updateSubmitButton(form);
    };

    document.addEventListener("DOMContentLoaded", window.initAddressEditValidation);
})();
