(function () {
    function getForm() {
        return document.querySelector("form[id$='companyForm']");
    }

    function getFields(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".company-edit-input"));
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

    function normalizeIban(value) {
        return value.replace(/\s+/g, "").toUpperCase();
    }

    function isValidIban(value) {
        const iban = normalizeIban(value);

        if (iban.length === 0) {
            return true;
        }

        if (!/^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/.test(iban)) {
            return false;
        }

        const rearranged = iban.slice(4) + iban.slice(0, 4);
        let remainder = 0;

        for (let i = 0; i < rearranged.length; i++) {
            const char = rearranged.charAt(i);

            if (/[0-9]/.test(char)) {
                remainder = (remainder * 10 + parseInt(char, 10)) % 97;
            } else if (/[A-Z]/.test(char)) {
                const valueNumber = char.charCodeAt(0) - 55;
                remainder = (remainder * 100 + valueNumber) % 97;
            } else {
                return false;
            }
        }

        return remainder === 1;
    }

    function validateInput(input, showMessage) {
        const value = input.value.trim();
        const minLength = parseInt(input.dataset.minLength || "0", 10);
        const maxLength = parseInt(input.dataset.maxLength || "0", 10);

        if (input.classList.contains("company-edit-required") && value.length === 0) {
            if (showMessage) {
                showError(input, input.dataset.requiredMessage);
            }
            return false;
        }

        if (input.classList.contains("company-edit-iban") && !isValidIban(value)) {
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
        const saveButton = form.querySelector(".company-edit-save-button");

        if (!saveButton) {
            return;
        }

        const isValid = getFields(form).every(function (field) {
            return validateInput(field, false);
        });

        saveButton.disabled = !isValid;
        saveButton.classList.toggle("disabled", !isValid);
    }

    window.validateCompanyFormBeforeSubmit = function () {
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

    window.initCompanyEditValidation = function () {
        const form = getForm();

        if (!form || form.dataset.companyEditValidationInitialized === "true") {
            return;
        }

        form.dataset.companyEditValidationInitialized = "true";

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

    document.addEventListener("DOMContentLoaded", window.initCompanyEditValidation);
})();
