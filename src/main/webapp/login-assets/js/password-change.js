document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".password-change-form");
    const currentPasswordInput = document.querySelector(".current-password-input");
    const passwordInput = document.querySelector(".new-password-input");
    const confirmationInput = document.querySelector(".password-confirmation-input");
    const submitButton = document.querySelector(".password-change-button");

    if (!form || !passwordInput || !confirmationInput || !submitButton) {
        return;
    }

    const passwordPattern = /^(?=.*[A-Z])(?=.*\d).{8,255}$/;

    function wrapper(input) {
        return input.closest(".validate-input");
    }

    function showError(input, message) {
        const inputWrapper = wrapper(input);
        inputWrapper.setAttribute("data-validate", message);
        inputWrapper.classList.add("alert-validate");
    }

    function hideError(input) {
        wrapper(input).classList.remove("alert-validate");
    }

    function validatePassword() {
        if (passwordInput.value === "") {
            showError(passwordInput, passwordInput.dataset.requiredMessage);
            return false;
        }
        if (!passwordPattern.test(passwordInput.value)) {
            showError(passwordInput, passwordInput.dataset.formatMessage);
            return false;
        }
        hideError(passwordInput);
        return true;
    }

    function validateCurrentPassword() {
        if (!currentPasswordInput) {
            return true;
        }
        if (currentPasswordInput.value === "") {
            showError(currentPasswordInput, currentPasswordInput.dataset.requiredMessage);
            return false;
        }
        hideError(currentPasswordInput);
        return true;
    }

    function validateConfirmation() {
        if (confirmationInput.value === "") {
            showError(confirmationInput, confirmationInput.dataset.requiredMessage);
            return false;
        }
        if (confirmationInput.value !== passwordInput.value) {
            showError(confirmationInput, confirmationInput.dataset.mismatchMessage);
            return false;
        }
        hideError(confirmationInput);
        return true;
    }

    function updateButton() {
        const currentPasswordIsValid = !currentPasswordInput
                || currentPasswordInput.value.length > 0;
        submitButton.disabled = !(currentPasswordIsValid
                && passwordPattern.test(passwordInput.value)
                && confirmationInput.value === passwordInput.value);
    }

    if (currentPasswordInput) {
        currentPasswordInput.addEventListener("blur", validateCurrentPassword);
        currentPasswordInput.addEventListener("input", function () {
            hideError(currentPasswordInput);
            updateButton();
        });
    }

    passwordInput.addEventListener("blur", validatePassword);
    confirmationInput.addEventListener("blur", validateConfirmation);

    passwordInput.addEventListener("input", function () {
        hideError(passwordInput);
        if (confirmationInput.value !== "") {
            validateConfirmation();
        }
        updateButton();
    });

    confirmationInput.addEventListener("input", function () {
        hideError(confirmationInput);
        updateButton();
    });

    form.addEventListener("submit", function (event) {
        const currentPasswordIsValid = validateCurrentPassword();
        const passwordIsValid = validatePassword();
        const confirmationIsValid = validateConfirmation();
        if (!currentPasswordIsValid || !passwordIsValid || !confirmationIsValid) {
            event.preventDefault();
        }
    });

    updateButton();
});
