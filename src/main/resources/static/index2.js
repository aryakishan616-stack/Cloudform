const message = document.getElementById("message");

const API_BASE_URL = window.location.port && window.location.port !== "8080"
    ? "http://localhost:8080"
    : "";

const loginView = document.getElementById("loginView");
const signupView = document.getElementById("signupView");

const showSignupBtn = document.getElementById("showSignup");
const showLoginBtn = document.getElementById("showLogin");

function showView(view) {
    const isLogin = view === "login";

    loginView.hidden = !isLogin;
    signupView.hidden = isLogin;

    message.textContent = "";
}

showSignupBtn.addEventListener("click", () => {
    showView("signup");
});

showLoginBtn.addEventListener("click", () => {
    showView("login");
});

async function submitUserForm(event, endpoint, form) {
    event.preventDefault();

    const formData = new FormData(form);

    const user = {
        username: formData.get("username"),
        password: formData.get("password")
    };

    message.textContent = "Please wait...";

    try {
        const csrfResponse = await fetch(
            `${API_BASE_URL}/api/user/csrf`,
            { credentials: "include" }
        );
        if (!csrfResponse.ok) {
            throw new Error(`Could not get security token (HTTP ${csrfResponse.status})`);
        }
        const csrfToken = await csrfResponse.text();
        const response = await fetch(
            `${API_BASE_URL}/api/user/${endpoint}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-XSRF-TOKEN": csrfToken
                },
                credentials: "include",
                body: JSON.stringify(user)
            }
        );

        const result = await response.text();

        message.textContent = result || (
            response.ok
                ? "Request completed successfully."
                : "Request failed. Please try again."
        );


        if (response.ok && endpoint === "signup") {
            form.reset();
            showView("login");
            message.textContent =
                result || "Account created successfully. Please log in.";
        } else if (response.ok && endpoint === "login") {
            window.location.assign(`${API_BASE_URL}/`);
        }

    } catch (error) {
        message.textContent =
            "Could not connect to the server. Please try again.";

        console.error("Authentication error:", error);
    }
}

document.getElementById("loginForm").addEventListener(
    "submit",
    (event) => {
        submitUserForm(event, "login", event.currentTarget);
    }
);

document.getElementById("signupForm").addEventListener(
    "submit",
    (event) => {
        submitUserForm(event, "signup", event.currentTarget);
    }
);
showView("login");
