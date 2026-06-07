document.addEventListener("DOMContentLoaded", function () {
    const form = document.querySelector(".login100-form");
    const emailInput = document.querySelector(".login-email-input");
    const passwordInput = document.querySelector(".login-password-input");
    const loginButton = document.querySelector(".login-button");

    if (!form || !emailInput || !passwordInput || !loginButton) {
        return;
    }

    updateSubmitButton();

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function getWrapper(input) {
        return input.closest(".validate-input");
    }

    function showError(input, message) {
        const wrapper = getWrapper(input);

        if (!wrapper) {
            return;
        }

        wrapper.setAttribute("data-validate", message);
        wrapper.classList.add("alert-validate");
    }

    function hideError(input) {
        const wrapper = getWrapper(input);

        if (!wrapper) {
            return;
        }

        wrapper.classList.remove("alert-validate");
    }

    function validateEmail() {
        const email = emailInput.value.trim();

        if (email === "") {
            showError(emailInput, emailInput.dataset.requiredMessage);
            return false;
        }

        if (!isValidEmail(email)) {
            showError(emailInput, emailInput.dataset.invalidMessage);
            return false;
        }

        hideError(emailInput);
        return true;
    }

    function validatePassword() {
        const password = passwordInput.value.trim();

        if (password.length < 1) {
            showError(passwordInput, passwordInput.dataset.requiredMessage);
            return false;
        }

        hideError(passwordInput);
        return true;
    }
    function updateSubmitButton() {
        const email = emailInput.value.trim();
        const password = passwordInput.value.trim();

        const formIsValid = isValidEmail(email) && password.length > 0;

        loginButton.disabled = !formIsValid;
    }

    emailInput.addEventListener("blur", validateEmail);
    passwordInput.addEventListener("blur", validatePassword);

    emailInput.addEventListener("input", function () {
        hideError(emailInput);
        updateSubmitButton();
    });

    passwordInput.addEventListener("input", function () {
        hideError(passwordInput);
        updateSubmitButton();
    });

    form.addEventListener("submit", function (event) {
        const emailOk = validateEmail();
        const passwordOk = validatePassword();

        if (!emailOk || !passwordOk) {
            event.preventDefault();
        }
    });
});