document.getElementById('summaryForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const text = document.getElementById('textInput').value;

    fetch('/summarize', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(text)
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById('summaryText').innerText = data.join('\n');
        })
        .catch(error => {
            console.error('Error:', error);
        });
});
