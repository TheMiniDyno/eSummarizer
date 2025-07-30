document.addEventListener('DOMContentLoaded', function () {
    // Fade-in animation
    const signupForm = document.querySelector('section');
    signupForm.style.opacity = 0;

    setTimeout(() => {
        signupForm.style.transition = 'opacity 1s ease-in-out';
        signupForm.style.opacity = 1;
    }, 500);

    // Handle form submission
    const form = document.getElementById('signupForm');
    form.addEventListener('submit', function (e) {
        e.preventDefault(); // Prevent default form submission

        // Get form values
        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Client-side validation for password length and matching passwords
        if (password.length < 8) {
            alert('Password must be at least 8 characters long');
            return;
        }

        if (password !== confirmPassword) {
            alert('Passwords do not match');
            return;
        }

        // Create the data object to send
        const data = {
            username,
            email,
            password
        };

        // Convert data to JSON
        const jsonData = JSON.stringify(data);

        // Make the POST request to the server
        fetch('/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonData
        })
            .then(response => {
                if (response.ok) {
                    return response.json(); // Return the response data as JSON
                } else {
                    return response.text().then(text => { throw new Error(text) });
                }
            })
            .then(data => {
                // Handle success - navigate to login page
                alert('Registration successful!');
                window.location.href = '/login';
            })
            .catch(error => {
                // Handle failure
                alert('Registration failed: ' + error.message);
            });
    });
});
