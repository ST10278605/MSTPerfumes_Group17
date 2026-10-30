//index product counter

(() => {
    const dropdowns = document.querySelectorAll('.nav-dropdown');

    function closeDropdown(dropdown) {
        const button = dropdown.querySelector('.nav-dropdown__toggle');
        const menu = dropdown.querySelector('.nav-dropdown__menu');

        if (!button || !menu) return;

        button.setAttribute('aria-expanded', 'false');
        menu.classList.remove('is-open');
    }

    dropdowns.forEach((dropdown) => {
        const button = dropdown.querySelector('.nav-dropdown__toggle');
        const menu = dropdown.querySelector('.nav-dropdown__menu');

        if (!button || !menu) return;

        button.addEventListener('click', () => {
            const shouldOpen =
                button.getAttribute('aria-expanded') !== 'true';

            dropdowns.forEach(closeDropdown);

            if (shouldOpen) {
                button.setAttribute('aria-expanded', 'true');
                menu.classList.add('is-open');
            }
        });
    });

    document.addEventListener('click', (event) => {
        dropdowns.forEach((dropdown) => {
            if (!dropdown.contains(event.target)) {
                closeDropdown(dropdown);
            }
        });
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            dropdowns.forEach(closeDropdown);
        }
    });
})();
