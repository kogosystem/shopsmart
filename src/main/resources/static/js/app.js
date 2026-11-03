// Small front-end helpers for ShopSmart. The heavy lifting is all server-side;
// this only improves the on-screen experience.
document.addEventListener('DOMContentLoaded', function () {

    // Auto-dismiss success alerts after 4 seconds so they do not linger.
    document.querySelectorAll('.alert-success').forEach(function (el) {
        setTimeout(function () {
            try { bootstrap.Alert.getOrCreateInstance(el).close(); } catch (e) {}
        }, 4000);
    });

    // Quantity +/- buttons on the cart page (they submit the small forms).
    document.querySelectorAll('[data-qty-step]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = document.getElementById(btn.dataset.target);
            if (!input) return;
            var step = parseInt(btn.dataset.qtyStep, 10);
            var val = parseInt(input.value, 10) + step;
            if (val < 1) val = 1;
            input.value = val;
            if (btn.dataset.autosubmit === 'true') btn.closest('form').submit();
        });
    });
});
