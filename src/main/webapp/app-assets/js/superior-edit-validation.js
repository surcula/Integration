(function () {
    function showError(input, message) {
        input.classList.add("is-invalid");

        const feedback = input.parentElement.querySelector(".invalid-feedback");
        if (feedback) {
            feedback.textContent = message;
            feedback.style.display = "block";
        }
    }

    function hideError(input) {
        input.classList.remove("is-invalid");

        const feedback = input.parentElement.querySelector(".invalid-feedback");
        if (feedback) {
            feedback.textContent = "";
            feedback.style.display = "";
        }
    }

    function validateRequired(input) {
        if (!input.value) {
            showError(input, input.dataset.requiredMessage);
            return false;
        }

        hideError(input);
        return true;
    }

    function validateDate(input) {
        if (!input.value) {
            hideError(input);
            return true;
        }

        if (!/^\d{4}-\d{2}-\d{2}$/.test(input.value)) {
            showError(input, input.dataset.invalidMessage);
            return false;
        }

        hideError(input);
        return true;
    }

    function validateDifferentEmployees() {
        const employeeInput = document.querySelector("#superiorForm\\:employeeId");
        const superiorInput = document.querySelector("#superiorForm\\:superiorId");

        if (!employeeInput || !superiorInput || !employeeInput.value || !superiorInput.value) {
            return true;
        }

        if (employeeInput.value === superiorInput.value) {
            showError(superiorInput, superiorInput.dataset.sameEmployeeMessage || "Employee and superior must be different.");
            return false;
        }

        hideError(superiorInput);
        return true;
    }

    window.initSuperiorEditValidation = function () {
        const requiredInputs = document.querySelectorAll(".superior-edit-required");
        const dateInputs = document.querySelectorAll(".superior-edit-date");
        const saveButton = document.querySelector(".superior-edit-save-button");

        function updateSubmitButton() {
            if (!saveButton) {
                return;
            }

            const requiredOk = Array.from(requiredInputs).every(function (input) {
                return !!input.value;
            });

            saveButton.disabled = !requiredOk;
        }

        requiredInputs.forEach(function (input) {
            input.addEventListener("blur", function () {
                validateRequired(input);
                validateDifferentEmployees();
            });

            input.addEventListener("change", function () {
                hideError(input);
                validateDifferentEmployees();
                updateSubmitButton();
            });
        });

        dateInputs.forEach(function (input) {
            input.addEventListener("blur", function () {
                validateDate(input);
            });

            input.addEventListener("input", function () {
                hideError(input);
            });
        });

        updateSubmitButton();
    };

    window.validateSuperiorFormBeforeSubmit = function () {
        const requiredInputs = document.querySelectorAll(".superior-edit-required");
        const dateInputs = document.querySelectorAll(".superior-edit-date");

        const requiredOk = Array.from(requiredInputs).every(validateRequired);
        const datesOk = Array.from(dateInputs).every(validateDate);
        const differentOk = validateDifferentEmployees();

        return requiredOk && datesOk && differentOk;
    };

    document.addEventListener("DOMContentLoaded", window.initSuperiorEditValidation);
})();
