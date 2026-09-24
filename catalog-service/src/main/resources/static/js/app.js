// Progressive enhancement only: client-side filter of the catalog.
// With JS disabled, every book still renders. No framework, no build step.
(function () {
    var input = document.getElementById('bookSearch');
    var grid = document.getElementById('bookGrid');
    if (!input || !grid) {
        return;
    }
    var cards = Array.prototype.slice.call(grid.querySelectorAll('.book-card'));
    var noResults = document.getElementById('noResults');

    input.addEventListener('input', function () {
        var q = input.value.trim().toLowerCase();
        var shown = 0;
        cards.forEach(function (card) {
            var haystack = card.getAttribute('data-search') || '';
            var match = haystack.indexOf(q) !== -1;
            card.hidden = !match;
            if (match) {
                shown++;
            }
        });
        if (noResults) {
            noResults.hidden = shown !== 0;
        }
    });
})();
