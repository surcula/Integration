(function () {
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
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

        if (input.classList.contains("employee-edit-required") && value.length === 0) {
            if (showMessage) {
                showError(input, input.dataset.requiredMessage);
            }
            return false;
        }

        if (input.classList.contains("employee-edit-email") && value.length > 0 && !isValidEmail(value)) {
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

    function validateSelect(select, showMessage) {
        const value = select.value.trim();

        if (select.classList.contains("employee-edit-required") && value.length === 0) {
            if (showMessage) {
                showError(select, select.dataset.requiredMessage);
            }
            return false;
        }

        if (showMessage) {
            hideError(select);
        }

        return true;
    }

    function getForm() {
        return document.querySelector("form[id$='basicInformationForm']");
    }

    function getInputs(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".employee-edit-input"));
    }

    function getSelects(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".employee-edit-select"));
    }

    function getFields(form) {
        return getInputs(form).concat(getSelects(form));
    }

    function validateField(field, showMessage) {
        if (field.tagName.toLowerCase() === "select") {
            return validateSelect(field, showMessage);
        }

        return validateInput(field, showMessage);
    }

    function updateSubmitButton(form) {
        const saveButton = form.querySelector(".employee-edit-save-button");

        if (!saveButton) {
            return;
        }

        const isValid = getFields(form).every(function (field) {
            return validateField(field, false);
        });

        saveButton.disabled = !isValid;
        saveButton.classList.toggle("disabled", !isValid);
    }

    window.initEmployeeEditValidation = function () {
        const form = getForm();

        if (!form || form.dataset.employeeEditValidationInitialized === "true") {
            return;
        }

        form.dataset.employeeEditValidationInitialized = "true";

        getFields(form).forEach(function (field) {
            field.addEventListener("blur", function () {
                validateField(field, true);
                updateSubmitButton(form);
            });

            field.addEventListener("input", function () {
                hideError(field);
                updateSubmitButton(form);
            });

            field.addEventListener("change", function () {
                validateField(field, true);
                updateSubmitButton(form);
            });
        });

        form.addEventListener("submit", function (event) {
            const isValid = getFields(form).every(function (field) {
                return validateField(field, true);
            });

            updateSubmitButton(form);

            if (!isValid) {
                event.preventDefault();
            }
        });

        updateSubmitButton(form);
    };

    document.addEventListener("DOMContentLoaded", window.initEmployeeEditValidation);
})();
