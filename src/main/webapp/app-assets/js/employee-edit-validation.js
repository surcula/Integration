(function () {
    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function getAgeFromBirthDate(value) {
        const birthDate = new Date(value + "T00:00:00");

        if (isNaN(birthDate.getTime())) {
            return null;
        }

        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDifference = today.getMonth() - birthDate.getMonth();
        const dayDifference = today.getDate() - birthDate.getDate();

        if (monthDifference < 0 || (monthDifference === 0 && dayDifference < 0)) {
            age--;
        }

        return age;
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

        if (input.classList.contains("employee-edit-birth-date") && value.length > 0) {
            const age = getAgeFromBirthDate(value);

            if (age === null || age < 15 || age > 100) {
                if (showMessage) {
                    showError(input, input.dataset.ageMessage);
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

    function getAddressForm() {
        return document.querySelector("form[id$='addressForm']");
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

    function getAddressInputs(form) {
        return Array.prototype.slice.call(form.querySelectorAll(".employee-address-input"));
    }

    function validateAddressInput(input, showMessage) {
        const value = input.value.trim();
        const minLength = parseInt(input.dataset.minLength || "0", 10);
        const maxLength = parseInt(input.dataset.maxLength || "0", 10);

        if (input.classList.contains("employee-address-required") && value.length === 0) {
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

    function updateAddressSubmitButton(form) {
        const saveButton = form.querySelector(".employee-address-save-button");

        if (!saveButton) {
            return;
        }

        const isValid = getAddressInputs(form).every(function (input) {
            return validateAddressInput(input, false);
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

    window.validateEmployeeAddressFormBeforeSubmit = function () {
        const form = getAddressForm();

        if (!form) {
            return true;
        }

        const isValid = getAddressInputs(form).every(function (input) {
            return validateAddressInput(input, true);
        });

        updateAddressSubmitButton(form);
        return isValid;
    };

    window.initEmployeeAddressValidation = function () {
        const form = getAddressForm();

        if (!form || form.dataset.employeeAddressValidationInitialized === "true") {
            return;
        }

        form.dataset.employeeAddressValidationInitialized = "true";

        getAddressInputs(form).forEach(function (input) {
            input.addEventListener("blur", function () {
                validateAddressInput(input, true);
                updateAddressSubmitButton(form);
            });

            input.addEventListener("input", function () {
                hideError(input);
                updateAddressSubmitButton(form);
            });
        });

        updateAddressSubmitButton(form);
    };

    document.addEventListener("DOMContentLoaded", window.initEmployeeEditValidation);
    document.addEventListener("DOMContentLoaded", window.initEmployeeAddressValidation);
})();
