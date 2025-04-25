function logout() {
    jQuery.ajax({
        method: "POST",
        url: "api/logout",
        success: () => {
            window.location.href="login.html"
        },
        error: () => {
            alert("Logout failed. Please try again.")
        }
    });
}