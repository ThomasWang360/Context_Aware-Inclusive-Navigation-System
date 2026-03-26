// Appsmith Custom Widget: Signup Form (JS panel)
// The widget triggers "onSignupSubmit" with { username, password, displayName }
// and "onLoginClick" when user clicks Log in. Configure these in the widget's
// Event Handlers in Appsmith.

(function() {
  const form = document.getElementById('signupForm');
  const username = document.getElementById('username');
  const password = document.getElementById('password');
  const displayName = document.getElementById('displayName');
  const errorMsg = document.getElementById('errorMsg');
  const loginLink = document.getElementById('loginLink');

  function triggerEvent(name, payload) {
    if (typeof appsmith !== 'undefined' && appsmith.triggerEvent) {
      appsmith.triggerEvent(name, payload);
    }
  }

  form.addEventListener('submit', function(e) {
    e.preventDefault();
    errorMsg.textContent = '';

    const u = username.value.trim();
    const p = password.value;
    const d = displayName.value.trim();

    if (!u || !p) {
      errorMsg.textContent = 'Username and password are required';
      return;
    }

    triggerEvent('onSignupSubmit', {
      username: u,
      password: p,
      displayName: d || u
    });
  });

  loginLink.addEventListener('click', function(e) {
    e.preventDefault();
    triggerEvent('onLoginClick', {});
  });
})();
